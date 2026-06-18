package se.sundsvall.esigning.integration.operaton.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.operaton")
public record OperatonProperties(int connectTimeout, int readTimeout) {
}
