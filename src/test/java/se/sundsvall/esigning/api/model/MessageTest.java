package se.sundsvall.esigning.api.model;

import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

class MessageTest {

	@Test
	void testBean() {
		assertThat(Message.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var body = "body";
		final var subject = "subject";

		final var bean = Message.create()
			.withBody(body)
			.withSubject(subject);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getBody()).isEqualTo(body);
		assertThat(bean.getSubject()).isEqualTo(subject);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new Message()).hasAllNullFieldsOrProperties();
		assertThat(Message.create()).hasAllNullFieldsOrProperties();
	}
}
