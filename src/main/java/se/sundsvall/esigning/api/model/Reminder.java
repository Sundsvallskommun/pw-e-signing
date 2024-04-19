package se.sundsvall.esigning.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

import java.time.OffsetDateTime;
import java.util.Objects;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Reminder message model for the signature request.")
public class Reminder {

	@Schema(description = "The interval in hours between each reminder message.", example = "24", requiredMode = REQUIRED)
	@Min(1)
	private int intervalInHours;

	@Schema(description = "Reminder message sent to parties that has not fulfilled the signing task within the given timeframe.", requiredMode = REQUIRED)
	@NotNull
	@Valid
	private Message reminderMessage;

	@Schema(description = "The date and time when the first reminder message will be sent.", example = "2021-12-31T23:59:59Z", requiredMode = REQUIRED)
	@NotNull
	@Future
	@DateTimeFormat(iso = DATE_TIME)
	private OffsetDateTime startDateTime;

	public static Reminder create() {
		return new Reminder();
	}

	public int getIntervalInHours() {
		return intervalInHours;
	}

	public void setIntervalInHours(int intervalInHours) {
		this.intervalInHours = intervalInHours;
	}

	public Reminder withIntervalInHours(int intervalInHours) {
		this.intervalInHours = intervalInHours;
		return this;
	}

	public Message getReminderMessage() {
		return reminderMessage;
	}

	public void setReminderMessage(Message reminderMessage) {
		this.reminderMessage = reminderMessage;
	}

	public Reminder withReminderMessage(Message reminderMessage) {
		this.reminderMessage = reminderMessage;
		return this;
	}

	public OffsetDateTime getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(OffsetDateTime startDateTime) {
		this.startDateTime = startDateTime;
	}

	public Reminder withStartDateTime(OffsetDateTime startDateTime) {
		this.startDateTime = startDateTime;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(intervalInHours, reminderMessage, startDateTime);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Reminder)) {
			return false;
		}
		Reminder other = (Reminder) obj;
		return intervalInHours == other.intervalInHours && Objects.equals(reminderMessage, other.reminderMessage) && Objects.equals(startDateTime, other.startDateTime);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Reminder [intervalInHours=").append(intervalInHours).append(", reminderMessage=").append(reminderMessage).append(", startDateTime=").append(startDateTime).append("]");
		return builder.toString();
	}
}
