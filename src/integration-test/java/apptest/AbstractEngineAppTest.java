package apptest;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.stream.Stream.concat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import generated.se.sundsvall.camunda.HistoricActivityInstanceDto;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.esigning.integration.camunda.CamundaClient;

/**
 * Engine-neutral base for the testcontainer-driven process tests. Holds the shared route helper; the per-engine base
 * classes (in the {@code apptest.camunda} and {@code apptest.operaton} packages) pick the engine by delegating their
 * {@code @DynamicPropertySource} to {@link apptest.engine.EngineTestProperties}. The {@code camundaClient} is used purely
 * as a read client for process history and works against either engine since Operaton is API-compatible with Camunda 7.
 * See Camunda API for more details https://docs.camunda.org/rest/camunda-bpm-platform/7.21/
 */
public abstract class AbstractEngineAppTest extends AbstractAppTest {

	private static final String TENANT_ID_E_SIGNING = "E_SIGNING";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

	@Autowired
	protected CamundaClient camundaClient;

	@Value("${integration.camunda.url}")
	private String engineBaseUrl;

	/**
	 * Deletes the tenant's process instances before each test, so state cannot leak between the test classes that share
	 * the same engine container (it is reused across {@code @DirtiesContext} contexts). Without this, an instance that
	 * outlived an earlier test - especially on the slower Operaton engine - gets picked up by this context's external
	 * task workers and corrupts this test's WireMock scenario state.
	 */
	@BeforeEach
	void purgeProcessInstances() throws Exception {
		final var listRequest = HttpRequest.newBuilder(URI.create(engineBaseUrl + "/process-instance?tenantIdIn=" + TENANT_ID_E_SIGNING)).GET().build();
		final var listResponse = HTTP_CLIENT.send(listRequest, HttpResponse.BodyHandlers.ofString());
		final JsonNode instances = OBJECT_MAPPER.readTree(listResponse.body());
		for (final JsonNode instance : instances) {
			final var deleteRequest = HttpRequest.newBuilder(
				URI.create(engineBaseUrl + "/process-instance/" + instance.get("id").asText() + "?skipCustomListeners=true&skipIoMappings=true&failIfNotExists=false"))
				.DELETE().build();
			HTTP_CLIENT.send(deleteRequest, HttpResponse.BodyHandlers.discarding());
		}
	}

	protected List<HistoricActivityInstanceDto> getProcessInstanceRoute(String processInstanceId) {
		return getRoute(processInstanceId, new ArrayList<HistoricActivityInstanceDto>());
	}

	private List<HistoricActivityInstanceDto> getRoute(String processInstanceId, List<HistoricActivityInstanceDto> route) {
		if (isNull(processInstanceId)) {
			return route;
		}
		return camundaClient.getHistoricActivities(processInstanceId).stream()
			.sorted(comparing(HistoricActivityInstanceDto::getEndTime))
			.flatMap(activity -> concat(List.of(activity).stream(), getRoute(activity.getCalledProcessInstanceId(), route).stream()))
			.toList();
	}
}
