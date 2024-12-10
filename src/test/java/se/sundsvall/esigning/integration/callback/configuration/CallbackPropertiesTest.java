package se.sundsvall.esigning.integration.callback.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.esigning.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class CallbackPropertiesTest {

	@Autowired
	private CallbackProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.connectTimeout()).isEqualTo(1);
		assertThat(properties.readTimeout()).isEqualTo(2);
	}
}
