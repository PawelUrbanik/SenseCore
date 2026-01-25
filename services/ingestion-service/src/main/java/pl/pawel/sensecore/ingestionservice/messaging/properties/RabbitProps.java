package pl.pawel.sensecore.ingestionservice.messaging.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "sensecore.rabbit")
public record RabbitProps(String telemetryExchange, String temperatureRoutingKey) {}
