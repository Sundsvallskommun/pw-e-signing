package se.sundsvall.esigning.businesslogic.worker;

import com.google.gson.Gson;
import generated.se.sundsvall.comfactfacade.SigningInstance;
import generated.se.sundsvall.document.DocumentMetadata;
import generated.se.sundsvall.document.DocumentUpdateRequest;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import se.sundsvall.esigning.integration.comfactfacade.ComfactFacadeClient;
import se.sundsvall.esigning.integration.document.DocumentClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_CALLBACK_PRESENT;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_COMFACT_SIGNING_ID;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_ESIGNING_REQUEST;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_MUNICIPALITY_ID;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_REQUEST_ID;
import static se.sundsvall.esigning.Constants.DOCUMENT_METADATA_KEY_SIGNING_IN_PROGRESS;
import static se.sundsvall.esigning.Constants.DOCUMENT_METADATA_KEY_SIGNING_STATUS;
import static se.sundsvall.esigning.Constants.DOCUMENT_METADATA_KEY_SIGNING_STATUS_MESSAGE;

@ExtendWith(MockitoExtension.class)
class HandleNotSignedDocumentWorkerTest {

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
	private ComfactFacadeClient comfactFacadeClientMock;

	@Mock
	private DocumentClient documentClientMock;

	@InjectMocks
	private HandleNotSignedDocumentWorker worker;

	@Captor
	private ArgumentCaptor<DocumentUpdateRequest> documentUpdateRequestCaptor;

	@Test
	void verifyAnnotations() {
		// Assert
		assertThat(worker.getClass()).hasAnnotations(Component.class, ExternalTaskSubscription.class);
		assertThat(worker.getClass().getAnnotation(ExternalTaskSubscription.class).value()).isEqualTo("HandleNotSignedDocumentTask");
	}

	@ParameterizedTest
	@ValueSource(booleans = {
		true, false
	})
	void execute(boolean callbackPresent) {
		// Arrange
		final var json = "json";
		final var registrationNumber = "registrationNumber";
		final var municipalityId = "municipalityId";
		final var code = "code";
		final var message = "message";
		final var signingId = UUID.randomUUID().toString();
		final var existingMetadata = new ArrayList<>(List.of(new DocumentMetadata().key(DOCUMENT_METADATA_KEY_SIGNING_IN_PROGRESS).value("true")));
		final var bean = SigningRequest.create()
			.withRegistrationNumber(registrationNumber)
			.withCallbackUrl(callbackPresent ? "callbackUrl" : null);
		final var status = new generated.se.sundsvall.comfactfacade.Status()
			.code(code)
			.message(message);

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID)).thenReturn(municipalityId);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID)).thenReturn(signingId);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(comfactFacadeClientMock.getSigningInstance(municipalityId, signingId)).thenReturn(new SigningInstance().status(status));
		when(documentClientMock.getDocument(municipalityId, registrationNumber)).thenReturn(new generated.se.sundsvall.document.Document().metadataList(existingMetadata));

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(comfactFacadeClientMock).getSigningInstance(municipalityId, signingId);
		verify(documentClientMock).getDocument(municipalityId, registrationNumber);
		verify(documentClientMock).updateDocument(eq(municipalityId), eq(registrationNumber), documentUpdateRequestCaptor.capture());
		verify(externalTaskServiceMock).complete(externalTaskMock, Map.of(CAMUNDA_VARIABLE_CALLBACK_PRESENT, callbackPresent));
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, comfactFacadeClientMock, documentClientMock);
		verifyNoInteractions(failureHandlerMock);

		assertThat(documentUpdateRequestCaptor.getValue()).satisfies(req -> {
			assertThat(req.getArchive()).isNull();
			assertThat(req.getCreatedBy()).isEqualTo(Constants.DOCUMENT_USER);
			assertThat(req.getDescription()).isNull();
			assertThat(req.getMetadataList()).hasSize(3).satisfiesExactlyInAnyOrder(metadata -> {
				assertThat(metadata.getKey()).isEqualTo(DOCUMENT_METADATA_KEY_SIGNING_STATUS);
				assertThat(metadata.getValue()).isEqualTo("code");
			}, metadata -> {
				assertThat(metadata.getKey()).isEqualTo(DOCUMENT_METADATA_KEY_SIGNING_STATUS_MESSAGE);
				assertThat(metadata.getValue()).isEqualTo("message");
			}, metadata -> {
				assertThat(metadata.getKey()).isEqualTo(DOCUMENT_METADATA_KEY_SIGNING_IN_PROGRESS);
				assertThat(metadata.getValue()).isEqualTo("true");
			});
		});
	}

	@Test
	void executeThrowsException() {
		// Arrange
		final var registrationNumber = "registrationNumber";
		final var municipalityId = "municipalityId";
		final var signingId = UUID.randomUUID().toString();
		final var json = "json";
		final var bean = SigningRequest.create().withRegistrationNumber(registrationNumber);
		final var problem = Problem.valueOf(Status.I_AM_A_TEAPOT, "Big and stout");
		final var status = new generated.se.sundsvall.comfactfacade.Status();

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID)).thenReturn(municipalityId);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID)).thenReturn(signingId);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(comfactFacadeClientMock.getSigningInstance(municipalityId, signingId)).thenReturn(new SigningInstance().status(status));
		when(documentClientMock.getDocument(municipalityId, registrationNumber)).thenReturn(new generated.se.sundsvall.document.Document().metadataList(new ArrayList<>()));
		when(documentClientMock.updateDocument(anyString(), any(), any())).thenThrow(problem);

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
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, failureHandlerMock, comfactFacadeClientMock, documentClientMock);
	}
}
