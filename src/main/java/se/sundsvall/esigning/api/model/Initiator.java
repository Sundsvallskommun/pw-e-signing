package se.sundsvall.esigning.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@Schema(description = "A party assigned as initiator to the signing process.")
public class Initiator {

	@Schema(description = "The name of the party.", examples = "John Doe")
	private String name;

	@Schema(description = "The uuid of the party.", examples = "550e8400-e29b-41d4-a716-446655440000", requiredMode = REQUIRED)
	@ValidUuid
	private String partyId;

	@Schema(description = "The email for the party.", examples = "john.doe@sundsvall.se", requiredMode = REQUIRED)
	@Email
	@NotNull
	private String email;

	@Schema(description = "The organization for the party.", examples = "Sundsvalls kommun")
	private String organization;

	public static Initiator create() {
		return new Initiator();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Initiator withName(String name) {
		this.name = name;
		return this;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public Initiator withPartyId(String partyId) {
		this.partyId = partyId;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Initiator withEmail(String email) {
		this.email = email;
		return this;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public Initiator withOrganization(String organization) {
		this.organization = organization;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, name, organization, partyId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Initiator)) {
			return false;
		}
		Initiator other = (Initiator) obj;
		return Objects.equals(email, other.email) && Objects.equals(name, other.name) && Objects.equals(organization, other.organization) && Objects.equals(partyId, other.partyId);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Initiator [name=").append(name).append(", partyId=").append(partyId).append(", email=").append(email).append(", organization=").append(organization).append("]");
		return builder.toString();
	}

}
