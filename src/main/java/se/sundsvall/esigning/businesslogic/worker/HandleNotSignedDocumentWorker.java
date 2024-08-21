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

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_CALLBACK_PRESENT;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_COMFACT_SIGNING_ID;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_MUNICIPALITY_ID;
import static se.sundsvall.esigning.Constants.DOCUMENT_METADATA_KEY_SIGNING_STATUS;
import static se.sundsvall.esigning.Constants.DOCUMENT_METADATA_KEY_SIGNING_STATUS_MESSAGE;
import static se.sundsvall.esigning.integration.document.mapper.DocumentMapper.toDocumentMetadata;
import static se.sundsvall.esigning.integration.document.mapper.DocumentMapper.toDocumentUpdateRequest;

@Component
@ExternalTaskSubscription("HandleNotSignedDocumentTask")
public class HandleNotSignedDocumentWorker extends AbstractWorker {

	private final ComfactFacadeClient comfactFacadeClient;
	private final DocumentClient documentClient;

	HandleNotSignedDocumentWorker(CamundaClient camundaClient, FailureHandler failureHandler, Gson gson, ComfactFacadeClient comfactFacadeClient, DocumentClient documentClient) {
		super(camundaClient, failureHandler, gson);
		this.comfactFacadeClient = comfactFacadeClient;
		this.documentClient = documentClient;
	}

	@Override
	public void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		final var request = getSigningRequest(externalTask);
		final String municipalityId = externalTask.getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID);
		try {
			logInfo("Handling signing not completed for document {} with registration number {}", request.getFileName(), request.getRegistrationNumber());

			// Fetch signing instance
			final var response = comfactFacadeClient.getSigningInstance(externalTask.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID));

			// Save expired signing status and errormessage on document instance
			final var metaData = documentClient.getDocument(municipalityId, request.getRegistrationNumber()).getMetadataList();
			metaData.add(toDocumentMetadata(DOCUMENT_METADATA_KEY_SIGNING_STATUS, response.getStatus().getCode()));
			metaData.add(toDocumentMetadata(DOCUMENT_METADATA_KEY_SIGNING_STATUS_MESSAGE, response.getStatus().getMessage()));
			documentClient.updateDocument(municipalityId, request.getRegistrationNumber(), toDocumentUpdateRequest(metaData));

			externalTaskService.complete(externalTask, Map.of(CAMUNDA_VARIABLE_CALLBACK_PRESENT, isNotBlank(request.getCallbackUrl())));
		} catch (final Exception exception) {
			logException(externalTask, exception);
			failureHandler.handleException(externalTaskService, externalTask, "%s occured for document %s with registration number %s when handling not signed document (%s).".formatted(
				exception.getClass().getSimpleName(),
				request.getFileName(),
				request.getRegistrationNumber(),
				exception.getMessage()));
		}
	}
}
