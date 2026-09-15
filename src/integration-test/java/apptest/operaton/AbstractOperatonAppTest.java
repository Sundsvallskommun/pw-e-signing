package apptest.operaton;

import apptest.AbstractEngineAppTest;
import apptest.engine.EngineTestProperties;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base for the Operaton-engine integration tests: points every test in this package at a live Operaton engine
 * ({@code process-engine.type=operaton}), so the worker write-path executes against the Operaton REST API.
 * {@code @DirtiesContext} so each class tears down its context (and its external task client) afterwards, keeping the
 * shared engine container clean for the next class.
 */
@DirtiesContext
abstract class AbstractOperatonAppTest extends AbstractEngineAppTest {

	@DynamicPropertySource
	static void engine(DynamicPropertyRegistry registry) {
		EngineTestProperties.registerOperaton(registry);
	}
}
