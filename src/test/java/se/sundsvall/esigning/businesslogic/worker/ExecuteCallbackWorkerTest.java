package se.sundsvall.esigning.businesslogic.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_ESIGNING_REQUEST;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_REQUEST_ID;

import com.google.gson.Gson;
import java.net.URI;
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
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.callback.CallbackClient;
import se.sundsvall.esigning.integration.camunda.CamundaClient;

@ExtendWith(MockitoExtension.class)
class ExecuteCallbackWorkerTest {

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
	private CallbackClient callbackClientMock;

	@InjectMocks
	private ExecuteCallbackWorker worker;

	@Captor
	private ArgumentCaptor<URI> uriCaptor;

	@Test
	void verifyAnnotations() {
		// Assert
		assertThat(worker.getClass()).hasAnnotations(Component.class, ExternalTaskSubscription.class);
		assertThat(worker.getClass().getAnnotation(ExternalTaskSubscription.class).value()).isEqualTo("ExecuteCallbackTask");
	}

	@Test
	void execute() {
		// Arrange
		final var protocol = "http";
		final var callbackUrl = "callback.url";
		final var json = "json";
		final var processinstanceId = UUID.randomUUID().toString();
		final var bean = SigningRequest.create()
			.withCallbackUrl(protocol + "://" + callbackUrl)
			.withExpires(OffsetDateTime.MAX)
			.withRegistrationNumber("2024-ACTIVE");

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(externalTaskMock.getProcessInstanceId()).thenReturn(processinstanceId);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(externalTaskMock).getProcessInstanceId();
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(callbackClientMock).sendRequest(uriCaptor.capture());
		verify(externalTaskServiceMock).complete(externalTaskMock);
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, callbackClientMock, gsonMock);
		verifyNoInteractions(failureHandlerMock);

		assertThat(uriCaptor.getValue().getScheme()).isEqualTo(protocol);
		assertThat(uriCaptor.getValue().getHost()).isEqualTo(callbackUrl);
		assertThat(uriCaptor.getValue().getQuery()).matches("processId=[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}");
	}

	@Test
	void executeThrowsException() {
		// Arrange
		final var callbackUrl = "callbackUrl?key=value";
		final var json = "json";
		final var processinstanceId = UUID.randomUUID().toString();
		final var bean = SigningRequest.create()
			.withCallbackUrl(callbackUrl)
			.withExpires(OffsetDateTime.MAX)
			.withRegistrationNumber("2024-ACTIVE");

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(externalTaskMock.getProcessInstanceId()).thenReturn(processinstanceId);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);

		final var problem = Problem.valueOf(Status.I_AM_A_TEAPOT, "Big and stout");

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		doThrow(problem).when(externalTaskServiceMock).complete(externalTaskMock);

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(externalTaskMock).getProcessInstanceId();
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(callbackClientMock).sendRequest(any());
		verify(externalTaskServiceMock).complete(externalTaskMock);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_REQUEST_ID);
		verify(externalTaskMock).getId();
		verify(externalTaskMock).getBusinessKey();
		verify(failureHandlerMock).handleException(eq(externalTaskServiceMock), eq(externalTaskMock), anyString());
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, failureHandlerMock);
	}
}
