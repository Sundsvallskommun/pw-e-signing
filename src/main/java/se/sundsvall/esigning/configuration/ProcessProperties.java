package se.sundsvall.esigning.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("camunda.process")
public record ProcessProperties(String waitDuration) {
}
