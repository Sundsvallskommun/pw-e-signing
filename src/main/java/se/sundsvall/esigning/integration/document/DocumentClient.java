package se.sundsvall.esigning.integration.document;

import generated.se.sundsvall.document.Document;
import generated.se.sundsvall.document.DocumentDataCreateRequest;
import generated.se.sundsvall.document.DocumentUpdateRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.esigning.integration.document.configuration.DocumentConfiguration;

import static se.sundsvall.esigning.integration.document.configuration.DocumentConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.document.url}", configuration = DocumentConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface DocumentClient {

	@GetMapping(path = "/{municipalityId}/documents/{registrationNumber}")
	Document getDocument(@PathVariable String municipalityId, @PathVariable String registrationNumber);

	@PatchMapping(path = "/{municipalityId}/documents/{registrationNumber}")
	Document updateDocument(@PathVariable String municipalityId, @PathVariable String registrationNumber, DocumentUpdateRequest body);

	@GetMapping(path = "/{municipalityId}/documents/{registrationNumber}/files/{documentDataId}")
	ResponseEntity<ByteArrayResource> getDocumentData(@PathVariable String municipalityId, @PathVariable String registrationNumber, @PathVariable String documentDataId);

	@PutMapping(path = "/{municipalityId}/documents/{registrationNumber}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	Void addFileToDocument(@PathVariable String municipalityId, @PathVariable String registrationNumber, @RequestPart("document") DocumentDataCreateRequest document,
		@RequestPart("documentFile") MultipartFile documentFile);
}
