package se.sundsvall.esigning;

public final class Constants {

	private Constants() {}

	public static final String PROCESS_KEY = "process-e-signing"; // Must match ID of process defined in bpmn schema
	public static final String TENANTID = "E_SIGNING"; // Namespace where process is deployed, a.k.a tenant (must match setting in application.yaml)

	public static final String CAMUNDA_VARIABLE_REQUEST_ID = "requestId";
	public static final String CAMUNDA_VARIABLE_SIGNING_ID = "signingId";
	public static final String CAMUNDA_VARIABLE_ESIGNING_REQUEST = "eSigningRequest";
	public static final String CAMUNDA_VARIABLE_WAIT_DURATION = "waitDuration";
	public static final String CAMUNDA_VARIABLE_CALLBACK_PRESENT = "callbackPresent";
	public static final String CAMUNDA_VARIABLE_SIGNING_STATUS = "signStatus";
}
