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
		final var name = "name";
		final var organization = "organization";
		final var partyId = "partyId";

		final var bean = Initiator.create()
			.withEmail(email)
			.withName(name)
			.withOrganization(organization)
			.withPartyId(partyId);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getEmail()).isEqualTo(email);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getOrganization()).isEqualTo(organization);
		assertThat(bean.getPartyId()).isEqualTo(partyId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new Initiator()).hasAllNullFieldsOrProperties();
		assertThat(Initiator.create()).hasAllNullFieldsOrProperties();
	}
}
