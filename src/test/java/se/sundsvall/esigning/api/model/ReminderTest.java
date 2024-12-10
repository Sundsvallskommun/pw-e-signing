package se.sundsvall.esigning.api.model;

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

import java.time.OffsetDateTime;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ReminderTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		assertThat(Reminder.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var intervalInHours = 48;
		final var reminderMessage = Message.create();
		final var startDateTime = OffsetDateTime.now();

		final var bean = Reminder.create()
			.withIntervalInHours(intervalInHours)
			.withReminderMessage(reminderMessage)
			.withStartDateTime(startDateTime);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getIntervalInHours()).isEqualTo(intervalInHours);
		assertThat(bean.getReminderMessage()).isEqualTo(reminderMessage);
		assertThat(bean.getStartDateTime()).isEqualTo(startDateTime);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new Reminder()).hasAllNullFieldsOrPropertiesExcept("intervalInHours").hasFieldOrPropertyWithValue("intervalInHours", 0);
		assertThat(Reminder.create()).hasAllNullFieldsOrPropertiesExcept("intervalInHours").hasFieldOrPropertyWithValue("intervalInHours", 0);
	}
}
