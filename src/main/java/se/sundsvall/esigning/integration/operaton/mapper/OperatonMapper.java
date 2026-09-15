package se.sundsvall.esigning.integration.operaton.mapper;

import com.google.gson.Gson;
import generated.se.sundsvall.operaton.StartProcessInstanceDto;
import generated.se.sundsvall.operaton.VariableValueDto;
import java.util.Map;
import org.camunda.bpm.engine.variable.value.SerializationDataFormat;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.configuration.ProcessProperties;
import se.sundsvall.esigning.integration.camunda.mapper.VariableFormat;

import static se.sundsvall.esigning.Constants.PROCESS_VARIABLE_E_SIGNING_REQUEST;
import static se.sundsvall.esigning.Constants.PROCESS_VARIABLE_MUNICIPALITY_ID;
import static se.sundsvall.esigning.Constants.PROCESS_VARIABLE_REQUEST_ID;
import static se.sundsvall.esigning.Constants.PROCESS_VARIABLE_WAIT_DURATION;

@Component
public class OperatonMapper {

	private final Gson gson;
	private final ProcessProperties processProperties;

	OperatonMapper(Gson gson, ProcessProperties processProperties) {
		this.gson = gson;
		this.processProperties = processProperties;
	}

	public StartProcessInstanceDto toStartProcessInstanceDto(String municipalityId, SigningRequest request) {
		return new StartProcessInstanceDto()
			.businessKey(request.getRegistrationNumber())
			.variables(Map.of(
				PROCESS_VARIABLE_MUNICIPALITY_ID, toVariableValueDto(VariableFormat.STRING, String.class, municipalityId),
				PROCESS_VARIABLE_E_SIGNING_REQUEST, toVariableValueDto(VariableFormat.JSON, request.getClass(), gson.toJson(request)),
				PROCESS_VARIABLE_REQUEST_ID, toVariableValueDto(VariableFormat.STRING, String.class, RequestId.get()),
				PROCESS_VARIABLE_WAIT_DURATION, toVariableValueDto(VariableFormat.STRING, String.class, processProperties.waitDuration())));
	}

	public static VariableValueDto toVariableValueDto(SerializationDataFormat format, Class<?> objectClass, Object objectValue) {
		return new VariableValueDto()
			.type(format.getName())
			.value(objectValue)
			.valueInfo(Map.of(
				"objectTypeName", objectClass.getName(),
				"serializationDataFormat", format.getName()));
	}
}
