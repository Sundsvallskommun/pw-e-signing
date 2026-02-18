package se.sundsvall.esigning.businesslogic.util;

import java.net.URI;
import org.springframework.web.util.DefaultUriBuilderFactory;

import static java.util.Collections.emptyMap;

public final class UriUtility {
	private UriUtility() {}

	public static URI addProcessIdParameter(String url, String processId) {
		return new DefaultUriBuilderFactory(url)
			.builder()
			.queryParam("processId", processId)
			.build(emptyMap());
	}
}
