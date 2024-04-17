package se.sundsvall.esigning.businesslogic.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_ESIGNING_REQUEST;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_REQUEST_ID;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_COMFACT_SIGNING_ID;
import static se.sundsvall.esigning.integration.comfactfacade.mapper.ComfactFacadeMapper.toSigningRequest;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import com.google.gson.Gson;

import generated.se.sundsvall.camunda.VariableValueDto;
import generated.se.sundsvall.comfactfacade.CreateSigningResponse;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.businesslogic.handler.FailureHandler;
import se.sundsvall.esigning.integration.camunda.CamundaClient;
import se.sundsvall.esigning.integration.camunda.mapper.VariableFormat;
import se.sundsvall.esigning.integration.comfactfacade.ComfactFacadeClient;

@ExtendWith(MockitoExtension.class)
class InitiateSigningWorkerTest {

	private static final String REQUEST_ID = "RequestId";

	@Mock
	private CamundaClient camundaClientMock;

	@Mock
	private ExternalTask externalTaskMock;

	@Mock
	private ExternalTaskService externalTaskServiceMock;

	@Mock
	private FailureHandler failureHandlerMock;

	@Mock
	private Gson gsonMock;

	@Mock
	private ComfactFacadeClient comfactFacadeClientMock;

	@InjectMocks
	private InitiateSigningWorker worker;

	@Captor
	private ArgumentCaptor<VariableValueDto> variableValueDtoCaptor;

	@Test
	void verifyAnnotations() {
		// Assert
		assertThat(worker.getClass()).hasAnnotations(Component.class, ExternalTaskSubscription.class);
		assertThat(worker.getClass().getAnnotation(ExternalTaskSubscription.class).value()).isEqualTo("InitiateSigningTask");
	}

	@Test
	void executeWhenSigningIdExists() {
		// Arrange
		final var json = "json";
		final var bean = SigningRequest.create();
		final var signingId = UUID.randomUUID().toString();

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID)).thenReturn(signingId);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(externalTaskServiceMock).complete(externalTaskMock);
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock);
		verifyNoInteractions(comfactFacadeClientMock, failureHandlerMock);
	}

	@Test
	void executeWhenNoSigningIdExists() {
		// Arrange
		final var json = "json";
		final var bean = SigningRequest.create();
		final var processInstanceId = UUID.randomUUID().toString();
		final var signingId = UUID.randomUUID().toString();

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(externalTaskMock.getProcessInstanceId()).thenReturn(processInstanceId);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(comfactFacadeClientMock.createSigngingInstance(any())).thenReturn(new CreateSigningResponse().signingId(signingId));

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(comfactFacadeClientMock).createSigngingInstance(eq(toSigningRequest(bean)));
		verify(camundaClientMock).setProcessInstanceVariable(eq(processInstanceId), eq(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID), variableValueDtoCaptor.capture());
		verify(externalTaskServiceMock).complete(externalTaskMock);
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, comfactFacadeClientMock);
		verifyNoInteractions(failureHandlerMock);

		assertThat(variableValueDtoCaptor.getValue().getType()).isEqualTo(VariableFormat.STRING.getName());
		assertThat(variableValueDtoCaptor.getValue().getValue()).isEqualTo(signingId);
		assertThat(variableValueDtoCaptor.getValue().getValueInfo()).containsExactlyInAnyOrderEntriesOf(Map.of(
			"objectTypeName", String.class.getName(),
			"serializationDataFormat", VariableFormat.STRING.getName()));
	}

	@Test
	void executeThrowsExceptionOnFacadeCall() {
		// Arrange
		final var json = "json";
		final var bean = SigningRequest.create()
			.withExpires(OffsetDateTime.MAX)
			.withRegistrationNumber("2024-ACTIVE");
		final var problem = Problem.valueOf(Status.I_AM_A_TEAPOT, "Big and stout");

		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_REQUEST_ID)).thenReturn(REQUEST_ID);
		when(externalTaskMock.getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST)).thenReturn(json);
		when(gsonMock.fromJson(json, SigningRequest.class)).thenReturn(bean);
		when(comfactFacadeClientMock.createSigngingInstance(any())).thenThrow(problem);

		// Act
		worker.execute(externalTaskMock, externalTaskServiceMock);

		// Assert and verify
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_ESIGNING_REQUEST);
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_COMFACT_SIGNING_ID);
		verify(gsonMock).fromJson(json, SigningRequest.class);
		verify(comfactFacadeClientMock).createSigngingInstance(eq(toSigningRequest(bean)));
		verify(externalTaskMock).getVariable(CAMUNDA_VARIABLE_REQUEST_ID);
		verify(externalTaskMock).getId();
		verify(externalTaskMock).getBusinessKey();
		verify(failureHandlerMock).handleException(eq(externalTaskServiceMock), eq(externalTaskMock), anyString());
		verifyNoMoreInteractions(externalTaskServiceMock, externalTaskMock, gsonMock, failureHandlerMock, comfactFacadeClientMock);
	}
}
