package pl.pawel.sensecore.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pl.pawel.sensecore.contracts.TelemetryEvent;
import pl.pawel.sensecore.processor.service.TelemetryProcessorService;

@Component
public class TelemetryConsumer {

    private static final Logger log = LoggerFactory.getLogger(TelemetryConsumer.class);
    private final TelemetryProcessorService telemetryProcessorService;

    public TelemetryConsumer(TelemetryProcessorService telemetryProcessorService) {
        this.telemetryProcessorService = telemetryProcessorService;
    }

    @RabbitListener(queues = "${SENSECORE_TEMPERATURE_QUEUE:sensecore.telemetry.temperature}")
    public void onMessage(TelemetryEvent payload) {
        log.info("Received telemetry: deviceId={}, sensorType={}, value={}, unit={}, ts={}",
                payload.deviceId(), payload.sensorType(), payload.value(), payload.unit(), payload.timestamp());

        telemetryProcessorService.process(payload);
    }
}
