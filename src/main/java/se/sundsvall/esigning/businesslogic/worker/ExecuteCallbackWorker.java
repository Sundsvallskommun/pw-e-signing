package se.sundsvall.esigning.businesslogic.worker;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;

@Component
@ExternalTaskSubscription("ExecuteCallbackTask")
public class ExecuteCallbackWorker extends AbstractWorker {

	ExecuteCallbackWorker(CamundaClient camundaClient, FailureHandler failureHandler, Gson gson) {
		super(camundaClient, failureHandler, gson);
	}

	@Override
	public void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		try {
			final var request = getSigningRequest(externalTask);
			logInfo("Executing callback to {} for document {} with registration number {}", request.getCallbackUrl(), request.getFileName(), request.getRegistrationNumber());

			// TODO: Execute http call to callback url (UF-7787)

			externalTaskService.complete(externalTask);
		} catch (final Exception exception) {
			logException(externalTask, exception);
			failureHandler.handleException(externalTaskService, externalTask, exception.getMessage());
		}
	}
}
