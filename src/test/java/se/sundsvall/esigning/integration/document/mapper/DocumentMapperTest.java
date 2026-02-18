package se.sundsvall.esigning.integration.document.mapper;

import generated.se.sundsvall.comfactfacade.Document;
import generated.se.sundsvall.comfactfacade.Signatory;
import java.util.List;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentMapperTest {

	@Test
	void toDocumentDataCreateRequest() {
		final var bean = DocumentMapper.toDocumentDataCreateRequest();
		assertThat(bean.getCreatedBy()).isEqualTo("E-signing-process");
	}

	@Test
	void toDocumentMetadataFromNull() {
		assertThat(DocumentMapper.toDocumentMetadata(null, null)).hasAllNullFieldsOrProperties();
	}

	@Test
	void toDocumentMetadata() {
		assertThat(DocumentMapper.toDocumentMetadata("key", "value")).satisfies(meta -> {
			assertThat(meta.getKey()).isEqualTo("key");
			assertThat(meta.getValue()).isEqualTo("value");
		});
	}

	@Test
	void toDocumentMetadatasFromNull() {
		assertThat(DocumentMapper.toDocumentMetadatas(null)).isEmpty();
	}

	@Test
	void toDocumentMetadatasFromEmptyList() {
		assertThat(DocumentMapper.toDocumentMetadatas(emptyList())).isEmpty();
	}

	@Test
	void toDocumentMetadatas() {
		final var signatories = List.of(
			createSignatory("name.a", "partyId.a", "email.a"),
			createSignatory("name.b", "partyId.b", "email.b"),
			createSignatory("name.c", "partyId.c", "email.c"));

		assertThat(DocumentMapper.toDocumentMetadatas(signatories)).hasSize(3).satisfiesExactlyInAnyOrder(
			meta -> {
				assertThat(meta.getKey()).isEqualTo("signatory.1");
				assertThat(meta.getValue()).isEqualTo("{\"name\":\"name.a\",\"partyId\":\"partyId.a\",\"email\":\"email.a\"}");
			}, meta -> {
				assertThat(meta.getKey()).isEqualTo("signatory.2");
				assertThat(meta.getValue()).isEqualTo("{\"name\":\"name.b\",\"partyId\":\"partyId.b\",\"email\":\"email.b\"}");
			}, meta -> {
				assertThat(meta.getKey()).isEqualTo("signatory.3");
				assertThat(meta.getValue()).isEqualTo("{\"name\":\"name.c\",\"partyId\":\"partyId.c\",\"email\":\"email.c\"}");
			});
	}

	@Test
	void toDocumentUpdateRequestFromNull() {
		assertThat(DocumentMapper.toDocumentUpdateRequest(null))
			.hasAllNullFieldsOrPropertiesExcept("createdBy", "metadataList")
			.hasFieldOrPropertyWithValue("createdBy", "E-signing-process");
	}

	@Test
	void toDocumentUpdateRequestFromEmptyList() {
		assertThat(DocumentMapper.toDocumentUpdateRequest(emptyList()))
			.hasAllNullFieldsOrPropertiesExcept("createdBy", "metadataList")
			.hasFieldOrPropertyWithValue("createdBy", "E-signing-process")
			.hasFieldOrPropertyWithValue("metadataList", emptyList());
	}

	@Test
	void toMultipartFileFromNull() {
		final var e = assertThrows((NullPointerException.class), () -> DocumentMapper.toMultipartFile(null));
		assertThat(e.getMessage()).isEqualTo("Document must be provided");
	}

	@Test
	void toMultipartFile() throws Exception {
		final var name = "name";
		final var fileName = "fileName";
		final var mimeType = "mimeType";
		final var content = "content".getBytes();
		final var document = new Document()
			.name(name)
			.fileName(fileName)
			.mimeType(mimeType)
			.content(content);

		final var multipartFile = DocumentMapper.toMultipartFile(document);

		assertThat(multipartFile.getBytes()).isEqualTo(content);
		assertThat(multipartFile.getContentType()).isEqualTo(mimeType);
		assertThat(multipartFile.getInputStream().readAllBytes()).isEqualTo(content);
		assertThat(multipartFile.getName()).isEqualTo(name);
		assertThat(multipartFile.getOriginalFilename()).isEqualTo(fileName);
		assertThat(multipartFile.getResource().getContentAsByteArray()).isEqualTo(content);
		assertThat(multipartFile.getSize()).isEqualTo(content.length);
		assertThat(multipartFile.isEmpty()).isFalse();
	}

	private static Signatory createSignatory(String name, String partyId, String email) {
		return new Signatory()
			.name(name)
			.partyId(partyId)
			.email(email);
	}
}
