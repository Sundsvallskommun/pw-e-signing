package se.sundsvall.esigning.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import se.sundsvall.dept44.common.validators.annotation.OneOf;
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
	private NotificationMessage notificationMessage;

	@Schema(description = "The title for the party.", example = "CEO")
	private String title;

	@Schema(description = "The email for the party.", example = "john.doe@sundsvall.se", requiredMode = REQUIRED)
	@Email
	@NotNull
	private String email;

	@Schema(description = "The organization for the party.", example = "Sundsvalls kommun")
	private String organization;

	@Schema(description = "Language parameter that overwrites the language of the signing instance for the current party. Valid values are one of [sv, en, da, fr, de, nb, ru, zh, fi, uk]", example = "sv")
	@OneOf(value = { "sv", "en", "da", "fr", "de", "nb", "ru", "zh", "fi", "uk" }, message = "The provided language is not valid. Valid values are [sv, en, da, fr, de, nb, ru, zh, fi, uk].", nullable = true)
	private String language;

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

	public NotificationMessage getNotificationMessage() {
		return notificationMessage;
	}

	public void setNotificationMessage(NotificationMessage notificationMessage) {
		this.notificationMessage = notificationMessage;
	}

	public Signatory withNotificationMessage(NotificationMessage notificationMessage) {
		this.notificationMessage = notificationMessage;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Signatory withTitle(String title) {
		this.title = title;
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

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Signatory withLanguage(String language) {
		this.language = language;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, language, name, notificationMessage, organization, partyId, title);
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
		return Objects.equals(email, other.email) && Objects.equals(language, other.language) && Objects.equals(name, other.name) && Objects.equals(notificationMessage, other.notificationMessage) && Objects.equals(organization, other.organization)
			&& Objects.equals(partyId, other.partyId) && Objects.equals(title, other.title);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Signatory [name=").append(name).append(", partyId=").append(partyId).append(", notificationMessage=").append(notificationMessage).append(", title=").append(title).append(", email=").append(email).append(", organization=")
			.append(organization).append(", language=").append(language).append("]");
		return builder.toString();
	}

}
