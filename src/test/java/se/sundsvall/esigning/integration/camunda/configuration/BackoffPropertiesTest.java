package se.sundsvall.esigning.integration.camunda.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.esigning.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class BackoffPropertiesTest {

	@Autowired
	private BackoffProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.initTime()).isEqualTo(500);
		assertThat(properties.factor()).isEqualTo(2);
		assertThat(properties.maxTime()).isEqualTo(15000);
	}
}
