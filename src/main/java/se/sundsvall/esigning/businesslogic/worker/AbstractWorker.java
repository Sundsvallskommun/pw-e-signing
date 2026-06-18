package se.sundsvall.esigning.businesslogic.worker;

import com.google.gson.Gson;
import generated.se.sundsvall.camunda.VariableValueDto;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.engine.EngineClient;

import static se.sundsvall.esigning.Constants.PROCESS_VARIABLE_E_SIGNING_REQUEST;
import static se.sundsvall.esigning.Constants.PROCESS_VARIABLE_REQUEST_ID;

abstract class AbstractWorker implements ExternalTaskHandler {

	private final Logger logger;
	private final EngineClient engineClient;
	private final Gson gson;
	protected final FailureHandler failureHandler;

	protected AbstractWorker(EngineClient engineClient, FailureHandler failureHandler, Gson gson) {
		this.logger = LoggerFactory.getLogger(getClass());
		this.engineClient = engineClient;
		this.failureHandler = failureHandler;
		this.gson = gson;
	}

	protected void setProcessInstanceVariable(ExternalTask externalTask, String variableName, VariableValueDto variableValue) {
		engineClient.setProcessInstanceVariable(externalTask.getProcessInstanceId(), variableName, variableValue);
	}

	protected SigningRequest getSigningRequest(ExternalTask externalTask) {
		return fromJson(externalTask.getVariable(PROCESS_VARIABLE_E_SIGNING_REQUEST), SigningRequest.class);
	}

	protected <T> T fromJson(String json, Class<T> clazz) {
		return gson.fromJson(json, clazz);
	}

	protected void logInfo(String msg, Object... arguments) {
		logger.info(msg, arguments);
	}

	protected void logException(ExternalTask externalTask, Exception exception) {
		logger.error("{} occurred in {} for task with id {} and businesskey {}",
			exception.getClass().getSimpleName(),
			this.getClass().getSimpleName(),
			externalTask.getId(),
			externalTask.getBusinessKey(), exception);
	}

	protected abstract void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService);

	@Override
	public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		RequestId.init(externalTask.getVariable(PROCESS_VARIABLE_REQUEST_ID));
		executeBusinessLogic(externalTask, externalTaskService);
	}
}
