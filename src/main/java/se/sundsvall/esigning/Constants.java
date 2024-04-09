package se.sundsvall.esigning;

public class Constants {

	private Constants() {}

	public static final String PROCESS_KEY = "process-e-signing"; // Must match ID of process defined in bpmn schema
	public static final String TENANTID = "E_SIGNING"; // Namespace where process is deployed, a.k.a tenant (must match setting in application.yaml)
}
