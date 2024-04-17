package se.sundsvall.esigning.integration.comfactfacade.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;

import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.Test;

import generated.se.sundsvall.comfactfacade.Identification;
import se.sundsvall.esigning.api.model.Initiator;
import se.sundsvall.esigning.api.model.Message;
import se.sundsvall.esigning.api.model.Reminder;
import se.sundsvall.esigning.api.model.Signatory;
import se.sundsvall.esigning.api.model.SigningRequest;

class ComfactFacadeMapperTest {

	private static final OffsetDateTime EXPIRES = OffsetDateTime.now().plusDays(new Random().nextLong(1000));
	private static final String FILE_NAME = "fileName";
	private static final String INITIATOR_EMAIL = "initiatorEmail";
	private static final String INITIATOR_NAME = "initiatorName";
	private static final String INITIATOR_ORGANIZATION = "initiatorOrganization";
	private static final String INITIATOR_PARTY_ID = "initiatorPartyId";
	private static final String DOCUMENT_NAME = "documentName";
	private static final String NOTIFICATION_MESSAGE_BODY = "notificationMessageBody";
	private static final String NOTIFICATION_MESSAGE_SUBJECT = "notificationMessageSubject";
	private static final String REGISTRATION_NUMBER = "registrationNumber";
	private static final int REMINDER_INTERVAL_IN_HOURS = new Random().nextInt(1000);
	private static final String REMINDER_MESSAGE_BODY = "reminderMessageBody";
	private static final String REMINDER_MESSAGE_SUBJECT = "reminderMessageSubject";
	private static final OffsetDateTime REMINDER_START_DATE_TIME = OffsetDateTime.now().plusDays(new Random().nextLong(1000));
	private static final String SIGNATORY_EMAIL = "signatoryEmail";
	private static final String SIGNATORY_NAME = "signatoryName";
	private static final String SIGNATORY_ORGANIZATION = "signatoryOrganization";
	private static final String SIGNATORY_PARTY_ID = "signatoryPartyId";
	private static final String SIGNATORY_CUSTOM_NOTIFICATION_MESSAGE_BODY = "signatoryCustomNotificationMessageBody";
	private static final String SIGNATORY_CUSTOM_NOTIFICATION_MESSAGE_SUBJECT = "signatoryCustomNotificationMessageSubject";

	@Test
	void toSigningRequestWithDefinedLanguage() {
		final var language = "en-US";
		final var input = createInput(language);

		final var bean = ComfactFacadeMapper.toSigningRequest(input);
		assertResult(language, bean);
	}

	@Test
	void toSigningRequestWithDefaultLanguage() {
		final var input = createInput(null);

		final var bean = ComfactFacadeMapper.toSigningRequest(input);
		assertResult("se-SE", bean);

	}

