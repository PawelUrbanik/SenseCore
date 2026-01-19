package pl.pawel.sensecore.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(
        basePackages = "pl.pawel.sensecore.persistence.entity"
)
public class ProcessorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcessorServiceApplication.class, args);
    }

}
