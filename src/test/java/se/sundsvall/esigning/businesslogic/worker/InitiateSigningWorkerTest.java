package se.sundsvall.esigning.businesslogic.worker;

import com.google.gson.Gson;
import generated.se.sundsvall.comfactfacade.CreateSigningResponse;
import generated.se.sundsvall.document.DocumentData;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;
import se.sundsvall.esigning.integration.comfactfacade.ComfactFacadeClient;
import se.sundsvall.esigning.integration.document.DocumentClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.hc.core5.http.ContentType.APPLICATION_PDF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_COMFACT_SIGNING_ID;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_ESIGNING_REQUEST;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_MUNICIPALITY_ID;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_REQUEST_ID;

@ExtendWith(MockitoExtension.class)
class InitiateSigningWorkerTest {

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

	@Mock
	private generated.se.sundsvall.document.Document documentMock;

	@Mock
	private DocumentData documentDataMock;

	@Mock
	private ResponseEntity<ByteArrayResource> byteArrayResourceMock;

	@InjectMocks
	private InitiateSigningWorker worker;

	@Captor
	private ArgumentCaptor<generated.se.sundsvall.comfactfacade.SigningRequest> signingRequestCaptor;

	@Captor
	private ArgumentCaptor<Map<String, Object>> variableValueDtoCaptor;

	@Test
	void verifyAnnotations() {
		// Assert
		assertThat(worker.getClass()).hasAnnotations(Component.class, ExternalTaskSubscription.class);
		assertThat(worker.getClass().getAnnotation(ExternalTaskSubscription.class).value()).isEqualTo("InitiateSigningTask");
	}

	@Test
	void execute() {
		// Arrange
		final var registrationNumber = "registrationNumber";
		final var municipalityId = "municipalityId";
		final var fileName = "fileName";
		final var json = "json";
		final var bean = SigningRequest.create().withRegistrationNumber(registrationNumber).withFileName(fileName);
		final var signingId = UUID.randomUUID().toString();
		final var documentDataId = UUID.randomUUID().toString();

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID)).thenReturn(municipalityId);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(documentClientMock.getDocument(municipalityId, registrationNumber)).thenReturn(documentMock);
		when(documentClientMock.getDocumentData(municipalityId, registrationNumber, documentDataId)).thenReturn(byteArrayResourceMock);
		when(comfactFacadeClientMock.createSigngingInstance(any())).thenReturn(new CreateSigningResponse().signingId(signingId));
		when(documentMock.getDocumentData()).thenReturn(List.of(documentDataMock));
		when(documentDataMock.getFileName()).thenReturn(fileName);
		when(documentDataMock.getId()).thenReturn(documentDataId);
		when(documentDataMock.getMimeType()).thenReturn(APPLICATION_PDF.getMimeType());
		when(byteArrayResourceMock.getBody()).thenReturn(new ByteArrayResource("content".getBytes()));

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(comfactFacadeClientMock).createSigngingInstance(signingRequestCaptor.capture());
		verify(externalTaskServiceMock).complete(eq(externalTaskMock), variableValueDtoCaptor.capture());
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, comfactFacadeClientMock, documentClientMock);
		verifyNoInteractions(failureHandlerMock);

		assertThat(variableValueDtoCaptor.getValue()).hasSize(1)
			.containsEntry(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID, signingId);
	}

	@Test
	void executeWhenDocumentNotFound() {
		// Arrange
		final var registrationNumber = "registrationNumber";
		final var municipalityId = "municipalityId";
		final var fileName = "fileName";
		final var json = "json";
		final var bean = SigningRequest.create().withRegistrationNumber(registrationNumber).withFileName(fileName);

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID)).thenReturn(municipalityId);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(documentClientMock.getDocument(municipalityId, registrationNumber)).thenReturn(documentMock);
		when(documentMock.getDocumentData()).thenReturn(List.of(documentDataMock));
		when(documentDataMock.getFileName()).thenReturn(fileName);
		when(documentDataMock.getMimeType()).thenReturn("otherMimeType");

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(documentClientMock).getDocument(municipalityId, registrationNumber);
		verify(externalTaskMock).getId();
		verify(externalTaskMock).getBusinessKey();
		verify(failureHandlerMock).handleException(externalTaskServiceMock, externalTaskMock,
			"DefaultProblem occured for document fileName with registration number registrationNumber when initiating signing (Not Found: File fileName of type application/pdf was not found within document with registrationNumber registrationNumber).");
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, failureHandlerMock, comfactFacadeClientMock, documentClientMock);
	}

	@Test
	void executeThrowsExceptionOnDocumentCall() {
		// Arrange
		final var registrationNumber = "registrationNumber";
		final var municipalityId = "municipalityId";
		final var fileName = "fileName";
		final var json = "json";
		final var bean = SigningRequest.create().withRegistrationNumber(registrationNumber).withFileName(fileName);
		final var problem = Problem.valueOf(Status.I_AM_A_TEAPOT, "Big and stout");

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID)).thenReturn(municipalityId);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(documentClientMock.getDocument(municipalityId, registrationNumber)).thenThrow(problem);

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(documentClientMock).getDocument(municipalityId, registrationNumber);
		verify(externalTaskMock).getId();
		verify(externalTaskMock).getBusinessKey();
		verify(failureHandlerMock).handleException(externalTaskServiceMock, externalTaskMock,
			"DefaultProblem occured for document fileName with registration number registrationNumber when initiating signing (I'm a teapot: Big and stout).");
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, failureHandlerMock, comfactFacadeClientMock, documentClientMock);
	}

	@Test
	void executeThrowsExceptionOnFacadeCall() {
		// Arrange
		final var registrationNumber = "registrationNumber";
		final var municipalityId = "municipalityId";
		final var fileName = "fileName";
		final var json = "json";
		final var bean = SigningRequest.create().withRegistrationNumber(registrationNumber).withFileName(fileName);
		final var documentDataId = UUID.randomUUID().toString();
		final var problem = Problem.valueOf(Status.I_AM_A_TEAPOT, "Big and stout");

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID)).thenReturn(municipalityId);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(documentClientMock.getDocument(municipalityId, registrationNumber)).thenReturn(documentMock);
		when(documentClientMock.getDocumentData(municipalityId, registrationNumber, documentDataId)).thenReturn(byteArrayResourceMock);
		when(comfactFacadeClientMock.createSigngingInstance(any())).thenThrow(problem);
		when(documentMock.getDocumentData()).thenReturn(List.of(documentDataMock));
		when(documentDataMock.getFileName()).thenReturn(fileName);
		when(documentDataMock.getId()).thenReturn(documentDataId);
		when(documentDataMock.getMimeType()).thenReturn(APPLICATION_PDF.getMimeType());
		when(byteArrayResourceMock.getBody()).thenReturn(new ByteArrayResource("content".getBytes()));

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(documentClientMock).getDocument(municipalityId, registrationNumber);
		verify(documentClientMock).getDocumentData(municipalityId, registrationNumber, documentDataId);
		verify(comfactFacadeClientMock).createSigngingInstance(any());
		verify(externalTaskMock).getId();
		verify(externalTaskMock).getBusinessKey();
		verify(failureHandlerMock).handleException(externalTaskServiceMock, externalTaskMock,
			"DefaultProblem occured for document fileName with registration number registrationNumber when initiating signing (I'm a teapot: Big and stout).");
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, failureHandlerMock, comfactFacadeClientMock, documentClientMock);
	}
}
