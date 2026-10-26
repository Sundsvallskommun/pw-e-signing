package se.sundsvall.esigning.integration.engine.externaltask;

import feign.RequestTemplate;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.client.exception.ConnectionLostException;
import org.camunda.bpm.client.exception.EngineException;
import org.camunda.bpm.client.exception.RestException;
import org.camunda.bpm.client.exception.UnknownHttpErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.util.ReflectionTestUtils;
import se.sundsvall.dept44.configuration.feign.interceptor.OAuth2RequestInterceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;

class OperatonExternalTaskTokenTest {

	private static final ClientRegistration REGISTRATION = ClientRegistration.withRegistrationId("operaton")
		.clientId("the-client-id")
		.clientSecret("the-client-secret")
		.authorizationGrantType(CLIENT_CREDENTIALS)
		.tokenUri("http://localhost/token")
		.scope("openid")
		.build();

	private final List<ClientRegistration> requestedRegistrations = new ArrayList<>();

	private OperatonExternalTaskToken token;

	@BeforeEach
	void setUp() {
		token = createToken(requestedRegistrations);
	}

	@Test
	void reusesCachedTokenWithoutNewTokenRequest() {
		assertThat(token.getAuthorizationHeader()).isEqualTo("Bearer token-1");
		assertThat(token.getAuthorizationHeader()).isEqualTo("Bearer token-1");
		assertThat(token.getAuthorizationHeader()).isEqualTo("Bearer token-1");

		assertThat(requestedRegistrations).hasSize(1);
	}

	@Test
	void fetchesNewTokenAfterEvict() {
		assertThat(token.getAuthorizationHeader()).isEqualTo("Bearer token-1");

		token.evict();

		assertThat(token.getAuthorizationHeader()).isEqualTo("Bearer token-2");
		assertThat(token.getAuthorizationHeader()).isEqualTo("Bearer token-2");
		assertThat(requestedRegistrations).hasSize(2);
	}

	@Test
	void evictBeforeAnyTokenIsNoop() {
		token.evict();

		assertThat(token.getAuthorizationHeader()).isEqualTo("Bearer token-1");
		assertThat(requestedRegistrations).hasSize(1);
	}

	@Test
	void evictKeepsTokenFetchedAfterTheOneLastHandedOut() {
		// A token handed out by another component (e.g. after an expiry) replaces the cached one behind our back
		assertThat(token.getAuthorizationHeader()).isEqualTo("Bearer token-1");
		final var oAuth2RequestInterceptor = (OAuth2RequestInterceptor) ReflectionTestUtils.getField(token, "oAuth2RequestInterceptor");
		oAuth2RequestInterceptor.removeToken("Bearer token-1");
		final var template = new RequestTemplate();
		oAuth2RequestInterceptor.apply(template);
		assertThat(template.headers().get("Authorization")).containsExactly("Bearer token-2");

		// Evicting the token last handed out by this component must not remove the newer one
		token.evict();

		assertThat(token.getAuthorizationHeader()).isEqualTo("Bearer token-2");
		assertThat(requestedRegistrations).hasSize(2);
	}

	@Test
	void requestsTokenWithRegistrationScopeAndOneDeviceScope() {
		token.getAuthorizationHeader();

		assertThat(requestedRegistrations).hasSize(1);
		assertThat(requestedRegistrations.getFirst().getRegistrationId()).isEqualTo("operaton");
		assertThat(requestedRegistrations.getFirst().getScopes())
			.hasSize(2)
			.contains("openid")
			.filteredOn(scope -> scope.startsWith("device_"))
			.singleElement()
			.satisfies(scope -> assertThat(scope).matches("device_[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
	}

	@Test
	void twoInstancesRequestDifferentDeviceScopes() {
		final var otherRequestedRegistrations = new ArrayList<ClientRegistration>();
		final var otherToken = createToken(otherRequestedRegistrations);

		token.getAuthorizationHeader();
		otherToken.getAuthorizationHeader();

		assertThat(deviceScope(requestedRegistrations.getFirst())).isNotEqualTo(deviceScope(otherRequestedRegistrations.getFirst()));
	}

	@Test
	void isUnauthorizedForDirect401() {
		assertThat(OperatonExternalTaskToken.isUnauthorized(restException(401))).isTrue();
	}

	@Test
	void isUnauthorizedFor401WrappedAsCause() {
		assertThat(OperatonExternalTaskToken.isUnauthorized(new UnknownHttpErrorException("Unauthorized", restException(401)))).isTrue();
	}

	@Test
	void isNotUnauthorizedFor500() {
		assertThat(OperatonExternalTaskToken.isUnauthorized(new EngineException("Internal error", restException(500)))).isFalse();
	}

	@Test
	void isNotUnauthorizedForMissingStatus() {
		assertThat(OperatonExternalTaskToken.isUnauthorized(new RestException("No status", "type", 0))).isFalse();
	}

	@Test
	void isNotUnauthorizedForConnectionLost() {
		assertThat(OperatonExternalTaskToken.isUnauthorized(new ConnectionLostException("Connection lost", new IOException()))).isFalse();
	}

	@Test
	void isNotUnauthorizedForNull() {
		assertThat(OperatonExternalTaskToken.isUnauthorized(null)).isFalse();
	}

	private static OperatonExternalTaskToken createToken(final List<ClientRegistration> requested) {
		final var newToken = new OperatonExternalTaskToken(new InMemoryClientRegistrationRepository(REGISTRATION));

		// Stands in for the token endpoint: issues a new token unless one is already cached, as the client credentials
		// provider does for a token that has not expired
		setAuthorizedClientProvider(newToken, context -> {
			if (context.getAuthorizedClient() != null) {
				return null;
			}
			requested.add(context.getClientRegistration());
			final var issuedAt = Instant.now();
			final var accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token-" + requested.size(), issuedAt, issuedAt.plusSeconds(3600));
			return new OAuth2AuthorizedClient(context.getClientRegistration(), context.getPrincipal().getName(), accessToken);
		});
		return newToken;
	}

	private static void setAuthorizedClientProvider(final OperatonExternalTaskToken target, final OAuth2AuthorizedClientProvider provider) {
		final var oAuth2RequestInterceptor = ReflectionTestUtils.getField(target, "oAuth2RequestInterceptor");
		final var manager = (AuthorizedClientServiceOAuth2AuthorizedClientManager) ReflectionTestUtils.getField(oAuth2RequestInterceptor, "authorizedClientManager");
		manager.setAuthorizedClientProvider(provider);
	}

	private static String deviceScope(final ClientRegistration registration) {
		return registration.getScopes().stream().filter(scope -> scope.startsWith("device_")).findFirst().orElseThrow();
	}

	private static RestException restException(final int httpStatusCode) {
		final var restException = new RestException("message", "type", 0);
		restException.setHttpStatusCode(httpStatusCode);
		return restException;
	}
}
