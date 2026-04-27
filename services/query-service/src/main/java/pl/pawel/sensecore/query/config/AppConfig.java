package pl.pawel.sensecore.query.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EntityScan(
        basePackages = "pl.pawel.sensecore.persistence.entity"
)
public class AppConfig {

    @Bean
    public RestClient deviceRegistryRestClient(
            @Value("${sensecore.device-registry.url:http://localhost:8084}") String deviceRegistryUrl) {
        return RestClient.builder()
                .baseUrl(deviceRegistryUrl)
                .build();
    }
}
