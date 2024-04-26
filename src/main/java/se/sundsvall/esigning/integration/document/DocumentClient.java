package se.sundsvall.esigning.integration.document;

import static se.sundsvall.esigning.integration.document.configuration.DocumentConfiguration.CLIENT_ID;

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

import generated.se.sundsvall.document.Document;
import generated.se.sundsvall.document.DocumentUpdateRequest;
import se.sundsvall.esigning.integration.document.configuration.DocumentConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.document.url}", configuration = DocumentConfiguration.class)
public interface DocumentClient {

	@GetMapping(path = "documents/{registrationNumber}")
	Document getDocument(@PathVariable("registrationNumber") String registrationNumber);

	@PatchMapping(path = "documents/{registrationNumber}")
	Document updateDocument(@PathVariable("registrationNumber") String registrationNumber, DocumentUpdateRequest body);

	@GetMapping(path = "documents/{registrationNumber}/files/{documentDataId}")
	ResponseEntity<ByteArrayResource> getDocumentData(@PathVariable("registrationNumber") String registrationNumber, @PathVariable("documentDataId") String documentDataId);

	@PutMapping(path = "/documents/{registrationNumber}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	Void addFileToDocument(@PathVariable("registrationNumber") String registrationNumber, @RequestPart("document") String document, @RequestPart("documentFile") MultipartFile documentFile);
}
