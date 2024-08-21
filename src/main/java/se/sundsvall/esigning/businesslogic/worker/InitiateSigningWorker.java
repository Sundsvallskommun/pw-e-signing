package se.sundsvall.esigning.businesslogic.worker;

import com.google.gson.Gson;
import generated.se.sundsvall.document.Document;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;
import se.sundsvall.esigning.integration.comfactfacade.ComfactFacadeClient;
import se.sundsvall.esigning.integration.document.DocumentClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.apache.hc.core5.http.ContentType.APPLICATION_PDF;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_COMFACT_SIGNING_ID;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_MUNICIPALITY_ID;
import static se.sundsvall.esigning.integration.comfactfacade.mapper.ComfactFacadeMapper.toSigningRequest;

@Component
@ExternalTaskSubscription("InitiateSigningTask")
public class InitiateSigningWorker extends AbstractWorker {

	private final ComfactFacadeClient comfactFacadeClient;
	private final DocumentClient documentClient;

	InitiateSigningWorker(CamundaClient camundaClient, FailureHandler failureHandler, Gson gson, ComfactFacadeClient comfactFacadeClient, DocumentClient documentClient) {
		super(camundaClient, failureHandler, gson);
		this.comfactFacadeClient = comfactFacadeClient;
		this.documentClient = documentClient;
	}

	@Override
	public void executeBusinessLogic(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		final var request = getSigningRequest(externalTask);
		final String municipalityId = externalTask.getVariable(CAMUNDA_VARIABLE_MUNICIPALITY_ID);

		try {
			logInfo("Initiating signing of document {} with registration number {}", request.getFileName(), request.getRegistrationNumber());

			// Fetch file to sign
			final var documentData = getDocumentData(municipalityId, request.getRegistrationNumber(), request.getFileName());

			// Create signing instance
			final var signingId = comfactFacadeClient.createSigngingInstance(toSigningRequest(request, documentData.getBody(), APPLICATION_PDF.getMimeType())).getSigningId();

			externalTaskService.complete(externalTask, Map.of(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID, signingId));
		} catch (final Exception exception) {
			logException(externalTask, exception);
			failureHandler.handleException(externalTaskService, externalTask, "%s occured for document %s with registration number %s when initiating signing (%s).".formatted(
				exception.getClass().getSimpleName(),
				request.getFileName(),
				request.getRegistrationNumber(),
				exception.getMessage()));
		}
	}

	private ResponseEntity<ByteArrayResource> getDocumentData(String municipalityId, String registrationNumber, String fileName) {
		return Stream.of(documentClient.getDocument(municipalityId, registrationNumber))
			.map(Document::getDocumentData)
			.flatMap(List::stream)
			.filter(data -> data.getFileName().equals(fileName))
			.filter(data -> data.getMimeType().equals(APPLICATION_PDF.getMimeType()))
			.map(data -> documentClient.getDocumentData(municipalityId, registrationNumber, data.getId()))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "File %s of type %s was not found within document with registrationNumber %s".formatted(
				fileName,
				APPLICATION_PDF.getMimeType(),
				registrationNumber)));
	}
}
