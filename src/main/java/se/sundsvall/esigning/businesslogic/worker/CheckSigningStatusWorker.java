package se.sundsvall.esigning.businesslogic.worker;

import com.google.gson.Gson;
import generated.se.sundsvall.comfactfacade.Status;
import java.util.Map;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;
import se.sundsvall.esigning.integration.comfactfacade.ComfactFacadeClient;

import static java.util.Optional.ofNullable;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_COMFACT_SIGNING_ID;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_COMFACT_SIGNING_STATUS;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_MUNICIPALITY_ID;

@Component
@ExternalTaskSubscription("CheckSigningStatusTask")
public class CheckSigningStatusWorker extends AbstractWorker {

	private final ComfactFacadeClient comfactFacadeClient;

	CheckSigningStatusWorker(CamundaClient camundaClient, FailureHandler failureHandler, Gson gson, ComfactFacadeClient comfactFacadeClient) {
		super(camundaClient, failureHandler, gson);
		this.comfactFacadeClient = comfactFacadeClient;
	}

	@Override
	public void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		final var request = getSigningRequest(externalTask);
		try {
			logInfo("Checking signing status for document {} with registration number {}", request.getFileName(), request.getRegistrationNumber());

			final var response = comfactFacadeClient.getSigningInstance(externalTask.getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID), externalTask.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID));
			String status = ofNullable(response.getStatus())
				.map(Status::getCode)
				.orElse("Notpresent");

			externalTaskService.complete(externalTask, Map.of(CAMUNDA_VARIABLE_COMFACT_SIGNING_STATUS, status));

		} catch (final Exception exception) {
			logException(externalTask, exception);
			failureHandler.handleException(externalTaskService, externalTask, "%s occured for document %s with registration number %s when signing status check was performed (%s).".formatted(
				exception.getClass().getSimpleName(),
				request.getFileName(),
				request.getRegistrationNumber(),
				exception.getMessage()));
		}
	}
}