	private static void assertResult(final String language, final generated.se.sundsvall.comfactfacade.SigningRequest bean) {
		assertThat(bean.getAdditionalDocuments()).isNull();
		assertThat(bean.getAdditionalParties()).isNull();
		assertThat(bean.getCustomerReference()).isEqualTo(REGISTRATION_NUMBER + " - " + FILE_NAME);
		assertThat(bean.getExpires()).isEqualTo(EXPIRES);
		assertThat(bean.getLanguage()).isEqualTo(language);

		assertThat(bean.getDocument()).isNotNull();
		assertThat(bean.getDocument().getContent()).isEqualTo(ComfactFacadeMapper.EXAMPLE_PDF.getBytes());
		assertThat(bean.getDocument().getFileName()).isEqualTo(FILE_NAME);
		assertThat(bean.getDocument().getMimeType()).isEqualTo(ContentType.APPLICATION_PDF.getMimeType());
		assertThat(bean.getDocument().getName()).isEqualTo(DOCUMENT_NAME);

		assertThat(bean.getInitiator()).isNotNull();
		assertThat(bean.getInitiator().getEmail()).isEqualTo(INITIATOR_EMAIL);
		assertThat(bean.getInitiator().getLanguage()).isEqualTo(language);
		assertThat(bean.getInitiator().getName()).isEqualTo(INITIATOR_NAME);
		assertThat(bean.getInitiator().getNotificationMessage()).isNull();
		assertThat(bean.getInitiator().getOrganization()).isEqualTo(INITIATOR_ORGANIZATION);
		assertThat(bean.getInitiator().getPartyId()).isEqualTo(INITIATOR_PARTY_ID);
		assertThat(bean.getInitiator().getPhoneNumber()).isNull();
		assertThat(bean.getInitiator().getTitle()).isNull();

		assertThat(bean.getNotificationMessage()).isNotNull();
		assertThat(bean.getNotificationMessage().getBody()).isEqualTo(NOTIFICATION_MESSAGE_BODY);
		assertThat(bean.getNotificationMessage().getLanguage()).isEqualTo(language);
		assertThat(bean.getNotificationMessage().getSubject()).isEqualTo(NOTIFICATION_MESSAGE_SUBJECT);

		assertThat(bean.getReminder()).isNotNull();
		assertThat(bean.getReminder().getEnabled()).isTrue();
		assertThat(bean.getReminder().getIntervalInHours()).isEqualTo(REMINDER_INTERVAL_IN_HOURS);
		assertThat(bean.getReminder().getStartDateTime()).isEqualTo(REMINDER_START_DATE_TIME);
		assertThat(bean.getReminder().getMessage()).isNotNull();
		assertThat(bean.getReminder().getMessage().getBody()).isEqualTo(REMINDER_MESSAGE_BODY);
		assertThat(bean.getReminder().getMessage().getLanguage()).isEqualTo(language);
		assertThat(bean.getReminder().getMessage().getSubject()).isEqualTo(REMINDER_MESSAGE_SUBJECT);

		assertThat(bean.getSignatories()).hasSize(1);
		assertThat(bean.getSignatories().getFirst().getEmail()).isEqualTo(SIGNATORY_EMAIL);
		assertThat(bean.getSignatories().getFirst().getLanguage()).isEqualTo(language);
		assertThat(bean.getSignatories().getFirst().getName()).isEqualTo(SIGNATORY_NAME);
		assertThat(bean.getSignatories().getFirst().getOrganization()).isEqualTo(SIGNATORY_ORGANIZATION);
		assertThat(bean.getSignatories().getFirst().getPartyId()).isEqualTo(SIGNATORY_PARTY_ID);
		assertThat(bean.getSignatories().getFirst().getPhoneNumber()).isNull();
		assertThat(bean.getSignatories().getFirst().getTitle()).isNull();
		assertThat(bean.getSignatories().getFirst().getIdentifications()).containsExactly(new Identification().alias("SvensktEId"));
		assertThat(bean.getSignatories().getFirst().getNotificationMessage()).isNotNull();
		assertThat(bean.getSignatories().getFirst().getNotificationMessage().getBody()).isEqualTo(SIGNATORY_CUSTOM_NOTIFICATION_MESSAGE_BODY);
		assertThat(bean.getSignatories().getFirst().getNotificationMessage().getLanguage()).isEqualTo(language);
		assertThat(bean.getSignatories().getFirst().getNotificationMessage().getSubject()).isEqualTo(SIGNATORY_CUSTOM_NOTIFICATION_MESSAGE_SUBJECT);
	}

	private static SigningRequest createInput(String language) {
		return SigningRequest.create()
			.withExpires(EXPIRES)
			.withFileName(FILE_NAME)
			.withInitiator(Initiator.create()
				.withEmail(INITIATOR_EMAIL)
				.withName(INITIATOR_NAME)
				.withOrganization(INITIATOR_ORGANIZATION)
				.withPartyId(INITIATOR_PARTY_ID))
			.withLanguage(language)
			.withName(DOCUMENT_NAME)
			.withNotificationMessage(Message.create()
				.withBody(NOTIFICATION_MESSAGE_BODY)
				.withSubject(NOTIFICATION_MESSAGE_SUBJECT))
			.withRegistrationNumber(REGISTRATION_NUMBER)
			.withReminder(Reminder.create()
				.withIntervalInHours(REMINDER_INTERVAL_IN_HOURS)
				.withReminderMessage(Message.create()
					.withBody(REMINDER_MESSAGE_BODY)
					.withSubject(REMINDER_MESSAGE_SUBJECT))
				.withStartDateTime(REMINDER_START_DATE_TIME))
			.withSignatories(List.of(Signatory.create()
				.withEmail(SIGNATORY_EMAIL)
				.withName(SIGNATORY_NAME)
				.withNotificationMessage(Message.create()
					.withBody(SIGNATORY_CUSTOM_NOTIFICATION_MESSAGE_BODY)
					.withSubject(SIGNATORY_CUSTOM_NOTIFICATION_MESSAGE_SUBJECT))
				.withOrganization(SIGNATORY_ORGANIZATION)
				.withPartyId(SIGNATORY_PARTY_ID)));
	}
}
