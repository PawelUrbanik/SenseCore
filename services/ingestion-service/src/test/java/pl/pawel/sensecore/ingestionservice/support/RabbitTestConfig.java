package pl.pawel.sensecore.ingestionservice.support;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RabbitTestConfig {

    @Bean
    DirectExchange telemetryExchange(@Value("${sensecore.rabbit.telemetry-exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, false, false);
    }

    @Bean
    Queue telemetryQueue(@Value("${sensecore.rabbit.temperature-routing-key}") String routingKey) {
        return new Queue(routingKey, false);
    }

    @Bean
    Binding telemetryBinding(Queue telemetryQueue,
                             DirectExchange telemetryExchange,
                             @Value("${sensecore.rabbit.temperature-routing-key}") String routingKey) {
        return BindingBuilder.bind(telemetryQueue).to(telemetryExchange).with(routingKey);
    }
}
