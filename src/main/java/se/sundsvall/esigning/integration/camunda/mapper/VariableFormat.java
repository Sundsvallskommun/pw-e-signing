package se.sundsvall.esigning.integration.camunda.mapper;

import org.camunda.bpm.engine.variable.value.SerializationDataFormat;

public enum VariableFormat implements SerializationDataFormat {
	STRING("String"),
	JSON("Json");

	final String name;

	private VariableFormat(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}
