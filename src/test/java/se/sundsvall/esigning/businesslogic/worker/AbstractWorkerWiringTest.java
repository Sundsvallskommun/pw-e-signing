package se.sundsvall.esigning.businesslogic.worker;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.esigning.Application;
import se.sundsvall.esigning.integration.engine.externaltask.OperatonExternalTaskToken;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class AbstractWorkerWiringTest {

	@Autowired
	private List<AbstractWorker> workers;

	@Autowired
	private OperatonExternalTaskToken token;

	@Test
	void everyWorkerIsBuiltWithTheExternalTaskToken() {
		// Without the token a 401 on complete/handleFailure cannot be retried, see AbstractWorkerTest
		assertThat(workers)
			.isNotEmpty()
			.allSatisfy(worker -> assertThat(worker).extracting("token").isSameAs(token));
	}
}
