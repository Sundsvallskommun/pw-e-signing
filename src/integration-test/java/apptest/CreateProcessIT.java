package apptest;

import com.fasterxml.jackson.core.JsonProcessingException;
import generated.se.sundsvall.camunda.HistoricActivityInstanceDto;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.esigning.Application;
import se.sundsvall.esigning.api.model.StartResponse;
import se.sundsvall.esigning.integration.camunda.CamundaClient;

import java.time.Duration;

import static generated.se.sundsvall.camunda.HistoricProcessInstanceDto.StateEnum.COMPLETED;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.setDefaultPollDelay;
import static org.awaitility.Awaitility.setDefaultPollInterval;
import static org.awaitility.Awaitility.setDefaultTimeout;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.ACCEPTED;

@WireMockAppTestSuite(files = "classpath:/CreateProcess/", classes = Application.class)
class CreateProcessIT extends AbstractCamundaAppTest {
	private static final int DEFAULT_TESTCASE_TIMEOUT_IN_SECONDS = 30;
	private static final String REQUEST_FILE = "request.json";
	private static final String PATH = "/2281/process/start";

	@Autowired
	private CamundaClient camundaClient;

	@BeforeEach
	void setup() {
		setDefaultPollInterval(500, MILLISECONDS);
		setDefaultPollDelay(Duration.ZERO);
		setDefaultTimeout(Duration.ofSeconds(DEFAULT_TESTCASE_TIMEOUT_IN_SECONDS));

		await()
			.ignoreExceptions()
			.until(() -> camundaClient.getDeployments("process-e-signing.bpmn", null, null).size(), equalTo(1));

		verifyAllStubs();
	}

	@Test
	void test001_createProcessWithCallbackBeingCompleted() throws JsonProcessingException, ClassNotFoundException {

		// Callback mock address
		final var callbackUrl = "http://localhost:%s/callback-mock".formatted(wiremock.port());

		// Setup call and modify incoming callback to point to a mock
		final var call = setupCall();
		final var request = StringUtils.replace(fromTestFile(REQUEST_FILE), "[callback.url]", callbackUrl);

		// Start process
		final var startResponse = call
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest(request)
			.withExpectedResponseStatus(ACCEPTED)
			.sendRequest()
			.andReturnBody(StartResponse.class);

		// Wait for process to finish
		await()
			.ignoreExceptions()
			.atMost(DEFAULT_TESTCASE_TIMEOUT_IN_SECONDS, SECONDS)
			.until(() -> camundaClient.getHistoricProcessInstance(startResponse.getProcessId()).getState(), equalTo(COMPLETED));

		// Verify mocked stubs
		verifyAllStubs();

		// Verify process pathway
		assertThat(getProcessInstanceRoute(startResponse.getProcessId()))
			.extracting(HistoricActivityInstanceDto::getActivityName, HistoricActivityInstanceDto::getActivityId)
			.containsExactlyInAnyOrder(
				tuple("Start process", "start_process"),
				tuple("Add signal for ongoing signing", "add_ongoing_signing_signal"),
				tuple("Initiate signing", "initiate_signing"),
				tuple("Add signingId to document metdata", "add_signing_id"),
				tuple("Wait", "wait_timer"),
				tuple("Check signing status", "check_signing_status"),
				tuple("Gateway for signing status", "gateway_signing_status"), // The signing status is not complete on the first call
				tuple("Wait", "wait_timer"),
				tuple("Check signing status", "check_signing_status"),
				tuple("Gateway for signing status", "gateway_signing_status"), // The signing status is complete on the second call
				tuple("Add signed document", "add_signed_document"),
				tuple("Add signatory metadata to document", "add_metadata_to_signed_document"),
				tuple("Callback present?", "gateway_callback_present"),
				tuple("Execute callback", "execute_callback"),
				tuple("Remove signal for ongoing signing", "remove_ongoing_signing_signal"),
				tuple("End process", "end_process"));
	}

