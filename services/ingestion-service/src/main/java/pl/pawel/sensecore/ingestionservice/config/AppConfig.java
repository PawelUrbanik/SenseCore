package pl.pawel.sensecore.ingestionservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {


    @Bean
    RestClient deviceRegistryRestClient(
            @Value("${sensecore.device-registry.url}") String deviceRegistryUrl) {
        return RestClient.builder()
                .baseUrl(deviceRegistryUrl)
                .build();
    }
}
