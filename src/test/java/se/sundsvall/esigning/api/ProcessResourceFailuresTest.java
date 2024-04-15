package se.sundsvall.esigning.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import se.sundsvall.esigning.Application;
import se.sundsvall.esigning.api.model.Initiator;
import se.sundsvall.esigning.api.model.NotificationMessage;
import se.sundsvall.esigning.api.model.Signatory;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.service.ProcessService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ProcessResourceFailuresTest {

	@MockBean
	private ProcessService processServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void startProcessMissingBody() {

		// Act
		final var response = webTestClient.post().uri("/process/start")
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
			+ "se.sundsvall.esigning.api.ProcessResource.startProcess(se.sundsvall.esigning.api.model.SigningRequest)");

		verifyNoInteractions(processServiceMock);
	}

	@Test
	void startProcessEmptyBody() {
		// Act
		final var response = webTestClient.post().uri("/process/start")
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
			tuple("expires", "must not be null"),
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
		final var response = webTestClient.post().uri("/process/start")
			.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
			.bodyValue(new SigningRequest()
				.withInitiator(Initiator.create())
				.withNotificationMessage(NotificationMessage.create())
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
			tuple("expires", "must not be null"),
			tuple("fileName", "must not be blank"),
			tuple("initiator.email", "must not be null"),
			tuple("initiator.partyId", "not a valid UUID"),
			tuple("notificationMessage.body", "must not be blank"),
			tuple("notificationMessage.language", "The provided language is missing or not valid. Valid values are [sv, en, da, fr, de, nb, ru, zh, fi, uk]."),
			tuple("notificationMessage.subject", "must not be blank"),
			tuple("registrationNumber", "must not be blank"),
			tuple("signatories[0].email", "must not be null"),
			tuple("signatories[0].partyId", "not a valid UUID"));

		verifyNoInteractions(processServiceMock);
	}

	@Test
	void startProcessInvalidValues() {
		// Act
		final var response = webTestClient.post().uri("/process/start")
			.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
			.bodyValue(new SigningRequest()
				.withCallbackUrl("not_valid")
				.withFileName(" ")
				.withRegistrationNumber(" ")
				.withInitiator(Initiator.create()
					.withEmail("not_valid")
					.withLanguage("not_valid_language"))
				.withNotificationMessage(NotificationMessage.create()
					.withLanguage("not_valid"))
				.withSignatories(List.of(Signatory.create()
					.withEmail("not_valid")
					.withLanguage("not_valid")
					.withNotificationMessage(NotificationMessage.create()
						.withLanguage("not_valid")))))
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
			tuple("expires", "must not be null"),
			tuple("fileName", "must not be blank"),
			tuple("initiator.email", "must be a well-formed email address"),
			tuple("initiator.language", "The provided language is not valid. Valid values are [sv, en, da, fr, de, nb, ru, zh, fi, uk]."),
			tuple("initiator.partyId", "not a valid UUID"),
			tuple("notificationMessage.body", "must not be blank"),
			tuple("notificationMessage.language", "The provided language is missing or not valid. Valid values are [sv, en, da, fr, de, nb, ru, zh, fi, uk]."),
			tuple("notificationMessage.subject", "must not be blank"),
			tuple("registrationNumber", "must not be blank"),
			tuple("signatories[0].email", "must be a well-formed email address"),
			tuple("signatories[0].language", "The provided language is not valid. Valid values are [sv, en, da, fr, de, nb, ru, zh, fi, uk]."),
			tuple("signatories[0].notificationMessage.body", "must not be blank"),
			tuple("signatories[0].notificationMessage.language", "The provided language is missing or not valid. Valid values are [sv, en, da, fr, de, nb, ru, zh, fi, uk]."),
			tuple("signatories[0].notificationMessage.subject", "must not be blank"),
			tuple("signatories[0].partyId", "not a valid UUID"));

		verifyNoInteractions(processServiceMock);
	}
}
