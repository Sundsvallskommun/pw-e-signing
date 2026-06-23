package se.sundsvall.esigning.service;

import org.springframework.stereotype.Service;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.integration.operaton.OperatonClient;
import se.sundsvall.esigning.integration.operaton.mapper.OperatonMapper;

import static se.sundsvall.esigning.Constants.PROCESS_KEY;
import static se.sundsvall.esigning.Constants.TENANT_ID;

@Service
public class ProcessService {

	private final OperatonClient operatonClient;
	private final OperatonMapper operatonMapper;

	public ProcessService(OperatonClient operatonClient, OperatonMapper operatonMapper) {
		this.operatonClient = operatonClient;
		this.operatonMapper = operatonMapper;
	}

	public String startProcess(String municipalityId, SigningRequest request) {
		// New processes are always created in Operaton.
		return operatonClient.startProcessWithTenant(PROCESS_KEY, TENANT_ID, operatonMapper.toStartProcessInstanceDto(municipalityId, request)).getId();
	}
}