	@Test
	void test002_createProcessWithoutCallbackBeingCompleted() throws JsonProcessingException, ClassNotFoundException {

		// Start process
		final var startResponse = setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(ACCEPTED)
			.sendRequest()
			.andReturnBody(StartResponse.class);

		// Wait for process to finish
		await()
			.ignoreExceptions()
			.atMost(DEFAULT_TESTCASE_TIMEOUT_IN_SECONDS, SECONDS)
			.until(() -> camundaClient.getHistoricProcessInstance(startResponse.getProcessId()).getState(), equalTo(COMPLETED));

		// Verify mocked stubs
		verifyAllStubs();

		// Verify process pathway
		assertThat(getProcessInstanceRoute(startResponse.getProcessId()))
			.extracting(HistoricActivityInstanceDto::getActivityName, HistoricActivityInstanceDto::getActivityId)
			.containsExactlyInAnyOrder(
				tuple("Start process", "start_process"),
				tuple("Add signal for ongoing signing", "add_ongoing_signing_signal"),
				tuple("Initiate signing", "initiate_signing"),
				tuple("Add signingId to document metdata", "add_signing_id"),
				tuple("Wait", "wait_timer"),
				tuple("Check signing status", "check_signing_status"),
				tuple("Gateway for signing status", "gateway_signing_status"), // The signing status is not complete on the first call
				tuple("Wait", "wait_timer"),
				tuple("Check signing status", "check_signing_status"),
				tuple("Gateway for signing status", "gateway_signing_status"), // The signing status is complete on the second call
				tuple("Add signed document", "add_signed_document"),
				tuple("Add signatory metadata to document", "add_metadata_to_signed_document"),
				tuple("Callback present?", "gateway_callback_present"),
				tuple("Remove signal for ongoing signing", "remove_ongoing_signing_signal"),
				tuple("End process", "end_process"));
	}

	@Test
	void test003_createProcessWithCallbackGettingExpired() throws JsonProcessingException, ClassNotFoundException {

		// Callback mock address
		final var callbackUrl = "http://localhost:%s/callback-mock".formatted(wiremock.port());

		// Setup call and modify incoming callback to point to a mock
		final var call = setupCall();
		final var request = StringUtils.replace(fromTestFile(REQUEST_FILE), "[callback.url]", callbackUrl);

		// Start process
		final var startResponse = call
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest(request)
			.withExpectedResponseStatus(ACCEPTED)
			.sendRequest()
			.andReturnBody(StartResponse.class);

		// Wait for process to finish
		await()
			.ignoreExceptions()
			.atMost(DEFAULT_TESTCASE_TIMEOUT_IN_SECONDS, SECONDS)
			.until(() -> camundaClient.getHistoricProcessInstance(startResponse.getProcessId()).getState(), equalTo(COMPLETED));

		// Verify mocked stubs
		verifyAllStubs();

		// Verify process pathway
		assertThat(getProcessInstanceRoute(startResponse.getProcessId()))
			.extracting(HistoricActivityInstanceDto::getActivityName, HistoricActivityInstanceDto::getActivityId)
			.containsExactlyInAnyOrder(
				tuple("Start process", "start_process"),
				tuple("Add signal for ongoing signing", "add_ongoing_signing_signal"),
				tuple("Initiate signing", "initiate_signing"),
				tuple("Add signingId to document metdata", "add_signing_id"),
				tuple("Wait", "wait_timer"),
				tuple("Check signing status", "check_signing_status"),
				tuple("Gateway for signing status", "gateway_signing_status"), // The signing status is expired on the first call
				tuple("Handle not signed document", "handle_unsigned_document_signing"),
				tuple("Callback present?", "gateway_callback_present"),
				tuple("Execute callback", "execute_callback"),
				tuple("Remove signal for ongoing signing", "remove_ongoing_signing_signal"),
				tuple("End process", "end_process"));
	}

	@Test
	void test004_createProcessWithoutCallbackGettingExpired() throws JsonProcessingException, ClassNotFoundException {

		// Start process
		final var startResponse = setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(ACCEPTED)
			.sendRequest()
			.andReturnBody(StartResponse.class);

		// Wait for process to finish
		await()
			.ignoreExceptions()
			.atMost(DEFAULT_TESTCASE_TIMEOUT_IN_SECONDS, SECONDS)
			.until(() -> camundaClient.getHistoricProcessInstance(startResponse.getProcessId()).getState(), equalTo(COMPLETED));

		// Verify mocked stubs
		verifyAllStubs();

		// Verify process pathway
		assertThat(getProcessInstanceRoute(startResponse.getProcessId()))
			.extracting(HistoricActivityInstanceDto::getActivityName, HistoricActivityInstanceDto::getActivityId)
			.containsExactlyInAnyOrder(
				tuple("Start process", "start_process"),
				tuple("Add signal for ongoing signing", "add_ongoing_signing_signal"),
				tuple("Initiate signing", "initiate_signing"),
				tuple("Add signingId to document metdata", "add_signing_id"),
				tuple("Wait", "wait_timer"),
				tuple("Check signing status", "check_signing_status"),
				tuple("Gateway for signing status", "gateway_signing_status"), // The signing status is expired on the first call
				tuple("Handle not signed document", "handle_unsigned_document_signing"),
				tuple("Callback present?", "gateway_callback_present"),
				tuple("Remove signal for ongoing signing", "remove_ongoing_signing_signal"),
				tuple("End process", "end_process"));
	}
}
