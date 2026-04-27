package pl.pawel.sensecore.processor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    @Bean
    public RestClient deviceRestClient(
            @Value("${sensecore.device-registry.url:http://localhost:8084}") String deviceRegistryUrl){
        return RestClient.builder()
                .baseUrl(deviceRegistryUrl)
                .build();
    }
}
