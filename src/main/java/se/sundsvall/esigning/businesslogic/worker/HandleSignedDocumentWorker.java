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
@ExternalTaskSubscription("HandleSignedDocumentTask")
public class HandleSignedDocumentWorker extends AbstractWorker {

	HandleSignedDocumentWorker(CamundaClient camundaClient, FailureHandler failureHandler, Gson gson) {
		super(camundaClient, failureHandler, gson);
	}

	@Override
	public void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		try {
			final var request = getSigningRequest(externalTask);
			logInfo("Handling signed document {} with registration number {}", request.getFileName(), request.getRegistrationNumber());

			// TODO: Save signed document instance and signed status via document service (UF-7785)

			externalTaskService.complete(externalTask, Map.of(CAMUNDA_VARIABLE_CALLBACK_PRESENT, isNotBlank(request.getCallbackUrl())));
		} catch (final Exception exception) {
			logException(externalTask, exception);
			failureHandler.handleException(externalTaskService, externalTask, exception.getMessage());
		}
	}
}
