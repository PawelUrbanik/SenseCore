package pl.pawel.sensecore.ingestionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pl.pawel.sensecore.ingestionservice.messaging.properties.RabbitProps;

@SpringBootApplication
@EntityScan(
        basePackages = "pl.pawel.sensecore.persistence.entity"
)
@EnableConfigurationProperties(RabbitProps.class)
public class IngestionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IngestionServiceApplication.class, args);
    }

}
