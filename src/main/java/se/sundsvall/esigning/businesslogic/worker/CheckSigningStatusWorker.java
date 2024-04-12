package se.sundsvall.esigning.businesslogic.worker;

import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_SIGN_STATUS;

import java.time.OffsetDateTime;
import java.util.Map;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;

@Component
@ExternalTaskSubscription("CheckSigningStatusTask")
public class CheckSigningStatusWorker extends AbstractWorker {

	CheckSigningStatusWorker(CamundaClient camundaClient, FailureHandler failureHandler, Gson gson) {
		super(camundaClient, failureHandler, gson);
	}

	@Override
	public void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		try {
			final var request = getSigningRequest(externalTask);
			logInfo("Checking signing status for document {} with registration number {}", request.getFileName(), request.getRegistrationNumber());

			String status;
			if (OffsetDateTime.now().isAfter(request.getExpires())) {
				status = "EXPIRED";
			} else {
				status = request.getRegistrationNumber().endsWith("COMPLETED") ? "COMPLETED" : "ACTIVE";
			}

			// TODO: Replace code above with logic to check signing status via comfact service (UF-7786)

			externalTaskService.complete(externalTask, Map.of(CAMUNDA_VARIABLE_SIGN_STATUS, status));

		} catch (final Exception exception) {
			logException(externalTask, exception);
			failureHandler.handleException(externalTaskService, externalTask, exception.getMessage());
		}
	}
}
