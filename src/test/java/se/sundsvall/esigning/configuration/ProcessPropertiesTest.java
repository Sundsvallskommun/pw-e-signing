package se.sundsvall.esigning.configuration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.esigning.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class ProcessPropertiesTest {

	@Autowired
	private ProcessProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.waitDuration()).isEqualTo("R/PT10S");
	}
}
