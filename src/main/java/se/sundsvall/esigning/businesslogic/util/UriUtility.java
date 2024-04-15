package se.sundsvall.esigning.businesslogic.util;

import org.springframework.web.util.DefaultUriBuilderFactory;

public final class UriUtility {
	private UriUtility() {}

	public static String addProcessIdParameter(String url, String processId) {
		return new DefaultUriBuilderFactory(url)
			.builder()
			.queryParam("processId", processId)
			.toUriString();
	}
}
