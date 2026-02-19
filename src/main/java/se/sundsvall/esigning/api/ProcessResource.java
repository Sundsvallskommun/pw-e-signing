package se.sundsvall.esigning.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.api.model.StartResponse;
import se.sundsvall.esigning.service.ProcessService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.accepted;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

@Validated
@RestController
@RequestMapping("/{municipalityId}/process")
@Tag(name = "E-signing process endpoints", description = "Endpoints for managing e-signing processes")
class ProcessResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessResource.class);

	private final ProcessService service;

	ProcessResource(ProcessService service) {
		this.service = service;
	}

	@PostMapping(path = "start", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Start a new e-signing process instance", responses = {
		@ApiResponse(responseCode = "202", description = "Accepted", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
			Problem.class, ConstraintViolationProblem.class
		}))),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<StartResponse> startProcess(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@RequestBody @NotNull @Valid SigningRequest request) {

		final var startProcessResponse = new StartResponse(service.startProcess(municipalityId, request));
		LOGGER.info("Request for start of e-signing process for municipalityId {} and request {} has been received, resulting in an instance with id {}", sanitizeForLogging(municipalityId), sanitizeForLogging(request.toString()), startProcessResponse
			.getProcessId());

		return accepted().body(startProcessResponse);
	}
}
