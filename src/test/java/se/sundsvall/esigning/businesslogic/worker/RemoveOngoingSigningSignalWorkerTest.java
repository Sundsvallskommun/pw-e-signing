package se.sundsvall.esigning.businesslogic.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_ESIGNING_REQUEST;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_MUNICIPALITY_ID;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_REQUEST_ID;
import static se.sundsvall.esigning.Constants.DOCUMENT_METADATA_KEY_SIGNING_ID;
import static se.sundsvall.esigning.Constants.DOCUMENT_METADATA_KEY_SIGNING_IN_PROGRESS;

import com.google.gson.Gson;
import generated.se.sundsvall.document.Document;
import generated.se.sundsvall.document.DocumentMetadata;
import generated.se.sundsvall.document.DocumentUpdateRequest;
import java.util.ArrayList;
import java.util.List;
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
import se.sundsvall.esigning.Constants;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;
import se.sundsvall.esigning.integration.document.DocumentClient;

@ExtendWith(MockitoExtension.class)
class RemoveOngoingSigningSignalWorkerTest {

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

	@InjectMocks
	private RemoveOngoingSigningSignalWorker worker;

	@Captor
	private ArgumentCaptor<DocumentUpdateRequest> documentUpdateRequestCaptor;

	@Test
	void verifyAnnotations() {
		// Assert
		assertThat(worker.getClass()).hasAnnotations(Component.class, ExternalTaskSubscription.class);
		assertThat(worker.getClass().getAnnotation(ExternalTaskSubscription.class).value()).isEqualTo("RemoveOngoingSigningSignalTask");
	}

	@Test
	void execute() {
		// Arrange
		final var json = "json";
		final var registrationNumber = "registrationNumber";
		final var municipalityId = "municipalityId";
		final var bean = SigningRequest.create().withRegistrationNumber(registrationNumber);
		final var existingMetadata = new ArrayList<>(List.of(
			new DocumentMetadata().key(DOCUMENT_METADATA_KEY_SIGNING_IN_PROGRESS).value("true"),
			new DocumentMetadata().key(DOCUMENT_METADATA_KEY_SIGNING_ID).value("signingId")));

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID)).thenReturn(municipalityId);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(documentClientMock.getDocument(municipalityId, registrationNumber)).thenReturn(new Document().metadataList(existingMetadata));

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(documentClientMock).getDocument(municipalityId, registrationNumber);
		verify(documentClientMock).updateDocument(eq(municipalityId), eq(registrationNumber), documentUpdateRequestCaptor.capture());

		verify(externalTaskServiceMock).complete(externalTaskMock);
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, documentClientMock);
		verifyNoInteractions(failureHandlerMock);

		assertThat(documentUpdateRequestCaptor.getValue().getArchive()).isNull();
		assertThat(documentUpdateRequestCaptor.getValue().getCreatedBy()).isEqualTo(Constants.DOCUMENT_USER);
		assertThat(documentUpdateRequestCaptor.getValue().getDescription()).isNull();
		assertThat(documentUpdateRequestCaptor.getValue().getMetadataList()).hasSize(1).allSatisfy(metadata -> {
			assertThat(metadata.getKey()).isEqualTo(DOCUMENT_METADATA_KEY_SIGNING_ID);
			assertThat(metadata.getValue()).isEqualTo("signingId");
		});
	}

	@Test
	void executeThrowsException() {
		// Arrange
		final var json = "json";
		final var registrationNumber = "registrationNumber";
		final var municipalityId = "municipalityId";
		final var bean = SigningRequest.create().withRegistrationNumber(registrationNumber);
		final var problem = Problem.valueOf(Status.I_AM_A_TEAPOT, "Big and stout");

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID)).thenReturn(municipalityId);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(documentClientMock.getDocument(municipalityId, registrationNumber)).thenReturn(new Document().metadataList(new ArrayList<>()));
		when(documentClientMock.updateDocument(eq(municipalityId), eq(registrationNumber), any())).thenThrow(problem);

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(documentClientMock).getDocument(municipalityId, registrationNumber);
		verify(documentClientMock).updateDocument(eq(municipalityId), eq(registrationNumber), any());
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_REQUEST_ID);
		verify(externalTaskMock).getId();
		verify(externalTaskMock).getBusinessKey();
		verify(failureHandlerMock).handleException(eq(externalTaskServiceMock), eq(externalTaskMock), anyString());
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, failureHandlerMock, documentClientMock);
	}
}
