package se.sundsvall.esigning.businesslogic.worker;

import static se.sundsvall.esigning.businesslogic.util.UriUtility.addProcessIdParameter;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.google.gson.Gson;

import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;

@Component
@ExternalTaskSubscription("ExecuteCallbackTask")
public class ExecuteCallbackWorker extends AbstractWorker {

	final RestClient restClient;

	ExecuteCallbackWorker(CamundaClient camundaClient, FailureHandler failureHandler, Gson gson, RestClient restClient) {
		super(camundaClient, failureHandler, gson);
		this.restClient = restClient;
	}

	@Override
	public void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		final var request = getSigningRequest(externalTask);

		try {
			final var uri = addProcessIdParameter(request.getCallbackUrl(), externalTask.getProcessInstanceId());
			logInfo("Executing callback to {} for document {} with registration number {}", uri, request.getFileName(), request.getRegistrationNumber());

			restClient.get()
				.uri(uri)
				.retrieve()
				.toBodilessEntity();

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
