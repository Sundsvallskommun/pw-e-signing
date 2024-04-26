package se.sundsvall.esigning.integration.document.model;

import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import generated.se.sundsvall.comfactfacade.Document;
import wiremock.org.apache.commons.io.FileUtils;

@ExtendWith(MockitoExtension.class)
class DocumentMultipartFileTest {

	@Mock
	File fileMock;

	@ParameterizedTest
	@ValueSource(strings = "")
	@NullSource
	void fromDocumentWithoutContent(String content) throws Exception {

		final var name = "name";
		final var fileName = "fileName";
		final var mimeType = "mimeType";
		final var document = new Document()
			.name(name)
			.fileName(fileName)
			.mimeType(mimeType)
			.content(isNull(content) ? null : content.getBytes());

		final var multipartFile = DocumentMultipartFile.create(document);

		assertThat(multipartFile.getBytes()).isNullOrEmpty();
		assertThat(multipartFile.getContentType()).isEqualTo(mimeType);
		assertThat(multipartFile.getInputStream().readAllBytes()).isEmpty();
		assertThat(multipartFile.getName()).isEqualTo(name);
		assertThat(multipartFile.getOriginalFilename()).isEqualTo(fileName);
		assertThat(multipartFile.getResource().getContentAsByteArray()).isEmpty();
		assertThat(multipartFile.getSize()).isZero();
		assertThat(multipartFile.isEmpty()).isTrue();
	}

	@Test
	void fromDocumentWithContent() throws Exception {

		final var name = "name";
		final var fileName = "fileName";
		final var mimeType = "mimeType";
		final var content = "content".getBytes();
		final var document = new Document()
			.name(name)
			.fileName(fileName)
			.mimeType(mimeType)
			.content(content);

		final var multipartFile = DocumentMultipartFile.create(document);

		assertThat(multipartFile.getBytes()).isEqualTo(content);
		assertThat(multipartFile.getContentType()).isEqualTo(mimeType);
		assertThat(multipartFile.getInputStream().readAllBytes()).isEqualTo(content);
		assertThat(multipartFile.getName()).isEqualTo(name);
		assertThat(multipartFile.getOriginalFilename()).isEqualTo(fileName);
		assertThat(multipartFile.getResource().getContentAsByteArray()).isEqualTo(content);
		assertThat(multipartFile.getSize()).isEqualTo(content.length);
		assertThat(multipartFile.isEmpty()).isFalse();
	}

	@Test
	void transferToForDocumentWithContent() throws Exception {
		final var name = "name";
		final var fileName = "fileName";
		final var mimeType = "mimeType";
		final var content = "content".getBytes();
		final var document = new Document()
			.name(name)
			.fileName(fileName)
			.mimeType(mimeType)
			.content(content);
		final var multipartFile = DocumentMultipartFile.create(document);

		final var file = File.createTempFile("test_", null);
		multipartFile.transferTo(file);

		assertThat(file).exists();
		assertThat(FileUtils.readFileToByteArray(file)).isEqualTo(content);
	}

	@Test
	void transferToForDocumentWithoutContent() throws Exception {
		final var name = "name";
		final var fileName = "fileName";
		final var mimeType = "mimeType";
		final var document = new Document()
			.name(name)
			.fileName(fileName)
			.mimeType(mimeType);
		final var multipartFile = DocumentMultipartFile.create(document);

		multipartFile.transferTo(fileMock);

		verifyNoInteractions(fileMock);
	}
}
