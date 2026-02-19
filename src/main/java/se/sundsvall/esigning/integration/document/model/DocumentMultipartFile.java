package se.sundsvall.esigning.integration.document.model;

import generated.se.sundsvall.comfactfacade.Document;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

import static java.io.InputStream.nullInputStream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public class DocumentMultipartFile implements MultipartFile {

	private final Document document;

	public static DocumentMultipartFile create(Document document) {
		requireNonNull(document, "Document must be provided");

		return new DocumentMultipartFile(document);
	}

	private DocumentMultipartFile(Document document) {
		this.document = document;
	}

	@Override
	public String getName() {
		return document.getName();
	}

	@Override
	public String getOriginalFilename() {
		return document.getFileName();
	}

	@Override
	public String getContentType() {
		return document.getMimeType();
	}

	@Override
	public boolean isEmpty() {
		return isNull(document.getContent()) || document.getContent().length == 0;
	}

	@Override
	public long getSize() {
		return isNull(document.getContent()) ? 0 : document.getContent().length;
	}

	@Override
	public byte[] getBytes() throws IOException {
		return document.getContent();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return isNull(document.getContent()) ? nullInputStream() : new ByteArrayInputStream(document.getContent());
	}

	@Override
	public void transferTo(File dest) throws IOException, IllegalStateException {
		if (nonNull(document.getContent())) {
			try (FileOutputStream fileOutputStream = new FileOutputStream(dest)) {
				fileOutputStream.write(document.getContent());
			}
		}
	}
}
