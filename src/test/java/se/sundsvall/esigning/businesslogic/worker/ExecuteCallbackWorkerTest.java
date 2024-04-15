package se.sundsvall.esigning.businesslogic.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_ESIGNING_REQUEST;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_REQUEST_ID;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import com.google.gson.Gson;

import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
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
	private RestClient restClientMock;

	@Mock
	private RequestHeadersUriSpec<?> requestHeadersUriSpecMock;

	@Mock
	private RequestHeadersSpec<?> requestHeadersSpecMock;

	@Mock
	private ResponseSpec responseSpecMock;

	@InjectMocks
	private ExecuteCallbackWorker worker;

	@Test
	void verifyAnnotations() {
		// Assert
		assertThat(worker.getClass()).hasAnnotations(Component.class, ExternalTaskSubscription.class);
		assertThat(worker.getClass().getAnnotation(ExternalTaskSubscription.class).value()).isEqualTo("ExecuteCallbackTask");
	}

	@Test
	void execute() {
		// Arrange
		final var callbackUrl = "callbackUrl";
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
		when(restClientMock.get()).thenAnswer(x -> requestHeadersUriSpecMock);
		when(requestHeadersUriSpecMock.uri(anyString())).thenAnswer(mock -> requestHeadersSpecMock);
		when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(externalTaskMock).getProcessInstanceId();
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(restClientMock).get();
		verify(requestHeadersUriSpecMock).uri(callbackUrl + "?processId=" + processinstanceId);
		verify(requestHeadersSpecMock).retrieve();
		verify(responseSpecMock).toBodilessEntity();
		verify(externalTaskServiceMock).complete(externalTaskMock);
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, restClientMock, requestHeadersUriSpecMock, requestHeadersSpecMock, responseSpecMock, gsonMock);
		verifyNoInteractions(failureHandlerMock);
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
		when(restClientMock.get()).thenAnswer(x -> requestHeadersUriSpecMock);
		when(requestHeadersUriSpecMock.uri(anyString())).thenAnswer(mock -> requestHeadersSpecMock);
		when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);

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
		verify(restClientMock).get();
		verify(requestHeadersUriSpecMock).uri(callbackUrl + "&processId=" + processinstanceId);
		verify(requestHeadersSpecMock).retrieve();
		verify(responseSpecMock).toBodilessEntity();
		verify(externalTaskServiceMock).complete(externalTaskMock);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_REQUEST_ID);
		verify(externalTaskMock).getId();
		verify(externalTaskMock).getBusinessKey();
		verify(failureHandlerMock).handleException(eq(externalTaskServiceMock), eq(externalTaskMock), anyString());
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, restClientMock, requestHeadersUriSpecMock, requestHeadersSpecMock, responseSpecMock, gsonMock, failureHandlerMock);
	}
}
