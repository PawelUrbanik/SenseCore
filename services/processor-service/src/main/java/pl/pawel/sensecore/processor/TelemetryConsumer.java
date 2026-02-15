package pl.pawel.sensecore.processor;

import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pl.pawel.sensecore.contracts.TelemetryEvent;
import pl.pawel.sensecore.processor.service.TelemetryProcessorService;

@Component
@Log4j2
public class TelemetryConsumer {
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
