package se.sundsvall.esigning.api;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.esigning.Application;
import se.sundsvall.esigning.api.model.Initiator;
import se.sundsvall.esigning.api.model.Message;
import se.sundsvall.esigning.api.model.Reminder;
import se.sundsvall.esigning.api.model.Signatory;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.api.model.StartResponse;
import se.sundsvall.esigning.service.ProcessService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ProcessResourceTest {

	@MockBean
	private ProcessService processServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@MethodSource("startProcessArgumentProvider")
	void startProcess(SigningRequest request) {

		// Arrange
		final var uuid = randomUUID().toString();
		
		when(processServiceMock.startProcess(any())).thenReturn(uuid);

		// Act
		final var response = webTestClient.post().uri("/process/start")
			.bodyValue(request)
			.exchange()
			.expectStatus().isAccepted()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(StartResponse.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response.getProcessId()).isEqualTo(uuid);
		verify(processServiceMock).startProcess(request);
		verifyNoMoreInteractions(processServiceMock);
	}

	private static Stream<Arguments> startProcessArgumentProvider() {
		return Stream.of(
			Arguments.of(
				SigningRequest.create()
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
						.withPartyId(UUID.randomUUID().toString())))),
			Arguments.of(SigningRequest.create()
				.withCallbackUrl("http://valid.url?param1=value1&param2=value2")
				.withExpires(OffsetDateTime.now().plusDays(1))
				.withFileName("filename")
				.withInitiator(Initiator.create()
					.withEmail("valid.email@host.com")
					.withPartyId(UUID.randomUUID().toString()))
				.withNotificationMessage(Message.create()
					.withBody("body")
					.withSubject("subject"))
				.withRegistrationNumber("registrationNumber")
				.withSignatories(List.of(Signatory.create()
					.withEmail("valid.email@host.com")
					.withNotificationMessage(Message.create()
						.withBody("body")
						.withSubject("subject"))
					.withPartyId(UUID.randomUUID().toString())))));
	}
}
