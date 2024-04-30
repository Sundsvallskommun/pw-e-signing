package se.sundsvall.esigning.businesslogic.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_COMFACT_SIGNING_ID;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_ESIGNING_REQUEST;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_REQUEST_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import com.google.gson.Gson;

import generated.se.sundsvall.document.Document;
import generated.se.sundsvall.document.DocumentMetadata;
import generated.se.sundsvall.document.DocumentUpdateRequest;
import se.sundsvall.esigning.Constants;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;
import se.sundsvall.esigning.integration.document.DocumentClient;

@ExtendWith(MockitoExtension.class)
class AddSigningIdWorkerTest {

	private static final String REQUEST_ID = "RequestId";

	@Mock
	private CamundaClient camundaClientMock;

	@Mock
	private ExternalTask externalTaskMock;

	@Mock
	private ExternalTaskService externalTaskServiceMock;

	@Mock
	private FailureHandler failureHandlerMock;

	@Mock
	private Gson gsonMock;

	@Mock
	private DocumentClient documentClientMock;

	@Mock
	private Document documentMock;

	@InjectMocks
	private AddSigningIdWorker worker;

	@Captor
	private ArgumentCaptor<DocumentUpdateRequest> doucmentUpdateRequestCaptor;

	@Test
	void verifyAnnotations() {
		// Assert
		assertThat(worker.getClass()).hasAnnotations(Component.class, ExternalTaskSubscription.class);
		assertThat(worker.getClass().getAnnotation(ExternalTaskSubscription.class).value()).isEqualTo("AddSigningIdTask");
	}

	@Test
	void execute() {
		// Arrange
		final var registrationNumber = "registrationNumber";
		final var json = "json";
		final var existingMetadata = new ArrayList<>(List.of(new DocumentMetadata("someKey", "someValue")));
		final var bean = SigningRequest.create().withRegistrationNumber(registrationNumber);
		final var signingId = UUID.randomUUID().toString();

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID)).thenReturn(signingId);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(documentClientMock.getDocument(registrationNumber)).thenReturn(documentMock);
		when(documentMock.getMetadataList()).thenReturn(existingMetadata);

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(externalTaskServiceMock).complete(externalTaskMock);
		verify(documentClientMock).updateDocument(eq(registrationNumber), doucmentUpdateRequestCaptor.capture());
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, documentClientMock);
		verifyNoInteractions(failureHandlerMock);

		assertThat(doucmentUpdateRequestCaptor.getValue().getArchive()).isNull();
		assertThat(doucmentUpdateRequestCaptor.getValue().getCreatedBy()).isEqualTo(Constants.DOCUMENT_USER);
		assertThat(doucmentUpdateRequestCaptor.getValue().getDescription()).isNull();
		assertThat(doucmentUpdateRequestCaptor.getValue().getMetadataList()).hasSize(2).satisfiesExactlyInAnyOrder(meta -> {
			assertThat(meta.getKey()).isEqualTo(Constants.DOCUMENT_METADATA_KEY_SIGNING_ID);
			assertThat(meta.getValue()).isEqualTo(signingId);
		}, meta -> {
			assertThat(meta.getKey()).isEqualTo("someKey");
			assertThat(meta.getValue()).isEqualTo("someValue");
		});
	}

	@Test
	void executeThrowsExceptionOnDocumentClientCall() {
		// Arrange
		final var registrationNumber = "registrationNumber";
		final var json = "json";
		final var existingMetadata = new ArrayList<>(List.of(new DocumentMetadata("someKey", "someValue")));
		final var bean = SigningRequest.create().withRegistrationNumber(registrationNumber);
		final var problem = Problem.valueOf(Status.I_AM_A_TEAPOT, "Big and stout");

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(documentClientMock.updateDocument(any(), any())).thenThrow(problem);
		when(documentClientMock.getDocument(registrationNumber)).thenReturn(documentMock);
		when(documentMock.getMetadataList()).thenReturn(existingMetadata);

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(documentClientMock).updateDocument(eq(registrationNumber), any());
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_REQUEST_ID);
		verify(externalTaskMock).getId();
		verify(externalTaskMock).getBusinessKey();
		verify(failureHandlerMock).handleException(eq(externalTaskServiceMock), eq(externalTaskMock), anyString());
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, failureHandlerMock, documentClientMock);
	}
}
