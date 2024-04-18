package se.sundsvall.esigning.businesslogic.worker;

import static java.util.Objects.isNull;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_COMFACT_SIGNING_ID;
import static se.sundsvall.esigning.integration.camunda.mapper.CamundaMapper.toVariableValueDto;
import static se.sundsvall.esigning.integration.comfactfacade.mapper.ComfactFacadeMapper.toSigningRequest;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;
import se.sundsvall.esigning.integration.camunda.mapper.VariableFormat;
import se.sundsvall.esigning.integration.comfactfacade.ComfactFacadeClient;

@Component
@ExternalTaskSubscription("InitiateSigningTask")
public class InitiateSigningWorker extends AbstractWorker {

	private final ComfactFacadeClient comfactFacadeClient;

	InitiateSigningWorker(CamundaClient camundaClient, FailureHandler failureHandler, Gson gson, ComfactFacadeClient comfactFacadeClient) {
		super(camundaClient, failureHandler, gson);
		this.comfactFacadeClient = comfactFacadeClient;
	}

	@Override
	public void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		final var request = getSigningRequest(externalTask);
		try {
			logInfo("Initiating signing of document {} with registration number {}", request.getFileName(), request.getRegistrationNumber());

			// If process doesnt already have started a signing request (which might be the case if error occured when saving
			// metadata on document in earlier execution) then call facade to initialize signing
			if (isNull(externalTask.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID))) {
				final var response = comfactFacadeClient.createSigngingInstance(toSigningRequest(request));
				setProcessInstanceVariable(externalTask, CAMUNDA_VARIABLE_COMFACT_SIGNING_ID, toVariableValueDto(VariableFormat.STRING, String.class, response.getSigningId()));
			}

			// TODO: Save signingId as metadata on document instance when Documents service is in place

			externalTaskService.complete(externalTask);
		} catch (final Exception exception) {
			logException(externalTask, exception);
			failureHandler.handleException(externalTaskService, externalTask, "%s occured for document %s with registration number %s when initiating signing (%s).".formatted(
				exception.getClass().getSimpleName(),
				request.getFileName(),
				request.getRegistrationNumber(),
				exception.getMessage()));
		}
	}
}
