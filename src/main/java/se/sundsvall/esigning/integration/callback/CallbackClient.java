package se.sundsvall.esigning.integration.callback;

import static se.sundsvall.esigning.integration.callback.configuration.CallbackConfiguration.CLIENT_ID;

import java.net.URI;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import se.sundsvall.esigning.integration.callback.configuration.CallbackConfiguration;

@FeignClient(name = CLIENT_ID, url = "http://placeholder.url", configuration = CallbackConfiguration.class, dismiss404 = true)
public interface CallbackClient {

	@GetMapping
	public Void sendRequest(URI baseUrl);

}
