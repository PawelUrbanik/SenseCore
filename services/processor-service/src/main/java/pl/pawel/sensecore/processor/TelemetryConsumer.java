package pl.pawel.sensecore.processor;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pl.pawel.sensecore.contracts.TelemetryEvent;

@Component
public class TelemetryConsumer {


    @RabbitListener(queues = "${SENSECORE_TEMPERATURE_QUEUE:sensecore.telemetry.temperature}")
    public void onMessage(TelemetryEvent payload) {
        System.out.println("Received: " + payload);
    }
}
