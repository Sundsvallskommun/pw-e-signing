package se.sundsvall.esigning.integration.document.mapper;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static se.sundsvall.esigning.Constants.DOCUMENT_METADATA_KEY_SIGNATORY;
import static se.sundsvall.esigning.Constants.DOCUMENT_USER;

import com.google.gson.Gson;
import generated.se.sundsvall.comfactfacade.Document;
import generated.se.sundsvall.comfactfacade.Signatory;
import generated.se.sundsvall.document.DocumentDataCreateRequest;
import generated.se.sundsvall.document.DocumentMetadata;
import generated.se.sundsvall.document.DocumentUpdateRequest;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.esigning.integration.document.model.DocumentMultipartFile;

public final class DocumentMapper {
	private static final Gson GSON = new Gson();

	private DocumentMapper() {}

	public static DocumentUpdateRequest toDocumentUpdateRequest(List<DocumentMetadata> metaDatas) {
		return new DocumentUpdateRequest()
			.createdBy(DOCUMENT_USER)
			.metadataList(metaDatas);
	}

	public static DocumentMetadata toDocumentMetadata(String key, String value) {
		return new DocumentMetadata()
			.key(key)
			.value(value);
	}

	public static List<DocumentMetadata> toDocumentMetadatas(List<Signatory> signatories) {
		final var atomicInt = new AtomicInteger(1);
		return ofNullable(signatories).orElse(emptyList()).stream()
			.map(signatory -> toDocumentMetadata(DOCUMENT_METADATA_KEY_SIGNATORY + atomicInt.getAndIncrement(), GSON.toJson(toSignatoryMetaData(signatory))))
			.toList();
	}

	public static DocumentDataCreateRequest toDocumentDataCreateRequest() {
		return new DocumentDataCreateRequest(DOCUMENT_USER);
	}

	public static MultipartFile toMultipartFile(Document document) {
		return DocumentMultipartFile.create(document);
	}

	private static SignatoryMetaData toSignatoryMetaData(Signatory signatory) {
		return SignatoryMetaData.create()
			.withEmail(signatory.getEmail())
			.withName(signatory.getName())
			.withPartyId(signatory.getPartyId());
	}

	@SuppressWarnings("unused")
	private static class SignatoryMetaData {

		String name;
		String partyId;
		String email;

		public static SignatoryMetaData create() {
			return new SignatoryMetaData();
		}

		public SignatoryMetaData withName(String name) {
			this.name = name;
			return this;
		}

		public SignatoryMetaData withPartyId(String partyId) {
			this.partyId = partyId;
			return this;
		}

		public SignatoryMetaData withEmail(String email) {
			this.email = email;
			return this;
		}
	}
}
