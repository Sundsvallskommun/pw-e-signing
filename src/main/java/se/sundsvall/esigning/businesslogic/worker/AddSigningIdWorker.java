package se.sundsvall.esigning.businesslogic.worker;

import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_COMFACT_SIGNING_ID;
import static se.sundsvall.esigning.Constants.DOCUMENT_METADATA_KEY_SIGNING_ID;
import static se.sundsvall.esigning.integration.document.mapper.DocumentMapper.toDocumentMetadata;
import static se.sundsvall.esigning.integration.document.mapper.DocumentMapper.toDocumentUpdateRequest;

import java.util.List;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;
import se.sundsvall.esigning.integration.document.DocumentClient;

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
		try {
			logInfo("Add signingId as metadata on document {} with registration number {}", request.getFileName(), request.getRegistrationNumber());

			// Update document with signing id information
			documentClient.updateDocument(
				request.getRegistrationNumber(),
				toDocumentUpdateRequest(List.of(toDocumentMetadata(DOCUMENT_METADATA_KEY_SIGNING_ID, externalTask.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID)))));

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
