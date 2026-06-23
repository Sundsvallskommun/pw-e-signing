package apptest;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.esigning.Application;
import se.sundsvall.esigning.integration.camunda.CamundaClient;

/**
 * Verifies the Operaton wiring introduced by the migration: with {@code process-engine.type=operaton}, the startup
 * auto-deployment is routed through {@code OperatonEngineClient -> OperatonClient} (authenticated via the WSO2 gateway
 * stub) and the conditional external-task auth interceptor bean is registered. The engine container is shared with the
 * Camunda flow tests since Operaton is API-compatible with Camunda 7. The full business flow itself is covered by
 * {@link CreateProcessIT}; this test focuses on the engine-selection wiring.
 */
@DirtiesContext
@WireMockAppTestSuite(files = "classpath:/CreateProcessOperaton/", classes = Application.class)
class CreateProcessOperatonIT extends AbstractCamundaAppTest {
	private static final int DEFAULT_TESTCASE_TIMEOUT_IN_SECONDS = 30;
	private static final String PROCESSMODEL_FILE = "process-e-signing.bpmn";

	@Autowired
	private CamundaClient camundaClient;

	@Autowired
	private List<ClientRequestInterceptor> externalTaskInterceptors;

	@DynamicPropertySource
	static void operatonEngine(DynamicPropertyRegistry registry) {
		registry.add("process-engine.type", () -> "operaton");
	}

	@Test
	void test001_deploysViaOperatonAndRegistersExternalTaskAuth() {

		// The external-task auth interceptor bean is only wired when this instance targets Operaton.
		assertThat(externalTaskInterceptors)
			.anyMatch(interceptor -> interceptor.getClass().getSimpleName().equals("OperatonExternalTaskAuthInterceptor"));

		// Startup deployment ran through OperatonEngineClient -> OperatonClient (authenticated via the gateway token stub).
		await()
			.ignoreExceptions()
			.atMost(DEFAULT_TESTCASE_TIMEOUT_IN_SECONDS, SECONDS)
			.until(() -> camundaClient.getDeployments(PROCESSMODEL_FILE, null, null).size(), equalTo(1));
	}
}
