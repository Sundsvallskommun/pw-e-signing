package se.sundsvall.esigning.configuration;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

@Configuration
public class GsonConfiguration {

	private static class OffsetDateTimeDeserializer implements JsonDeserializer<OffsetDateTime> {
		@Override
		public OffsetDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
			try {
				return OffsetDateTime.parse(json.getAsString(), ISO_OFFSET_DATE_TIME);
			} catch (Exception e) {
				throw new JsonParseException(e);
			}
		}
	}

	private static class OffsetDateTimeSerializer implements JsonSerializer<OffsetDateTime> {
		@Override
		public JsonElement serialize(OffsetDateTime offsetDateTime, Type type, JsonSerializationContext context) {
			return new JsonPrimitive(ISO_OFFSET_DATE_TIME.format(offsetDateTime));
		}
	}

	@Bean
	@Primary
	public Gson gson() {
		return new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeDeserializer())
			.registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeSerializer())
			.create();
	}
}
