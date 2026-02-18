package se.sundsvall.esigning.api.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

class SigningRequestTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		assertThat(SigningRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var callbackUrl = "callbackUrl";
		final var exipres = OffsetDateTime.now();
		final var fileName = "fileName";
		final var initiator = Initiator.create();
		final var language = "language";
		final var name = "name";
		final var notificationMessage = Message.create();
		final var registrationNumber = "registrationNumber";
		final var reminder = Reminder.create();
		final var signatories = List.of(Signatory.create());

		final var bean = SigningRequest.create()
			.withCallbackUrl(callbackUrl)
			.withExpires(exipres)
			.withFileName(fileName)
			.withInitiator(initiator)
			.withLanguage(language)
			.withName(name)
			.withNotificationMessage(notificationMessage)
			.withRegistrationNumber(registrationNumber)
			.withReminder(reminder)
			.withSignatories(signatories);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCallbackUrl()).isEqualTo(callbackUrl);
		assertThat(bean.getExpires()).isEqualTo(exipres);
		assertThat(bean.getFileName()).isEqualTo(fileName);
		assertThat(bean.getInitiator()).isEqualTo(initiator);
		assertThat(bean.getLanguage()).isEqualTo(language);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getNotificationMessage()).isEqualTo(notificationMessage);
		assertThat(bean.getRegistrationNumber()).isEqualTo(registrationNumber);
		assertThat(bean.getReminder()).isEqualTo(reminder);
		assertThat(bean.getSignatories()).isEqualTo(signatories);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new SigningRequest()).hasAllNullFieldsOrProperties();
		assertThat(SigningRequest.create()).hasAllNullFieldsOrProperties();
	}
}
