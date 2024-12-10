package se.sundsvall.esigning.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "Start process response")
public class StartResponse {

	@Schema(description = "Process ID", example = "5", accessMode = READ_ONLY)
	private String processId;

	public StartResponse() {}

	public StartResponse(String processId) {
		this.processId = processId;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(processId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof StartResponse)) {
			return false;
		}
		StartResponse other = (StartResponse) obj;
		return Objects.equals(processId, other.processId);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StartResponse [processId=").append(processId).append("]");
		return builder.toString();
	}

}
