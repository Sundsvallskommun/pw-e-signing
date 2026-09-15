package se.sundsvall.esigning.integration.operaton.configuration;

import org.camunda.bpm.client.interceptor.ClientRequestContext;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

import static java.util.Objects.isNull;
import static se.sundsvall.esigning.integration.operaton.configuration.OperatonConfiguration.CLIENT_ID;

/**
 * Adds a WSO2 client-credentials bearer token to every external task request (fetchAndLock/complete/handleFailure) so
 * the Operaton instance can poll api-service-operaton, which sits behind the OAuth2-secured gateway. Only registered
 * when this instance targets Operaton (see {@link OperatonExternalTaskClientConfiguration}); the Camunda instance polls
 * its engine directly without a token.
 */
class OperatonExternalTaskAuthInterceptor implements ClientRequestInterceptor {

	private static final String PRINCIPAL = "operaton-external-task-client";

	private final OAuth2AuthorizedClientManager authorizedClientManager;
	private final OAuth2AuthorizedClientService authorizedClientService;

	OperatonExternalTaskAuthInterceptor(final OAuth2AuthorizedClientManager authorizedClientManager, final OAuth2AuthorizedClientService authorizedClientService) {
		this.authorizedClientManager = authorizedClientManager;
		this.authorizedClientService = authorizedClientService;
	}

	@Override
	public void intercept(final ClientRequestContext requestContext) {
		// Evict any cached token before authorizing so each poll forces a freshly issued token. The manager only renews near
		// the nominal expiry, and an interceptor never sees the response, so it cannot detect a 401 and evict a token that
		// WSO2 invalidated server-side early (gateway restart, revocation, clock skew). Removing it up front sidesteps that.
		authorizedClientService.removeAuthorizedClient(CLIENT_ID, PRINCIPAL);

		final var authorizedClient = authorizedClientManager.authorize(
			OAuth2AuthorizeRequest.withClientRegistrationId(CLIENT_ID).principal(PRINCIPAL).build());

		if (isNull(authorizedClient)) {
			throw new IllegalStateException("Could not obtain a WSO2 client-credentials token for client registration '" + CLIENT_ID + "'; check the OAuth2 client configuration.");
		}

		requestContext.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.getAccessToken().getTokenValue());
	}
}
