package se.sundsvall.esigning.api.model;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Message model for signature request emails.")
public class Message {

	@Schema(description = "The subject of the message.", example = "Please sign the document")
	@NotBlank
	private String subject;

	@Schema(description = "The body of the message.", example = "Dear John Doe, please sign the document.")
	@NotBlank
	private String body;

	public static Message create() {
		return new Message();
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Message withSubject(String subject) {
		this.subject = subject;
		return this;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Message withBody(String body) {
		this.body = body;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(body, subject);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Message)) {
			return false;
		}
		Message other = (Message) obj;
		return Objects.equals(body, other.body) && Objects.equals(subject, other.subject);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NotificationMessage [subject=").append(subject).append(", body=").append(body).append("]");
		return builder.toString();
	}
}
