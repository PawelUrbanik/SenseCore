package pl.pawel.sensecore.query.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan(
        basePackages = "pl.pawel.sensecore.persistence.entity"
)
public class AppConfig {
}
