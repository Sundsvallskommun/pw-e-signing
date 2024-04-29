package se.sundsvall.esigning;

public final class Constants {

	private Constants() {}

	public static final String PROCESS_KEY = "process-e-signing"; // Must match ID of process defined in bpmn schema
	public static final String TENANTID = "E_SIGNING"; // Namespace where process is deployed, a.k.a tenant (must match setting in application.yaml)

	public static final String CAMUNDA_VARIABLE_REQUEST_ID = "requestId";
	public static final String CAMUNDA_VARIABLE_ESIGNING_REQUEST = "eSigningRequest";
	public static final String CAMUNDA_VARIABLE_WAIT_DURATION = "waitDuration";
	public static final String CAMUNDA_VARIABLE_CALLBACK_PRESENT = "callbackPresent";
	public static final String CAMUNDA_VARIABLE_COMFACT_SIGNING_ID = "comfactSigningId";
	public static final String CAMUNDA_VARIABLE_COMFACT_SIGNING_STATUS = "comfactSigningStatus";

	public static final String DOCUMENT_USER = "E-signing-process";
	public static final String DOCUMENT_METADATA_KEY_SIGNING_IN_PROGRESS = "signingInProgress";
	public static final String DOCUMENT_METADATA_KEY_SIGNING_ID = "signingId";
	public static final String DOCUMENT_METADATA_KEY_SIGNATORY = "signatory.";
	public static final String DOCUMENT_METADATA_KEY_SIGNING_STATUS = "signingStatus";
	public static final String DOCUMENT_METADATA_KEY_SIGNING_STATUS_MESSAGE = "signingStatusMessage";

}
