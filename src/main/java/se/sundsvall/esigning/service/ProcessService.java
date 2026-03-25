package se.sundsvall.esigning.service;

import org.springframework.stereotype.Service;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.integration.camunda.CamundaClient;
import se.sundsvall.esigning.integration.camunda.mapper.CamundaMapper;

import static se.sundsvall.esigning.Constants.PROCESS_KEY;
import static se.sundsvall.esigning.Constants.TENANT_ID;

@Service
public class ProcessService {

	private final CamundaClient camundaClient;
	private final CamundaMapper camundaMapper;

	public ProcessService(CamundaClient camundaClient, CamundaMapper camundaMapper) {
		this.camundaClient = camundaClient;
		this.camundaMapper = camundaMapper;
	}

	public String startProcess(String municipalityId, SigningRequest request) {
		return camundaClient.startProcessWithTenant(PROCESS_KEY, TENANT_ID, camundaMapper.toStartProcessInstanceDto(municipalityId, request)).getId();
	}
}
