package se.sundsvall.esigning.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@Schema(description = "A party assigned as signatory to the signing.")
public class Signatory {

	@Schema(description = "The name of the party.", example = "John Doe")
	private String name;

	@Schema(description = "The uuid of the party.", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = REQUIRED)
	@ValidUuid
	private String partyId;

	@Schema(description = "Optional message for the signature request emails for the specific party. Overwrites the default message in the signing request when provided.")
	@Valid
	private Message notificationMessage;

	@Schema(description = "The email for the party.", example = "john.doe@sundsvall.se", requiredMode = REQUIRED)
	@Email
	@NotNull
	private String email;

	@Schema(description = "The organization for the party.", example = "Sundsvalls kommun")
	private String organization;

	public static Signatory create() {
		return new Signatory();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Signatory withName(String name) {
		this.name = name;
		return this;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public Signatory withPartyId(String partyId) {
		this.partyId = partyId;
		return this;
	}

	public Message getNotificationMessage() {
		return notificationMessage;
	}

	public void setNotificationMessage(Message notificationMessage) {
		this.notificationMessage = notificationMessage;
	}

	public Signatory withNotificationMessage(Message notificationMessage) {
		this.notificationMessage = notificationMessage;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Signatory withEmail(String email) {
		this.email = email;
		return this;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public Signatory withOrganization(String organization) {
		this.organization = organization;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, name, notificationMessage, organization, partyId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Signatory)) {
			return false;
		}
		Signatory other = (Signatory) obj;
		return Objects.equals(email, other.email) && Objects.equals(name, other.name) && Objects.equals(notificationMessage, other.notificationMessage) && Objects.equals(organization, other.organization)
			&& Objects.equals(partyId, other.partyId);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Signatory [name=").append(name).append(", partyId=").append(partyId).append(", notificationMessage=").append(notificationMessage).append(", email=").append(email).append(", organization=")
			.append(organization).append("]");
		return builder.toString();
	}

}
