package se.sundsvall.esigning.integration.engine.externaltask;

import org.camunda.bpm.client.spring.boot.starter.ClientProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.esigning.Application;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class ExternalTaskClientPropertiesTest {

	@Autowired
	private ClientProperties clientProperties;

	@Test
	void longPollingTimeoutIs25Seconds() {
		assertThat(clientProperties.getAsyncResponseTimeout()).isEqualTo(25000L);
	}
}
