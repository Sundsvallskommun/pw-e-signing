package se.sundsvall.esigning.integration.document;

import static se.sundsvall.esigning.integration.document.configuration.DocumentConfiguration.CLIENT_ID;

import generated.se.sundsvall.document.Document;
import generated.se.sundsvall.document.DocumentDataCreateRequest;
import generated.se.sundsvall.document.DocumentUpdateRequest;
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

@FeignClient(name = CLIENT_ID, url = "${integration.document.url}", configuration = DocumentConfiguration.class)
public interface DocumentClient {

	@GetMapping(path = "/{municipalityId}/documents/{registrationNumber}")
	Document getDocument(@PathVariable("municipalityId") String municipalityId, @PathVariable("registrationNumber") String registrationNumber);

	@PatchMapping(path = "/{municipalityId}/documents/{registrationNumber}")
	Document updateDocument(@PathVariable("municipalityId") String municipalityId, @PathVariable("registrationNumber") String registrationNumber, DocumentUpdateRequest body);

	@GetMapping(path = "/{municipalityId}/documents/{registrationNumber}/files/{documentDataId}")
	ResponseEntity<ByteArrayResource> getDocumentData(@PathVariable("municipalityId") String municipalityId, @PathVariable("registrationNumber") String registrationNumber, @PathVariable("documentDataId") String documentDataId);

	@PutMapping(path = "/{municipalityId}/documents/{registrationNumber}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	Void addFileToDocument(@PathVariable("municipalityId") String municipalityId, @PathVariable("registrationNumber") String registrationNumber, @RequestPart("document") DocumentDataCreateRequest document,
		@RequestPart("documentFile") MultipartFile documentFile);
}
