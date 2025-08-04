package se.sundsvall.esigning.businesslogic.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UriUtilityTest {

	@ParameterizedTest
	@MethodSource("testArgumentsProvider")
	void test(String url, String processId, String expected) {
		assertThat(UriUtility.addProcessIdParameter(url, processId)).hasToString(expected);
	}

	private static Stream<Arguments> testArgumentsProvider() {
		final var uuid = UUID.randomUUID().toString();
		return Stream.of(
			Arguments.of("http://host.com", uuid, "http://host.com?processId=" + uuid),
			Arguments.of("http://host.com?key1=value1&key2=value2", uuid, "http://host.com?key1=value1&key2=value2&processId=" + uuid),
			Arguments.of("http://host.com?processId=1", uuid, "http://host.com?processId=1&processId=" + uuid));
	}
}
