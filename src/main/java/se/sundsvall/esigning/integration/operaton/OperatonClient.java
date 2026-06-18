package se.sundsvall.esigning.integration.operaton;

import generated.se.sundsvall.operaton.DeploymentWithDefinitionsDto;
import generated.se.sundsvall.operaton.ProcessInstanceWithVariablesDto;
import generated.se.sundsvall.operaton.StartProcessInstanceDto;
import generated.se.sundsvall.operaton.VariableValueDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.io.File;
import java.time.OffsetDateTime;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import se.sundsvall.esigning.integration.operaton.configuration.OperatonConfiguration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static se.sundsvall.esigning.integration.operaton.configuration.OperatonConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.operaton.url}", configuration = OperatonConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface OperatonClient {

	@PostMapping(path = "process-definition/key/{key}/tenant-id/{tenant-id}/start", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	ProcessInstanceWithVariablesDto startProcessWithTenant(@PathVariable("key") String key, @PathVariable("tenant-id") String tenantId, StartProcessInstanceDto startProcessInstanceDto);

	@PutMapping(path = "process-instance/{id}/variables/{varName}", consumes = APPLICATION_JSON_VALUE)
	void setProcessInstanceVariable(@PathVariable("id") String id, @PathVariable("varName") String variableName, VariableValueDto variableValueDto);

	/**
	 * Deploys process resources to Operaton. Operaton's {@code POST /deployment/create} expects a
	 * {@code multipart/form-data} body where every field (including the file) is a form part.
	 * <p>
	 * The parameters are intentionally annotated with {@link PathVariable} even though none of the names occur in the
	 * path. Spring Cloud OpenFeign's contract treats a path variable whose name is missing from the path as a
	 * <b>form parameter</b> instead. Together with {@code consumes = multipart/form-data}, the configured form encoder
	 * then serializes each one as a multipart part - {@code tenant-id}, {@code deployment-name}, ... and {@code data}
	 * as the binary file part - which is exactly what the endpoint requires. Using {@code @RequestPart} would be more
	 * readable but produces identical Feign metadata; this mirrors {@code CamundaClient} so both engines behave the same.
	 */
	@PostMapping(path = "deployment/create", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
	DeploymentWithDefinitionsDto deploy(
		@PathVariable("tenant-id") String tenantId,
		@PathVariable("deployment-source") String deploymentSource,
		@PathVariable("deploy-changed-only") Boolean deployChangedOnly,
		@PathVariable("enable-duplicate-filtering") Boolean enableDuplicateFiltering,
		@PathVariable("deployment-name") String deploymentName,
		@PathVariable("deployment-activation-time") OffsetDateTime deploymentActivationTime,
		@PathVariable("data") File data);
}
