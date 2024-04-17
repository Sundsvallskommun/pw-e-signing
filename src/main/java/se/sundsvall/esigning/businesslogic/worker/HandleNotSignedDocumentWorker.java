package se.sundsvall.esigning.businesslogic.worker;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_CALLBACK_PRESENT;

import java.util.Map;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;

@Component
@ExternalTaskSubscription("HandleNotSignedDocumentTask")
public class HandleNotSignedDocumentWorker extends AbstractWorker {

	HandleNotSignedDocumentWorker(CamundaClient camundaClient, FailureHandler failureHandler, Gson gson) {
		super(camundaClient, failureHandler, gson);
	}

	@Override
	public void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		final var request = getSigningRequest(externalTask);
		try {
			logInfo("Handling signing not completed for document {} with registration number {}", request.getFileName(), request.getRegistrationNumber());

			// TODO: Save expired signing status and errormessage on document instance via document service (UF-7785)

			externalTaskService.complete(externalTask, Map.of(CAMUNDA_VARIABLE_CALLBACK_PRESENT, isNotBlank(request.getCallbackUrl())));
		} catch (final Exception exception) {
			logException(externalTask, exception);
			failureHandler.handleException(externalTaskService, externalTask, "%s occured for document %s with registration number %s when handling expired document signing (%s).".formatted(
				exception.getClass().getSimpleName(),
				request.getFileName(),
				request.getRegistrationNumber(),
				exception.getMessage()));
		}
	}
}
