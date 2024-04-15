package se.sundsvall.esigning.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

class NotificationMessageTest {

	@Test
	void testBean() {
		assertThat(NotificationMessage.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var body = "body";
		final var language = "language";
		final var subject = "subject";

		final var bean = NotificationMessage.create()
			.withBody(body)
			.withLanguage(language)
			.withSubject(subject);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getBody()).isEqualTo(body);
		assertThat(bean.getLanguage()).isEqualTo(language);
		assertThat(bean.getSubject()).isEqualTo(subject);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new NotificationMessage()).hasAllNullFieldsOrProperties();
		assertThat(NotificationMessage.create()).hasAllNullFieldsOrProperties();
	}
}
