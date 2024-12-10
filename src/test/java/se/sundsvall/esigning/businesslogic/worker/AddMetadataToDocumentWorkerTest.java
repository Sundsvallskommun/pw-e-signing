package se.sundsvall.esigning.businesslogic.worker;

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

import com.google.gson.Gson;
import generated.se.sundsvall.comfactfacade.Signatory;
import generated.se.sundsvall.comfactfacade.SigningInstance;
import generated.se.sundsvall.document.DocumentMetadata;
import generated.se.sundsvall.document.DocumentUpdateRequest;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

@ExtendWith(MockitoExtension.class)
class AddMetadataToDocumentWorkerTest {

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
	private AddMetadataToDocumentWorker worker;

	@Captor
	private ArgumentCaptor<DocumentUpdateRequest> documentUpdateRequestCaptor;

	@Test
	void verifyAnnotations() {
		// Assert
		assertThat(worker.getClass()).hasAnnotations(Component.class, ExternalTaskSubscription.class);
		assertThat(worker.getClass().getAnnotation(ExternalTaskSubscription.class).value()).isEqualTo("AddMetadataToDocumentTask");
	}

	@ParameterizedTest
	@ValueSource(booleans = {
		true, false
	})
	void execute(boolean callbackPresent) throws Exception {
		// Arrange
		final var json = "json";
		final var registrationNumber = "registrationNumber";
		final var municipalityId = "municipalityId";
		final var bean = SigningRequest.create()
			.withRegistrationNumber(registrationNumber)
			.withCallbackUrl(callbackPresent ? "callbackUrl" : null);
		final var signingId = UUID.randomUUID().toString();
		final var existingMetadata = new ArrayList<>(List.of(new DocumentMetadata().key(DOCUMENT_METADATA_KEY_SIGNING_IN_PROGRESS).value("true")));

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID)).thenReturn(signingId);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID)).thenReturn(municipalityId);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(comfactFacadeClientMock.getSigningInstance(municipalityId, signingId)).thenReturn(new SigningInstance()
			.signatories(List.of(createSignatory("1.name", "1.partyId", "1.email"), createSignatory("2.name", "2.partyId", "2.email"))));
		when(documentClientMock.getDocument(municipalityId, registrationNumber)).thenReturn(new generated.se.sundsvall.document.Document().metadataList(existingMetadata));

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
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
				assertThat(metadata.getKey()).isEqualTo("signatory.1");
				assertThat(metadata.getValue()).isEqualTo("{\"name\":\"1.name\",\"partyId\":\"1.partyId\",\"email\":\"1.email\"}");
			}, metadata -> {
				assertThat(metadata.getKey()).isEqualTo("signatory.2");
				assertThat(metadata.getValue()).isEqualTo("{\"name\":\"2.name\",\"partyId\":\"2.partyId\",\"email\":\"2.email\"}");
			}, metadata -> {
				assertThat(metadata.getKey()).isEqualTo(DOCUMENT_METADATA_KEY_SIGNING_IN_PROGRESS);
				assertThat(metadata.getValue()).isEqualTo("true");
			});
		});
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
		final var municipalityId = "municipalityId";

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID)).thenReturn(municipalityId);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID)).thenReturn(signingId);
		when(comfactFacadeClientMock.getSigningInstance(anyString(), any())).thenThrow(problem);

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(comfactFacadeClientMock).getSigningInstance(municipalityId, signingId);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_REQUEST_ID);
		verify(externalTaskMock).getId();
		verify(externalTaskMock).getBusinessKey();
		verify(failureHandlerMock).handleException(externalTaskServiceMock, externalTaskMock,
			"DefaultProblem occured for document fileName with registration number registrationNumber when adding signatory metadata to document (I'm a teapot: Big and stout).");
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, failureHandlerMock, comfactFacadeClientMock, documentClientMock);
	}

	private static Signatory createSignatory(String name, String partyId, String email) {
		return new Signatory()
			.name(name)
			.partyId(partyId)
			.email(email);
	}
}
