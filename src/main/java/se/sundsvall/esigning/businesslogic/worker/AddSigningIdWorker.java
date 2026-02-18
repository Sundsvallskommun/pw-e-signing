package se.sundsvall.esigning.businesslogic.worker;

import com.google.gson.Gson;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;
import se.sundsvall.esigning.integration.document.DocumentClient;

import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_COMFACT_SIGNING_ID;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_MUNICIPALITY_ID;
import static se.sundsvall.esigning.Constants.DOCUMENT_METADATA_KEY_SIGNING_ID;
import static se.sundsvall.esigning.integration.document.mapper.DocumentMapper.toDocumentMetadata;
import static se.sundsvall.esigning.integration.document.mapper.DocumentMapper.toDocumentUpdateRequest;

@Component
@ExternalTaskSubscription("AddSigningIdTask")
public class AddSigningIdWorker extends AbstractWorker {

	private final DocumentClient documentClient;

	AddSigningIdWorker(CamundaClient camundaClient, FailureHandler failureHandler, Gson gson, DocumentClient documentClient) {
		super(camundaClient, failureHandler, gson);
		this.documentClient = documentClient;
	}

	@Override
	public void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		final var request = getSigningRequest(externalTask);
		final String municipalityId = externalTask.getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID);

		try {
			logInfo("Add signingId as metadata on document {} with registration number {}", request.getFileName(), request.getRegistrationNumber());

			// Read and update existing document metadata with signing id information
			final var metaData = documentClient.getDocument(municipalityId, request.getRegistrationNumber()).getMetadataList();
			metaData.add(toDocumentMetadata(DOCUMENT_METADATA_KEY_SIGNING_ID, externalTask.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID)));

			documentClient.updateDocument(
				municipalityId,
				request.getRegistrationNumber(),
				toDocumentUpdateRequest(metaData));

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
