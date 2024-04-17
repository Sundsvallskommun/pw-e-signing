package se.sundsvall.esigning.integration.camunda.mapper;

import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_ESIGNING_REQUEST;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_REQUEST_ID;
import static se.sundsvall.esigning.Constants.CAMUNDA_VARIABLE_WAIT_DURATION;

import java.util.Map;

import org.camunda.bpm.engine.variable.value.SerializationDataFormat;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import generated.se.sundsvall.camunda.PatchVariablesDto;
import generated.se.sundsvall.camunda.StartProcessInstanceDto;
import generated.se.sundsvall.camunda.VariableValueDto;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.configuration.ProcessProperties;

@Component
public class CamundaMapper {

	private final Gson gson;
	private final ProcessProperties processProperties;

	CamundaMapper(Gson gson, ProcessProperties processProperties) {
		this.gson = gson;
		this.processProperties = processProperties;
	}

	public StartProcessInstanceDto toStartProcessInstanceDto(SigningRequest request) {
		return new StartProcessInstanceDto()
			.businessKey(request.getRegistrationNumber())
			.variables(Map.of(
				CAMUNDA_VARIABLE_ESIGNING_REQUEST, toVariableValueDto(VariableFormat.JSON, request.getClass(), gson.toJson(request)),
				CAMUNDA_VARIABLE_REQUEST_ID, toVariableValueDto(VariableFormat.STRING, String.class, RequestId.get()),
				CAMUNDA_VARIABLE_WAIT_DURATION, toVariableValueDto(VariableFormat.STRING, String.class, processProperties.waitDuration())));
	}

	public static VariableValueDto toVariableValueDto(SerializationDataFormat format, Class<?> objectClass, Object objectValue) {
		return new VariableValueDto()
			.type(format.getName())
			.value(objectValue)
			.valueInfo(Map.of(
				"objectTypeName", objectClass.getName(),
				"serializationDataFormat", format.getName()));
	}

	public static PatchVariablesDto toPatchVariablesDto(Map<String, VariableValueDto> variablesToUpdate) {
		return new PatchVariablesDto()
			.modifications(variablesToUpdate);
	}
}
