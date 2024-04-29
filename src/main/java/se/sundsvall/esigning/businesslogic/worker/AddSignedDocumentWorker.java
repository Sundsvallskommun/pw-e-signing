package se.sundsvall.esigning.businesslogic.worker;

import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_COMFACT_SIGNING_ID;
import static se.sundsvall.esigning.integration.document.mapper.DocumentMapper.toDocumentDataCreateRequest;
import static se.sundsvall.esigning.integration.document.mapper.DocumentMapper.toMultipartFile;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;
import se.sundsvall.esigning.integration.comfactfacade.ComfactFacadeClient;
import se.sundsvall.esigning.integration.document.DocumentClient;

@Component
@ExternalTaskSubscription("AddSignedDocumentTask")
public class AddSignedDocumentWorker extends AbstractWorker {

	private final ComfactFacadeClient comfactFacadeClient;
	private final DocumentClient documentClient;

	AddSignedDocumentWorker(CamundaClient camundaClient, FailureHandler failureHandler, Gson gson, ComfactFacadeClient comfactFacadeClient, DocumentClient documentClient) {
		super(camundaClient, failureHandler, gson);
		this.comfactFacadeClient = comfactFacadeClient;
		this.documentClient = documentClient;
	}

	@Override
	public void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		final var request = getSigningRequest(externalTask);
		try {
			logInfo("Handling signed document {} with registration number {}", request.getFileName(), request.getRegistrationNumber());

			// Fetch signing instance
			final var response = comfactFacadeClient.getSigningInstance(externalTask.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID));

			// Create new revision of document with signed documentdata
			documentClient.addFileToDocument(request.getRegistrationNumber(),
				toDocumentDataCreateRequest(),
				toMultipartFile(response.getSignedDocument()));

			externalTaskService.complete(externalTask);
		} catch (final Exception exception) {
			logException(externalTask, exception);
			failureHandler.handleException(externalTaskService, externalTask, "%s occured for document %s with registration number %s when adding signed document (%s).".formatted(
				exception.getClass().getSimpleName(),
				request.getFileName(),
				request.getRegistrationNumber(),
				exception.getMessage()));
		}
	}
}
