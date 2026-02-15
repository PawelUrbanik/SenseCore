package pl.pawel.sensecore.processor.service;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.stereotype.Service;
import pl.pawel.sensecore.contracts.TelemetryEvent;
import pl.pawel.sensecore.persistence.entity.TelemetryReading;
import pl.pawel.sensecore.processor.repository.DeviceRepository;
import pl.pawel.sensecore.processor.repository.TelemetryReadingRepository;

import java.time.Instant;

import static org.apache.logging.log4j.util.Strings.isBlank;

@Service
@Log4j2
public class TelemetryProcessorServiceImpl implements TelemetryProcessorService {

    private final DeviceRepository deviceRepository;
    private final TelemetryReadingRepository telemetryReadingRepository;

    public TelemetryProcessorServiceImpl(DeviceRepository deviceRepository, TelemetryReadingRepository telemetryReadingRepository) {
        this.deviceRepository = deviceRepository;
        this.telemetryReadingRepository = telemetryReadingRepository;
    }

    @Override
    @Transactional
    public void process(TelemetryEvent event) {
        validate(event);

        log.debug("Looking for device with id: " + event.deviceId());
        deviceRepository.findByDeviceId(event.deviceId()).orElseThrow(
                () -> new AmqpRejectAndDontRequeueException("Invalid device id")
        );
        log.debug("Device found");

        log.debug("Creating Telemetry reading entity");
        TelemetryReading telemetryReading = new TelemetryReading();
        telemetryReading.setDeviceId(event.deviceId());
        telemetryReading.setSensorType(event.sensorType().getValue());
        telemetryReading.setValueNumeric(event.value());
        telemetryReading.setUnit(event.unit().getSymbol());
        telemetryReading.setTimestamp(event.timestamp());
        telemetryReading.setReceivedAt(Instant.now());
        log.debug("Created Telemetry reading entity: " + telemetryReading);

        log.debug("Saving entity to DB");
        telemetryReadingRepository.save(telemetryReading);
    }

    private void validate(TelemetryEvent event) {
        log.debug("Validating event");
        if (event == null) throw new AmqpRejectAndDontRequeueException("event is null");
        if (event.schemaVersion() == null || isBlank(event.schemaVersion()))
            throw new AmqpRejectAndDontRequeueException("schemaVersion missing");
        if (event.deviceId() == null || isBlank(event.deviceId()))
            throw new AmqpRejectAndDontRequeueException("deviceId missing");
        if (event.sensorType() == null)
            throw new AmqpRejectAndDontRequeueException("sensorType missing");
        if (event.unit() == null)
            throw new AmqpRejectAndDontRequeueException("unit missing");
        if (event.value() == null)
            throw new AmqpRejectAndDontRequeueException("value missing");
        if (event.timestamp() == null)
            throw new AmqpRejectAndDontRequeueException("timestamp missing");

        if (!event.sensorType().isUnitCompatible(event.unit())) {
            throw new AmqpRejectAndDontRequeueException("timestamp missing");
        }
    }
}
