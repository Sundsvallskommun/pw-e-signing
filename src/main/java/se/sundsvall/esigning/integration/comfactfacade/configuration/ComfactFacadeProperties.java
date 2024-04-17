package se.sundsvall.esigning.integration.comfactfacade.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.comfactfacade")
public record ComfactFacadeProperties(int connectTimeout, int readTimeout) {
}
