package se.sundsvall.esigning.businesslogic.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_ESIGNING_REQUEST;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_REQUEST_ID;

import java.util.UUID;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import generated.se.sundsvall.camunda.VariableValueDto;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;

@ExtendWith(MockitoExtension.class)
class AbstractWorkerTest {

	private static class Worker extends AbstractWorker {

		protected Worker(CamundaClient camundaClient, FailureHandler failureHandler, Gson gson) {
			super(camundaClient, failureHandler, gson);
		}

		@Override
		protected void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService) {}
	} // Test class extending the abstract class

	@Mock
	private CamundaClient camundaClientMock;

	@Mock
	private ExternalTask externalTaskMock;

	@Mock
	private FailureHandler failureHandlerMock;

	@Mock
	private ExternalTaskService externalTaskServiceMock;

	@Mock
	private Logger loggerMock;

	@Mock
	private Gson gsonMock;

	@InjectMocks
	private Worker worker;

	@Test
	void setProcessInstanceVariable() {
		// Arrange
		final var uuid = UUID.randomUUID().toString();
		final var key = "someKey";
		final var value = new VariableValueDto().type(ValueType.STRING.getName()).value("someValue");

		when(externalTaskMock.getProcessInstanceId()).thenReturn(uuid);

		// Act
		worker.setProcessInstanceVariable(externalTaskMock, key, value);

		// Assert and verify
		verify(externalTaskMock).getProcessInstanceId();
		verify(camundaClientMock).setProcessInstanceVariable(uuid, key, value);
		verifyNoMoreInteractions(externalTaskMock, camundaClientMock);
		verifyNoInteractions(externalTaskServiceMock, failureHandlerMock, gsonMock);
	}

	@Test
	void execute() {
		// Arrange
		final var requestId = UUID.randomUUID().toString();

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(requestId);

		// Mock static RequestId to verify that static method is being called
		try (MockedStatic<RequestId> requestIdMock = mockStatic(RequestId.class)) {

			// Act
			worker.execute(externalTaskMock, externalTaskServiceMock);

			// Verify static method
			requestIdMock.verify(() -> RequestId.init(requestId));
		}

		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_REQUEST_ID);
		verifyNoMoreInteractions(externalTaskMock);
		verifyNoInteractions(camundaClientMock, externalTaskServiceMock, failureHandlerMock, gsonMock);
	}

	@Test
	void getSigningRequest() {
		final var json = "json";
		final var bean = SigningRequest.create();

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);

		final var signingRequest = worker.getSigningRequest(externalTaskMock);

		assertThat(signingRequest).isEqualTo(bean);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verifyNoMoreInteractions(externalTaskMock, gsonMock);
		verifyNoInteractions(camundaClientMock, externalTaskServiceMock, failureHandlerMock);
	}

	@Test
	void logInfo() {
		// Mock static LoggerFactory to enable spy and to verify that static method is being called
		try (MockedStatic<LoggerFactory> loggerFactoryMock = Mockito.mockStatic(LoggerFactory.class)) {
			loggerFactoryMock.when(() -> LoggerFactory.getLogger(Worker.class)).thenReturn(loggerMock);

			// Act
			final var worker = new AbstractWorkerTest.Worker(null, null, null);
			worker.logInfo("message with parameters {} {}", "parameter1", "parameter2");

			// Assert and verify
			loggerFactoryMock.verify(() -> LoggerFactory.getLogger(Worker.class));
			verify(loggerMock).info("message with parameters {} {}", new Object[] { "parameter1", "parameter2" });
			verifyNoInteractions(camundaClientMock, failureHandlerMock, externalTaskServiceMock, externalTaskMock, gsonMock);
		}
	}

	@Test
	void logException() {
		// Arrange
		final var id = "errandId";
		final var businessKey = "businessKey";

		when(externalTaskMock.getId()).thenReturn(id);
		when(externalTaskMock.getBusinessKey()).thenReturn(businessKey);

		// Mock static LoggerFactory to enable spy and to verify that static method is being called
		try (MockedStatic<LoggerFactory> loggerFactoryMock = Mockito.mockStatic(LoggerFactory.class)) {
			loggerFactoryMock.when(() -> LoggerFactory.getLogger(Worker.class)).thenReturn(loggerMock);

			// Act
			final var worker = new AbstractWorkerTest.Worker(null, null, null);
			final var exception = new Exception("testexception");
			worker.logException(externalTaskMock, exception);

			// Assert and verify
			loggerFactoryMock.verify(() -> LoggerFactory.getLogger(Worker.class));
			verify(loggerMock).error("{} occurred in {} for task with id {} and businesskey {}", exception.getClass().getSimpleName(), Worker.class.getSimpleName(), id, businessKey, exception);
			verify(externalTaskMock).getBusinessKey();
			verifyNoMoreInteractions(externalTaskMock);
			verifyNoInteractions(camundaClientMock, failureHandlerMock, gsonMock);
		}
	}

}
