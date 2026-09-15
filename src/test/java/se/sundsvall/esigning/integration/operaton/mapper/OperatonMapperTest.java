package se.sundsvall.esigning.integration.operaton.mapper;

import com.google.gson.Gson;
import generated.se.sundsvall.operaton.VariableValueDto;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.esigning.Application;
import se.sundsvall.esigning.api.model.Initiator;
import se.sundsvall.esigning.api.model.Message;
import se.sundsvall.esigning.api.model.Signatory;
import se.sundsvall.esigning.api.model.SigningRequest;
import se.sundsvall.esigning.integration.camunda.mapper.VariableFormat;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.esigning.Constants.PROCESS_VARIABLE_E_SIGNING_REQUEST;
import static se.sundsvall.esigning.Constants.PROCESS_VARIABLE_MUNICIPALITY_ID;
import static se.sundsvall.esigning.Constants.PROCESS_VARIABLE_REQUEST_ID;
import static se.sundsvall.esigning.Constants.PROCESS_VARIABLE_WAIT_DURATION;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class OperatonMapperTest {

	@Autowired
	private OperatonMapper operatonMapper;

	@Autowired
	private Gson gson;

	@Test
	void toStartProcessInstanceDto() {
		final var registrationNumber = "registrationNumber";
		final var municipalityId = "municipalityId";
		final var request = SigningRequest.create()
			.withRegistrationNumber(registrationNumber)
			.withInitiator(Initiator.create())
			.withNotificationMessage(Message.create())
			.withSignatories(List.of(Signatory.create()));

		final var dto = operatonMapper.toStartProcessInstanceDto(municipalityId, request);

		assertThat(dto.getBusinessKey()).isEqualTo(registrationNumber);
		assertThat(dto.getVariables().entrySet()).containsExactlyInAnyOrder(
			entry(PROCESS_VARIABLE_E_SIGNING_REQUEST, new VariableValueDto()
				.type(VariableFormat.JSON.getName())
				.value(gson.toJson(request))
				.valueInfo(Map.of(
					"objectTypeName", SigningRequest.class.getName(),
					"serializationDataFormat", VariableFormat.JSON.getName()))),
			entry(PROCESS_VARIABLE_WAIT_DURATION, new VariableValueDto()
				.type(VariableFormat.STRING.getName())
				.value("R/PT10S")
				.valueInfo(Map.of(
					"objectTypeName", String.class.getName(),
					"serializationDataFormat", VariableFormat.STRING.getName()))),
			entry(PROCESS_VARIABLE_REQUEST_ID, new VariableValueDto()
				.type(VariableFormat.STRING.getName())
				.value(RequestId.get())
				.valueInfo(Map.of(
					"objectTypeName", String.class.getName(),
					"serializationDataFormat", VariableFormat.STRING.getName()))),
			entry(PROCESS_VARIABLE_MUNICIPALITY_ID, new VariableValueDto()
				.type(VariableFormat.STRING.getName())
				.value(municipalityId)
				.valueInfo(Map.of(
					"objectTypeName", String.class.getName(),
					"serializationDataFormat", VariableFormat.STRING.getName()))));
	}

	@Test
	void toVariableValueDto() {
		final var value = "value";

		final var dto = OperatonMapper.toVariableValueDto(VariableFormat.STRING, value.getClass(), value);

		assertThat(dto.getType()).isEqualTo(VariableFormat.STRING.getName());
		assertThat(dto.getValue()).isEqualTo(value);
		assertThat(dto.getValueInfo()).hasSize(2)
			.containsExactlyInAnyOrderEntriesOf(Map.of(
				"objectTypeName", value.getClass().getName(),
				"serializationDataFormat", VariableFormat.STRING.getName()));
	}
}
