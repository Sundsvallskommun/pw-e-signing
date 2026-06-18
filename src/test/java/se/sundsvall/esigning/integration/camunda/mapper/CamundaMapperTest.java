package se.sundsvall.esigning.integration.camunda.mapper;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

class CamundaMapperTest {

	@Test
	void toVariableValueDto() {
		final var value = "value";

		final var dto = CamundaMapper.toVariableValueDto(VariableFormat.STRING, value.getClass(), value);

		assertThat(dto.getType()).isEqualTo(VariableFormat.STRING.getName());
		assertThat(dto.getValue()).isEqualTo(value);
		assertThat(dto.getValueInfo()).hasSize(2)
			.containsExactlyInAnyOrderEntriesOf(Map.of(
				"objectTypeName", value.getClass().getName(),
				"serializationDataFormat", VariableFormat.STRING.getName()));
	}

	@Test
	void toPatchVariablesDto() {
		final var key = "key";
		final var value = CamundaMapper.toVariableValueDto(VariableFormat.STRING, String.class, "value");
		final var dto = CamundaMapper.toPatchVariablesDto(Map.of(key, value));

		assertThat(dto.getDeletions()).isNullOrEmpty();
		assertThat(dto.getModifications()).hasSize(1).containsExactly(entry(key, value));
	}
}
