package se.sundsvall.esigning.businesslogic.worker;

import com.google.gson.Gson;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;
import se.sundsvall.esigning.integration.comfactfacade.ComfactFacadeClient;
import se.sundsvall.esigning.integration.document.DocumentClient;

import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_COMFACT_SIGNING_ID;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_MUNICIPALITY_ID;
import static se.sundsvall.esigning.integration.document.mapper.DocumentMapper.toDocumentDataCreateRequest;
import static se.sundsvall.esigning.integration.document.mapper.DocumentMapper.toMultipartFile;

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
		final String municipalityId = externalTask.getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID);

		try {
			logInfo("Handling signed document {} with registration number {}", request.getFileName(), request.getRegistrationNumber());

			// Fetch signing instance
			final var response = comfactFacadeClient.getSigningInstance(externalTask.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID));

			// Create new revision of document with signed documentdata
			documentClient.addFileToDocument(municipalityId, request.getRegistrationNumber(),
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
