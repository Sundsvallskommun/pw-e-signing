package se.sundsvall.esigning.configuration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import se.sundsvall.esigning.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class GsonConfigurationTest {

	private static class TestDateClass {
		private OffsetDateTime dateTime;

		public static TestDateClass create() {
			return new TestDateClass();
		}

		public OffsetDateTime getDateTime() {
			return dateTime;
		}

		public TestDateClass withDateTime(OffsetDateTime dateTime) {
			this.dateTime = dateTime;
			return this;
		}
	};

	@Autowired
	private Gson gson;

	@Test
	void serializeOffsetDateTime() throws Exception {
		final var dateTime = OffsetDateTime.now();
		final var bean = TestDateClass.create().withDateTime(dateTime);
		final var json = gson.toJson(bean);

		assertThat(json).isEqualTo("""
			{
			  \"dateTime\": \"%s\"
			}""".formatted(DateTimeFormatter.ISO_DATE_TIME.format(dateTime)));
	}

	@Test
	void deserializeOffsetDateTime() {
		final var dateTime = OffsetDateTime.now();
		final var json = """
			{
			  \"dateTime\": \"%s\"
			}""".formatted(DateTimeFormatter.ISO_DATE_TIME.format(dateTime));
		final var bean = gson.fromJson(json, TestDateClass.class);

		assertThat(bean.getDateTime()).isEqualTo(dateTime);
	}

	@Test
	void deserializeThrowsException() {
		final var json = """
			{
			  \"dateTime\": \"bogus_value\"
			}""";

		assertThrows(JsonParseException.class, () -> gson.fromJson(json, TestDateClass.class));
	}
}
