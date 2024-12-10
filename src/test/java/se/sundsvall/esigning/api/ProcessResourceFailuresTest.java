package se.sundsvall.esigning.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.esigning.Application;
import se.sundsvall.esigning.api.model.Initiator;
import se.sundsvall.esigning.api.model.Message;
import se.sundsvall.esigning.api.model.Reminder;
import se.sundsvall.esigning.api.model.Signatory;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.service.ProcessService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ProcessResourceFailuresTest {

	private static final String PATH = "/2281/process/start";

	@MockitoBean
	private ProcessService processServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void startProcessMissingBody() {

		// Act
		final var response = webTestClient.post().uri(PATH)
			.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
			.exchange()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Bad Request");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getDetail()).isEqualTo("Required request body is missing: public org.springframework.http.ResponseEntity<se.sundsvall.esigning.api.model.StartResponse> "
			+ "se.sundsvall.esigning.api.ProcessResource.startProcess(java.lang.String,se.sundsvall.esigning.api.model.SigningRequest)");

		verifyNoInteractions(processServiceMock);
	}

	@Test
	void startProcessEmptyBody() {
		// Act
		final var response = webTestClient.post().uri(PATH)
			.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
			.bodyValue(new SigningRequest())
			.exchange()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactlyInAnyOrder(
			tuple("fileName", "must not be blank"),
			tuple("initiator", "must not be null"),
			tuple("notificationMessage", "must not be null"),
			tuple("registrationNumber", "must not be blank"),
			tuple("signatories", "must not be empty"));

		verifyNoInteractions(processServiceMock);
	}

	@Test
	void startProcessEmptyInitiatorSignatorAndNotificationMessage() {
		// Act
		final var response = webTestClient.post().uri(PATH)
			.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
			.bodyValue(new SigningRequest()
				.withInitiator(Initiator.create())
				.withNotificationMessage(Message.create())
				.withSignatories(List.of(Signatory.create())))
			.exchange()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactlyInAnyOrder(
			tuple("fileName", "must not be blank"),
			tuple("initiator.email", "must not be null"),
			tuple("initiator.partyId", "not a valid UUID"),
			tuple("notificationMessage.body", "must not be blank"),
			tuple("notificationMessage.subject", "must not be blank"),
			tuple("registrationNumber", "must not be blank"),
			tuple("signatories[0].email", "must not be null"),
			tuple("signatories[0].partyId", "not a valid UUID"));

		verifyNoInteractions(processServiceMock);
	}

	@Test
	void startProcessInvalidValues() {
		// Act
		final var response = webTestClient.post().uri(PATH)
			.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
			.bodyValue(new SigningRequest()
				.withCallbackUrl("not_valid")
				.withExpires(OffsetDateTime.now().minusSeconds(1))
				.withFileName(" ")
				.withRegistrationNumber(" ")
				.withLanguage("not_valid")
				.withInitiator(Initiator.create()
					.withEmail("not_valid"))
				.withReminder(Reminder.create()
					.withIntervalInHours(0)
					.withReminderMessage(Message.create())
					.withStartDateTime(OffsetDateTime.now().minusSeconds(1)))
				.withNotificationMessage(Message.create())
				.withSignatories(List.of(Signatory.create()
					.withEmail("not_valid")
					.withNotificationMessage(Message.create()))))
			.exchange()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactlyInAnyOrder(
			tuple("callbackUrl", "must be a valid URL"),
			tuple("expires", "must be a future date"),
			tuple("fileName", "must not be blank"),
			tuple("language", "The provided language is not valid. Valid values are [en-US, sv-SE, da-DK, fr-FR, de-DE, nb-NO, ru-RU, zh-CN, fi-FI, uk-UA]."),
			tuple("initiator.email", "must be a well-formed email address"),
			tuple("initiator.partyId", "not a valid UUID"),
			tuple("notificationMessage.body", "must not be blank"),
			tuple("notificationMessage.subject", "must not be blank"),
			tuple("registrationNumber", "must not be blank"),
			tuple("reminder.intervalInHours", "must be greater than or equal to 1"),
			tuple("reminder.startDateTime", "must be a future date"),
			tuple("reminder.reminderMessage.subject", "must not be blank"),
			tuple("reminder.reminderMessage.body", "must not be blank"),
			tuple("signatories[0].email", "must be a well-formed email address"),
			tuple("signatories[0].notificationMessage.body", "must not be blank"),
			tuple("signatories[0].notificationMessage.subject", "must not be blank"),
			tuple("signatories[0].partyId", "not a valid UUID"));

		verifyNoInteractions(processServiceMock);
	}

	@Test
	void startProcessInvalidMunicipalityId() {
		// Act
		final var response = webTestClient.post().uri("/not-valid/process/start")
			.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
			.bodyValue(SigningRequest.create()
				.withFileName("filename")
				.withLanguage("en-US")
				.withInitiator(Initiator.create()
					.withEmail("valid.email@host.com")
					.withPartyId(UUID.randomUUID().toString()))
				.withNotificationMessage(Message.create()
					.withBody("body")
					.withSubject("subject"))
				.withRegistrationNumber("registrationNumber")
				.withReminder(Reminder.create()
					.withIntervalInHours(24)
					.withReminderMessage(Message.create()
						.withBody("body")
						.withSubject("subject"))
					.withStartDateTime(OffsetDateTime.now().plusDays(15)))
				.withSignatories(List.of(Signatory.create()
					.withEmail("valid.email@host.com")
					.withPartyId(UUID.randomUUID().toString()))))
			.exchange()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactlyInAnyOrder(
			tuple("startProcess.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(processServiceMock);
	}
}
