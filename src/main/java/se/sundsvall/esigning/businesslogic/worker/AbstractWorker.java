package se.sundsvall.esigning.businesslogic.worker;

import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_ESIGNING_REQUEST;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_REQUEST_ID;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import generated.se.sundsvall.camunda.VariableValueDto;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;

abstract class AbstractWorker implements ExternalTaskHandler {

	private final Logger logger;
	private final CamundaClient camundaClient;
	private final Gson gson;
	protected final FailureHandler failureHandler;

	protected AbstractWorker(CamundaClient camundaClient, FailureHandler failureHandler, Gson gson) {
		this.logger = LoggerFactory.getLogger(getClass());
		this.camundaClient = camundaClient;
		this.failureHandler = failureHandler;
		this.gson = gson;
	}

	protected void setProcessInstanceVariable(ExternalTask externalTask, String variableName, VariableValueDto variableValue) {
		camundaClient.setProcessInstanceVariable(externalTask.getProcessInstanceId(), variableName, variableValue);
	}

	protected SigningRequest getSigningRequest(ExternalTask externalTask) {
		return fromJson(externalTask.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST), SigningRequest.class);
	}

	protected <T> T fromJson(String json, Class<T> clazz) {
		return gson.fromJson(json, clazz);
	}

	protected void logInfo(String msg, Object... arguments) {
		logger.info(msg, arguments);
	}

	protected void logException(ExternalTask externalTask, Exception exception) {
		logger.error("Exception occurred in {} for task with id {} and businesskey {}", this.getClass().getSimpleName(), externalTask.getId(), externalTask.getBusinessKey(), exception);
	}

	protected abstract void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService);

	@Override
	public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		RequestId.init(externalTask.getVariable(CAMUNDA_VARIABLE_REQUEST_ID));
		executeBusinessLogic(externalTask, externalTaskService);
	}
}
