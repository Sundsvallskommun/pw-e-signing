package se.sundsvall.esigning.service;

import generated.se.sundsvall.operaton.ProcessInstanceWithVariablesDto;
import generated.se.sundsvall.operaton.StartProcessInstanceDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.integration.operaton.OperatonClient;
import se.sundsvall.esigning.integration.operaton.mapper.OperatonMapper;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessServiceTest {

	@Mock
	private OperatonClient operatonClientMock;

	@Mock
	private OperatonMapper operatonMapperMock;

	@InjectMocks
	private ProcessService processService;

	@Test
	void startProcess() {

		// Arrange
		final var process = "process-e-signing";
		final var tenant = "E_SIGNING";
		final var request = SigningRequest.create();
		final var uuid = randomUUID().toString();
		final var logId = randomUUID().toString();
		final var startProcessInstance = new StartProcessInstanceDto();
		final var processInstance = new ProcessInstanceWithVariablesDto().id(uuid);
		final var municipalityId = "municipalityId";

		when(operatonMapperMock.toStartProcessInstanceDto(municipalityId, request)).thenReturn(startProcessInstance);
		when(operatonClientMock.startProcessWithTenant(process, tenant, startProcessInstance)).thenReturn(processInstance);

		// Mock static RequestId to enable spy and to verify that static method is being called
		try (MockedStatic<RequestId> requestIdMock = mockStatic(RequestId.class)) {
			requestIdMock.when(RequestId::get).thenReturn(logId);

			// Act
			assertThat(processService.startProcess(municipalityId, request)).isEqualTo(uuid);
		}

		// Assert
		verify(operatonClientMock).startProcessWithTenant(process, tenant, startProcessInstance);
		verifyNoMoreInteractions(operatonClientMock);
	}
}
