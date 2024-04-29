package se.sundsvall.esigning.businesslogic.worker;

import static se.sundsvall.esigning.Constants.DOCUMENT_METADATA_KEY_SIGNING_IN_PROGRESS;
import static se.sundsvall.esigning.integration.document.mapper.DocumentMapper.toDocumentUpdateRequest;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;
import se.sundsvall.esigning.integration.document.DocumentClient;

@Component
@ExternalTaskSubscription("RemoveOngoingSigningSignalTask")
public class RemoveOngoingSigningSignalWorker extends AbstractWorker {

	private final DocumentClient documentClient;

	RemoveOngoingSigningSignalWorker(CamundaClient camundaClient, FailureHandler failureHandler, Gson gson, DocumentClient documentClient) {
		super(camundaClient, failureHandler, gson);
		this.documentClient = documentClient;
	}

	@Override
	public void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		final var request = getSigningRequest(externalTask);
		try {
			logInfo("Removing metadata entry flagging document {} with registration number {} as part of an ongoing signing process", request.getFileName(), request.getRegistrationNumber());

			// Remove metadata about ongoing signing process
			final var metaData = documentClient.getDocument(request.getRegistrationNumber()).getMetadataList();
			metaData.removeIf(item -> item.getKey().equals(DOCUMENT_METADATA_KEY_SIGNING_IN_PROGRESS));
			documentClient.updateDocument(request.getRegistrationNumber(), toDocumentUpdateRequest(metaData));

			externalTaskService.complete(externalTask);
		} catch (final Exception exception) {
			logException(externalTask, exception);
			failureHandler.handleException(externalTaskService, externalTask, "%s occured for document %s with registration number %s when removing signal for ongoing signing process (%s).".formatted(
				exception.getClass().getSimpleName(),
				request.getFileName(),
				request.getRegistrationNumber(),
				exception.getMessage()));
		}
	}
}
