package se.sundsvall.esigning.businesslogic.worker;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;

@Component
@ExternalTaskSubscription("UpdateDocumentTask")
public class UpdateDocumentWorker extends AbstractWorker {

	UpdateDocumentWorker(CamundaClient camundaClient, FailureHandler failureHandler, Gson gson) {
		super(camundaClient, failureHandler, gson);
	}

	@Override
	public void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		final var request = getSigningRequest(externalTask);
		try {
			logInfo("Executing update of status for document {} with registration number {}", request.getFileName(), request.getRegistrationNumber());

			// TODO: Save metadata on document instance via document service (UF-7785)

			externalTaskService.complete(externalTask);
		} catch (final Exception exception) {
			logException(externalTask, exception);
			failureHandler.handleException(externalTaskService, externalTask, "%s occured for document %s with registration number %s when updating status (%s).".formatted(
				exception.getClass().getSimpleName(),
				request.getFileName(),
				request.getRegistrationNumber(),
				exception.getMessage()));
		}
	}
}
