package se.sundsvall.esigning.businesslogic.util;

import static java.util.Collections.emptyMap;

import java.net.URI;
import org.springframework.web.util.DefaultUriBuilderFactory;

public final class UriUtility {
	private UriUtility() {}

	public static URI addProcessIdParameter(String url, String processId) {
		return new DefaultUriBuilderFactory(url)
			.builder()
			.queryParam("processId", processId)
			.build(emptyMap());
	}
}
