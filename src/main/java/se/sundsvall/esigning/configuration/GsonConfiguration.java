package se.sundsvall.esigning.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.util.Optional.ofNullable;

@Configuration
public class GsonConfiguration {

	private static class OffsetDateTimeDeserializer implements JsonDeserializer<OffsetDateTime> {
		@Override
		public OffsetDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
			try {
				return ofNullable(json)
					.map(JsonElement::getAsString)
					.map(string -> OffsetDateTime.parse(string, ISO_OFFSET_DATE_TIME))
					.orElse(null);
			} catch (Exception e) {
				throw new JsonParseException(e);
			}
		}
	}

	private static class OffsetDateTimeSerializer implements JsonSerializer<OffsetDateTime> {
		@Override
		public JsonElement serialize(OffsetDateTime offsetDateTime, Type type, JsonSerializationContext context) {
			return ofNullable(offsetDateTime)
				.map(ISO_OFFSET_DATE_TIME::format)
				.map(JsonPrimitive::new)
				.orElse(null);
		}
	}

	@Bean
	@Primary
	Gson gson() {
		return new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeDeserializer())
			.registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeSerializer())
			.create();
	}
}
