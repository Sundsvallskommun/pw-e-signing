package se.sundsvall.esigning.integration.camunda.mapper;

import org.camunda.bpm.engine.variable.value.SerializationDataFormat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.esigning.integration.camunda.mapper.VariableFormat.JSON;
import static se.sundsvall.esigning.integration.camunda.mapper.VariableFormat.STRING;

class VariableFormatTest {

	@Test
	void enums() {
		assertThat(VariableFormat.values())
			.containsExactlyInAnyOrder(JSON, STRING)
			.allSatisfy(item -> assertThat(item).isInstanceOf(SerializationDataFormat.class));
	}

	@Test
	void getName() {
		assertThat(JSON.getName()).isEqualTo("Json");
		assertThat(STRING.getName()).isEqualTo("String");
	}
}
