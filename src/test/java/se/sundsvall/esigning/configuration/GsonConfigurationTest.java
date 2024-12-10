package se.sundsvall.esigning.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.esigning.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class GsonConfigurationTest {

	@Autowired
	private Gson gson;

	@Test
	void serializeOffsetDateTime() {
		final var dateTime = OffsetDateTime.now();
		final var bean = TestDateClass.create().withDateTime(dateTime);
		final var json = gson.toJson(bean);

		// Normalize JSON before comparing
		final var expectedJson = JsonParser.parseString("""
			{
				"dateTime": "%s"
			}
			""".formatted(DateTimeFormatter.ISO_DATE_TIME.format(dateTime))).toString();
		final var actualJson = JsonParser.parseString(json).toString();

		assertThat(actualJson).isEqualTo(expectedJson);
	}

	@Test
	void serializeNull() {
		final var json = gson.toJson(null);

		assertThat(json).isEqualTo("null");
	}

	@Test
	void serializeEmptyBean() {
		final var bean = TestDateClass.create();
		final var json = gson.toJson(bean);

		assertThat(json).isEqualTo("{}");
	}

	@Test
	void deserializeOffsetDateTime() {
		final var dateTime = OffsetDateTime.now();
		final var json = """
			{
			"dateTime": "%s"
			}""".formatted(DateTimeFormatter.ISO_DATE_TIME.format(dateTime));
		final var bean = gson.fromJson(json, TestDateClass.class);

		assertThat(bean.getDateTime()).isEqualTo(dateTime);
	}

	@Test
	void deserializeNull() {
		assertThat(gson.fromJson((String) null, TestDateClass.class)).isNull();
	}

	@Test
	void deserializeEmptyJson() {
		final var bean = gson.fromJson("{}", TestDateClass.class);
		assertThat(bean.getDateTime()).isNull();
	}

	@Test
	void deserializeThrowsException() {
		final var json = """
			{
			"dateTime": "bogus_value"
			}""";

		assertThrows(JsonParseException.class, () -> gson.fromJson(json, TestDateClass.class));
	}

	private static class TestDateClass {
		private OffsetDateTime dateTime;

		public static TestDateClass create() {
			return new TestDateClass();
		}

		public OffsetDateTime getDateTime() {
			return dateTime;
		}

		public TestDateClass withDateTime(final OffsetDateTime dateTime) {
			this.dateTime = dateTime;
			return this;
		}
	}
}
