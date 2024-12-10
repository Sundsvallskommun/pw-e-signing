package se.sundsvall.esigning.businesslogic.worker;

import static se.sundsvall.esigning.businesslogic.util.UriUtility.addProcessIdParameter;

import com.google.gson.Gson;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.callback.CallbackClient;
import se.sundsvall.esigning.integration.camunda.CamundaClient;

@Component
@ExternalTaskSubscription("ExecuteCallbackTask")
public class ExecuteCallbackWorker extends AbstractWorker {

	private final CallbackClient callbackClient;

	ExecuteCallbackWorker(CamundaClient camundaClient, FailureHandler failureHandler, Gson gson, CallbackClient callbackClient) {
		super(camundaClient, failureHandler, gson);
		this.callbackClient = callbackClient;
	}

	@Override
	public void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		final var request = getSigningRequest(externalTask);

		try {
			final var uri = addProcessIdParameter(request.getCallbackUrl(), externalTask.getProcessInstanceId());
			logInfo("Executing callback to {} for document {} with registration number {}", uri, request.getFileName(), request.getRegistrationNumber());

			callbackClient.sendRequest(uri);

			externalTaskService.complete(externalTask);
		} catch (final Exception exception) {
			logException(externalTask, exception);
			failureHandler.handleException(externalTaskService, externalTask, "%s occured for document %s with registration number %s when callback to url %s was executed (%s).".formatted(
				exception.getClass().getSimpleName(),
				request.getFileName(),
				request.getRegistrationNumber(),
				request.getCallbackUrl(),
				exception.getMessage()));
		}
	}
}
