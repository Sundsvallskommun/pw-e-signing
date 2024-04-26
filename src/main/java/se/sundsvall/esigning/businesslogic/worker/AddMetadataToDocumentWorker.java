package se.sundsvall.esigning.businesslogic.worker;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_CALLBACK_PRESENT;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_COMFACT_SIGNING_ID;
import static se.sundsvall.esigning.integration.document.mapper.DocumentMapper.toDocumentMetadatas;
import static se.sundsvall.esigning.integration.document.mapper.DocumentMapper.toDocumentUpdateRequest;

import java.util.Map;

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
@ExternalTaskSubscription("AddMetadataToDocumentTask")
public class AddMetadataToDocumentWorker extends AbstractWorker {

	private final ComfactFacadeClient comfactFacadeClient;
	private final DocumentClient documentClient;

	AddMetadataToDocumentWorker(CamundaClient camundaClient, FailureHandler failureHandler, Gson gson, ComfactFacadeClient comfactFacadeClient, DocumentClient documentClient) {
		super(camundaClient, failureHandler, gson);
		this.comfactFacadeClient = comfactFacadeClient;
		this.documentClient = documentClient;
	}

	@Override
	public void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		final var request = getSigningRequest(externalTask);
		try {
			logInfo("Adding metadata regarding signatories for document {} with registration number {}", request.getFileName(), request.getRegistrationNumber());

			// Fetch signing instance
			final var response = comfactFacadeClient.getSigningInstance(externalTask.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID));

			// Save signatory information as metadata on document
			final var metaData = documentClient.getDocument(request.getRegistrationNumber()).getMetadataList();
			metaData.addAll(toDocumentMetadatas(response.getSignatories()));
			documentClient.updateDocument(request.getRegistrationNumber(), toDocumentUpdateRequest(metaData));

			externalTaskService.complete(externalTask, Map.of(CAMUNDA_VARIABLE_CALLBACK_PRESENT, isNotBlank(request.getCallbackUrl())));
		} catch (final Exception exception) {
			logException(externalTask, exception);
			failureHandler.handleException(externalTaskService, externalTask, "%s occured for document %s with registration number %s when adding signatory metadata to document (%s).".formatted(
				exception.getClass().getSimpleName(),
				request.getFileName(),
				request.getRegistrationNumber(),
				exception.getMessage()));
		}
	}
}
