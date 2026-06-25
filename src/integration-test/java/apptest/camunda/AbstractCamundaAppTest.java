package apptest.camunda;

import apptest.AbstractEngineAppTest;
import apptest.engine.EngineTestProperties;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base for the Camunda-engine integration tests: points every test in this package at a live Camunda 7 engine
 * ({@code process-engine.type=camunda}). {@code @DirtiesContext} so each class tears down its context (and its external
 * task client) afterwards, keeping the shared engine container clean for the next class. This whole package is removed
 * once the migration to Operaton completes.
 */
@DirtiesContext
abstract class AbstractCamundaAppTest extends AbstractEngineAppTest {

	@DynamicPropertySource
	static void engine(DynamicPropertyRegistry registry) {
		EngineTestProperties.registerCamunda(registry);
	}
}
