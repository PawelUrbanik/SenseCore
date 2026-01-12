package pl.pawel.sensecore.processor;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TelemetryConsumer {

    @RabbitListener(queues = "${SENSECORE_TEMPERATURE_QUEUE:sensecore.telemetry.temperature}")
    public void onMessage(String payload) {
        System.out.println("Received: " + payload);
    }
}
