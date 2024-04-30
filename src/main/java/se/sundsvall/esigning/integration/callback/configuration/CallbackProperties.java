package se.sundsvall.esigning.integration.callback.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.callback")
public record CallbackProperties(int connectTimeout, int readTimeout) {
}
