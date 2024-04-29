package se.sundsvall.esigning.integration.document.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.document")
public record DocumentProperties(int connectTimeout, int readTimeout) {
}
