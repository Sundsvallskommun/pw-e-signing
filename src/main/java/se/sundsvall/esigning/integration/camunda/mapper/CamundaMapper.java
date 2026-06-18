package se.sundsvall.esigning.integration.camunda.mapper;

import generated.se.sundsvall.camunda.PatchVariablesDto;
import generated.se.sundsvall.camunda.VariableValueDto;
import java.util.Map;
import org.camunda.bpm.engine.variable.value.SerializationDataFormat;

public final class CamundaMapper {

	private CamundaMapper() {}

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
