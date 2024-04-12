package se.sundsvall.esigning.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import org.hibernate.validator.constraints.URL;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request model for starting a new e-signing process")
public class SigningRequest {

	@Schema(description = "Registration number for the document instance that owns the documentdata to be signed", example = "2024-1234", requiredMode = REQUIRED)
	@NotBlank
	private String registrationNumber;

	@Schema(description = "Filename for the documentdata instance that is to be signed. Needs to be valid pdf format.", example = "contract.pdf", requiredMode = REQUIRED)
	@NotBlank
	private String fileName;

	@Schema(description = "The date and time when the signing request expires. Format is yyyy-MM-dd'T'HH:mm:ss.SSSXXX", example = "2024-01-31T12:30:00.000", requiredMode = REQUIRED)
	@NotNull
	private OffsetDateTime expires;

	@Schema(description = "Notification message to send to signatories of the document.", example = "contract.pdf", requiredMode = REQUIRED)
	@NotNull
	@Valid
	private NotificationMessage notificationMessage;

	@Schema(description = "The party that has issued the signing of the document.", requiredMode = REQUIRED)
	@NotNull
	@Valid
	private Initiator initiator;

	@ArraySchema(schema = @Schema(implementation = Signatory.class), minItems = 1, uniqueItems = true)
	@NotEmpty
	private List<@Valid Signatory> signatories;

	@URL
	private String callbackUrl;

	public static SigningRequest create() {
		return new SigningRequest();
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public SigningRequest withRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
		return this;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public SigningRequest withFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}

	public OffsetDateTime getExpires() {
		return expires;
	}

	public void setExpires(OffsetDateTime expires) {
		this.expires = expires;
	}

	public SigningRequest withExpires(OffsetDateTime expires) {
		this.expires = expires;
		return this;
	}

	public NotificationMessage getNotificationMessage() {
		return notificationMessage;
	}

	public void setNotificationMessage(NotificationMessage notificationMessage) {
		this.notificationMessage = notificationMessage;
	}

	public SigningRequest withNotificationMessage(NotificationMessage notificationMessage) {
		this.notificationMessage = notificationMessage;
		return this;
	}

	public Initiator getInitiator() {
		return initiator;
	}

	public void setInitiator(Initiator initiator) {
		this.initiator = initiator;
	}

	public SigningRequest withInitiator(Initiator initiator) {
		this.initiator = initiator;
		return this;
	}

	public List<Signatory> getSignatories() {
		return signatories;
	}

	public void setSignatories(List<Signatory> signatories) {
		this.signatories = signatories;
	}

	public SigningRequest withSignatories(List<Signatory> signatories) {
		this.signatories = signatories;
		return this;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	public SigningRequest withCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(callbackUrl, expires, fileName, initiator, notificationMessage, registrationNumber, signatories);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SigningRequest)) {
			return false;
		}
		SigningRequest other = (SigningRequest) obj;
		return Objects.equals(callbackUrl, other.callbackUrl) && Objects.equals(expires, other.expires) && Objects.equals(fileName, other.fileName) && Objects.equals(initiator, other.initiator) && Objects.equals(notificationMessage,
			other.notificationMessage) && Objects.equals(registrationNumber, other.registrationNumber) && Objects.equals(signatories, other.signatories);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SigningRequest [registrationNumber=").append(registrationNumber).append(", fileName=").append(fileName).append(", expires=").append(expires).append(", notificationMessage=").append(notificationMessage).append(", initiator=")
			.append(initiator).append(", signatories=").append(signatories).append(", callbackUrl=").append(callbackUrl).append("]");
		return builder.toString();
	}
}
