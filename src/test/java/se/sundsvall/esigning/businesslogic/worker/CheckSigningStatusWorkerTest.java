package se.sundsvall.esigning.businesslogic.worker;

import com.google.gson.Gson;
import generated.se.sundsvall.comfactfacade.SigningInstance;
import generated.se.sundsvall.comfactfacade.Status;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;
import se.sundsvall.esigning.integration.comfactfacade.ComfactFacadeClient;

import java.util.Map;
import java.util.UUID;

import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_COMFACT_SIGNING_ID;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_COMFACT_SIGNING_STATUS;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_ESIGNING_REQUEST;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_MUNICIPALITY_ID;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_REQUEST_ID;

@ExtendWith(MockitoExtension.class)
class CheckSigningStatusWorkerTest {

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

	@InjectMocks
	private CheckSigningStatusWorker worker;

	@Test
	void verifyAnnotations() {
		// Assert
		assertThat(worker.getClass()).hasAnnotations(Component.class, ExternalTaskSubscription.class);
		assertThat(worker.getClass().getAnnotation(ExternalTaskSubscription.class).value()).isEqualTo("CheckSigningStatusTask");
	}

	@ParameterizedTest
	@ValueSource(strings = { "Created", "Active", "Halted", "Faulty", "Completed", "Approved", "Declined", "Withdrawn", "Expired" })
	@NullSource
	void execute(String statusCode) {
		// Arrange
		final var requestId = UUID.randomUUID().toString();
		final var json = "json";
		final var bean = SigningRequest.create();
		final var signingId = UUID.randomUUID().toString();
		final var municipalityId = "municipalityId";
		final var signingInstance = new SigningInstance()
			.status(new Status().code(statusCode));

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(requestId);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID)).thenReturn(municipalityId);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID)).thenReturn(signingId);
		when(comfactFacadeClientMock.getSigningInstance(municipalityId, signingId)).thenReturn(signingInstance);

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(comfactFacadeClientMock).getSigningInstance(municipalityId, signingId);
		verify(externalTaskServiceMock).complete(externalTaskMock, Map.of(CAMUNDA_VARIABLE_COMFACT_SIGNING_STATUS, isNull(statusCode) ? "Notpresent" : statusCode));
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, comfactFacadeClientMock);
		verifyNoInteractions(failureHandlerMock);
	}

	@Test
	void executeThrowsExceptionOnFacadeCall() {
		// Arrange
		final var problem = Problem.valueOf(org.zalando.problem.Status.I_AM_A_TEAPOT, "Big and stout");
		final var requestId = UUID.randomUUID().toString();
		final var municipalityId = "municipalityId";
		final var json = "json";
		final var bean = SigningRequest.create();
		final var signingId = UUID.randomUUID().toString();

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(requestId);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID)).thenReturn(municipalityId);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID)).thenReturn(signingId);
		when(comfactFacadeClientMock.getSigningInstance(anyString(), any())).thenThrow(problem);

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(comfactFacadeClientMock).getSigningInstance(municipalityId, signingId);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_REQUEST_ID);
		verify(externalTaskMock).getId();
		verify(externalTaskMock).getBusinessKey();
		verify(failureHandlerMock).handleException(eq(externalTaskServiceMock), eq(externalTaskMock), anyString());
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, comfactFacadeClientMock, failureHandlerMock);
	}
}
