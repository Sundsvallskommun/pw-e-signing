package se.sundsvall.esigning.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import se.sundsvall.dept44.common.validators.annotation.OneOf;

@Schema(description = "Custom message for the signature request emails.")
public class NotificationMessage {

	@Schema(description = "The subject of the notification message.", example = "Please sign the document")
	@NotBlank
	private String subject;

	@Schema(description = "The body of the notification message.", example = "Dear John Doe, please sign the document.")
	@NotBlank
	private String body;

	@Schema(description = "The language of the notification message. Valid values are one of [sv, en, da, fr, de, nb, ru, zh, fi, uk]", example = "sv", requiredMode = REQUIRED)
	@OneOf(value = { "sv", "en", "da", "fr", "de", "nb", "ru", "zh", "fi", "uk" }, message = "The provided language is missing or not valid. Valid values are [sv, en, da, fr, de, nb, ru, zh, fi, uk].")
	private String language;

	public static NotificationMessage create() {
		return new NotificationMessage();
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public NotificationMessage withSubject(String subject) {
		this.subject = subject;
		return this;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public NotificationMessage withBody(String body) {
		this.body = body;
		return this;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public NotificationMessage withLanguage(String language) {
		this.language = language;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(body, language, subject);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof NotificationMessage)) {
			return false;
		}
		NotificationMessage other = (NotificationMessage) obj;
		return Objects.equals(body, other.body) && Objects.equals(language, other.language) && Objects.equals(subject, other.subject);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NotificationMessage [subject=").append(subject).append(", body=").append(body).append(", language=").append(language).append("]");
		return builder.toString();
	}
}
