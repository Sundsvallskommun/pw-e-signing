package se.sundsvall.esigning.businesslogic.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_COMFACT_SIGNING_ID;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_ESIGNING_REQUEST;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_REQUEST_ID;

import java.time.OffsetDateTime;
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
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import com.google.gson.Gson;

import generated.se.sundsvall.comfactfacade.Document;
import generated.se.sundsvall.comfactfacade.SigningInstance;
import generated.se.sundsvall.document.DocumentDataCreateRequest;
import generated.se.sundsvall.document.DocumentUpdateRequest;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;
import se.sundsvall.esigning.integration.comfactfacade.ComfactFacadeClient;
import se.sundsvall.esigning.integration.document.DocumentClient;
@ExtendWith(MockitoExtension.class)
class AddSignedDocumentWorkerTest {

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
	private AddSignedDocumentWorker worker;

	@Captor
	private ArgumentCaptor<MultipartFile> multiPartFileCaptor;
	@Captor
	private ArgumentCaptor<DocumentUpdateRequest> documentUpdateRequestCaptor;

	@Test
	void verifyAnnotations() {
		// Assert
		assertThat(worker.getClass()).hasAnnotations(Component.class, ExternalTaskSubscription.class);
		assertThat(worker.getClass().getAnnotation(ExternalTaskSubscription.class).value()).isEqualTo("AddSignedDocumentTask");
	}

	@Test
	void execute() throws Exception {
		// Arrange
		final var json = "json";
		final var content = "content";
		final var fileName = "fileName";
		final var name = "name";
		final var mimeType = "mimeType";
		final var registrationNumber = "registrationNumber";
		final var bean = SigningRequest.create().withRegistrationNumber(registrationNumber);
		final var signingId = UUID.randomUUID().toString();
		final var document = new Document()
			.content(content.getBytes())
			.fileName(fileName)
			.name(name)
			.mimeType(mimeType);

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID)).thenReturn(signingId);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(comfactFacadeClientMock.getSigningInstance(signingId)).thenReturn(new SigningInstance()
			.signedDocument(document));

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(comfactFacadeClientMock).getSigningInstance(signingId);
		verify(documentClientMock).addFileToDocument(eq(registrationNumber), eq(new DocumentDataCreateRequest("E-signing-process")), multiPartFileCaptor.capture());
		verify(externalTaskServiceMock).complete(externalTaskMock);
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, comfactFacadeClientMock, documentClientMock);
		verifyNoInteractions(failureHandlerMock);

		assertThat(multiPartFileCaptor.getValue().getBytes()).isEqualTo(content.getBytes());
		assertThat(multiPartFileCaptor.getValue().getContentType()).isEqualTo(mimeType);
		assertThat(multiPartFileCaptor.getValue().getName()).isEqualTo(name);
		assertThat(multiPartFileCaptor.getValue().getOriginalFilename()).isEqualTo(fileName);
	}

	@Test
	void executeThrowsException() {
		// Arrange
		final var json = "json";
		final var bean = SigningRequest.create()
			.withExpires(OffsetDateTime.MAX)
			.withFileName("fileName")
			.withRegistrationNumber("registrationNumber");
		final var problem = Problem.valueOf(Status.I_AM_A_TEAPOT, "Big and stout");
		final var signingId = UUID.randomUUID().toString();

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID)).thenReturn(signingId);
		when(comfactFacadeClientMock.getSigningInstance(any())).thenThrow(problem);

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(comfactFacadeClientMock).getSigningInstance(signingId);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_REQUEST_ID);
		verify(externalTaskMock).getId();
		verify(externalTaskMock).getBusinessKey();
		verify(failureHandlerMock).handleException(externalTaskServiceMock, externalTaskMock, 
			"DefaultProblem occured for document fileName with registration number registrationNumber when adding signed document (I'm a teapot: Big and stout).");
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, failureHandlerMock, comfactFacadeClientMock, documentClientMock);
	}
}
