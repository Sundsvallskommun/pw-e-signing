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

class InitiatorTest {

	@Test
	void testBean() {
		assertThat(Initiator.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var email = "email";
		final var language = "language";
		final var name = "name";
		final var organization = "organization";
		final var partyId = "partyId";
		final var title = "title";

		final var bean = Initiator.create()
			.withEmail(email)
			.withLanguage(language)
			.withName(name)
			.withOrganization(organization)
			.withPartyId(partyId)
			.withTitle(title);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getEmail()).isEqualTo(email);
		assertThat(bean.getLanguage()).isEqualTo(language);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getOrganization()).isEqualTo(organization);
		assertThat(bean.getPartyId()).isEqualTo(partyId);
		assertThat(bean.getTitle()).isEqualTo(title);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new Initiator()).hasAllNullFieldsOrProperties();
		assertThat(Initiator.create()).hasAllNullFieldsOrProperties();
	}
}
