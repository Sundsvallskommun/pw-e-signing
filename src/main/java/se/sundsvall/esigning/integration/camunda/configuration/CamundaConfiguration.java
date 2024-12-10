package se.sundsvall.esigning.integration.camunda.configuration;

import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Import(FeignConfiguration.class)
public class CamundaConfiguration {

	public static final String CLIENT_ID = "camunda";

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(CamundaProperties properties) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new ProblemErrorDecoder(CLIENT_ID))
			.withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
			.composeCustomizersToOne();
	}
}
