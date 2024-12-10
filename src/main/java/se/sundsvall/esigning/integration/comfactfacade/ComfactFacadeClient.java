package se.sundsvall.esigning.integration.comfactfacade;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.esigning.integration.comfactfacade.configuration.ComfactFacadeConfiguration.CLIENT_ID;

import generated.se.sundsvall.comfactfacade.CreateSigningResponse;
import generated.se.sundsvall.comfactfacade.SigningInstance;
import generated.se.sundsvall.comfactfacade.SigningRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.esigning.integration.comfactfacade.configuration.ComfactFacadeConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.comfactfacade.url}", configuration = ComfactFacadeConfiguration.class)
public interface ComfactFacadeClient {

	@PostMapping(path = "/{municipalityId}/signings", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	CreateSigningResponse createSigngingInstance(@PathVariable("municipalityId") String municipalityId, @RequestBody SigningRequest signingRequest);

	@GetMapping(path = "/{municipalityId}/signings/{signingId}")
	SigningInstance getSigningInstance(@PathVariable("municipalityId") String municipalityId, @PathVariable("signingId") String signingId);
}
