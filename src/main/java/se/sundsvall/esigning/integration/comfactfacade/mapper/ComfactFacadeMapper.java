package se.sundsvall.esigning.integration.comfactfacade.mapper;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Objects;

import org.springframework.core.io.ByteArrayResource;

import generated.se.sundsvall.comfactfacade.Document;
import generated.se.sundsvall.comfactfacade.Identification;
import generated.se.sundsvall.comfactfacade.NotificationMessage;
import generated.se.sundsvall.comfactfacade.Party;
import generated.se.sundsvall.comfactfacade.Reminder;
import generated.se.sundsvall.comfactfacade.Signatory;
import generated.se.sundsvall.comfactfacade.SigningRequest;
import se.sundsvall.esigning.api.model.Initiator;
import se.sundsvall.esigning.api.model.Message;

public final class ComfactFacadeMapper {
	private ComfactFacadeMapper() {}

	private static final String DEFAULT_LANGUAGE = "sv-SE";
	private static final String E_IDENTIFICATION = "SvensktEId";

	public static SigningRequest toSigningRequest(se.sundsvall.esigning.api.model.SigningRequest request, ByteArrayResource documentData, String contentType) {
		final var language = request.getLanguage();
		final var signingRequest = new SigningRequest()
			.customerReference(request.getRegistrationNumber() + " - " + request.getFileName())
			.document(toDocument(request, documentData, contentType))
			.expires(request.getExpires())
			.language(ofNullable(language).orElse(DEFAULT_LANGUAGE))
			.initiator(toParty(request.getInitiator(), language))
			.signatories(toSignatories(request.getSignatories(), language))
			.notificationMessage(toNotificationMessage(request.getNotificationMessage(), language))
			.additionalDocuments(emptyList())
			.additionalParties(emptyList());

		ofNullable(request.getReminder()).ifPresent(reminder -> signingRequest.reminder(toReminder(reminder, language)));

		return signingRequest;
	}

	private static Party toParty(Initiator initiator, String language) {
		return ofNullable(initiator)
			.map(i -> new Party()
				.email(i.getEmail())
				.language(ofNullable(language).orElse(DEFAULT_LANGUAGE))
				.name(i.getName())
				.organization(i.getOrganization())
				.partyId(i.getPartyId()))
			.orElse(null);
	}

	private static NotificationMessage toNotificationMessage(Message notificationMessage, String language) {
		return ofNullable(notificationMessage)
			.map(n -> new NotificationMessage()
				.body(n.getBody())
				.language(ofNullable(language).orElse(DEFAULT_LANGUAGE))
				.subject(n.getSubject()))
			.orElse(null);
	}

	private static Reminder toReminder(se.sundsvall.esigning.api.model.Reminder reminder, String language) {
		return new Reminder()
			.enabled(true)
			.intervalInHours(reminder.getIntervalInHours())
			.message(toNotificationMessage(reminder.getReminderMessage(), language))
			.startDateTime(reminder.getStartDateTime());
	}

	private static Document toDocument(se.sundsvall.esigning.api.model.SigningRequest request, ByteArrayResource documentData, String contentType) {
		return new Document()
			.content(documentData.getByteArray())
			.fileName(request.getFileName())
			.name(request.getName())
			.mimeType(contentType);
	}

	private static List<Signatory> toSignatories(List<se.sundsvall.esigning.api.model.Signatory> signatories, String language) {
		return ofNullable(signatories).orElse(emptyList()).stream()
			.filter(Objects::nonNull)
			.map(signatory -> toSignatory(signatory, language))
			.toList();
	}

	private static Signatory toSignatory(se.sundsvall.esigning.api.model.Signatory signatory, String language) {
		return new Signatory()
			.email(signatory.getEmail())
			.language(ofNullable(language).orElse(DEFAULT_LANGUAGE))
			.name(signatory.getName())
			.organization(signatory.getOrganization())
			.partyId(signatory.getPartyId())
			.notificationMessage(toNotificationMessage(signatory.getNotificationMessage(), language))
			.addIdentificationsItem(new Identification().alias(E_IDENTIFICATION));
	}

}
