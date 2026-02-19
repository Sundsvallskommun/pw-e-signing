package se.sundsvall.esigning.integration.callback;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.net.URI;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import se.sundsvall.esigning.integration.callback.configuration.CallbackConfiguration;

import static se.sundsvall.esigning.integration.callback.configuration.CallbackConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "http://placeholder.url", configuration = CallbackConfiguration.class, dismiss404 = true)
@CircuitBreaker(name = CLIENT_ID)
public interface CallbackClient {

	@GetMapping
	Void sendRequest(URI baseUrl);

}
