package pl.pawel.sensecore.ingestionservice.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pl.pawel.sensecore.contracts.TelemetryEvent;
import pl.pawel.sensecore.ingestionservice.api.dto.TelemetryIngestRequest;
import pl.pawel.sensecore.ingestionservice.device.DeviceRegistryService;
import pl.pawel.sensecore.ingestionservice.enrich.TelemetryEnricher;
import pl.pawel.sensecore.ingestionservice.messaging.TelemetryPublisher;
import pl.pawel.sensecore.ingestionservice.security.ClientIdentity;
import pl.pawel.sensecore.ingestionservice.validation.PayloadValidator;
import pl.pawel.sensecore.persistence.entity.Device;

@Log4j2
@Service
public class IngestionService {

    private final DeviceRegistryService deviceRegistryServices;
    private final PayloadValidator payloadValidator;
    private final TelemetryEnricher telemetryEnricher;
    private final TelemetryPublisher telemetryPublisher;

    public IngestionService(DeviceRegistryService deviceRegistryServices, PayloadValidator payloadValidator, TelemetryEnricher telemetryEnricher, TelemetryPublisher telemetryPublisher) {
        this.deviceRegistryServices = deviceRegistryServices;
        this.payloadValidator = payloadValidator;
        this.telemetryEnricher = telemetryEnricher;
        this.telemetryPublisher = telemetryPublisher;
    }


    public void ingest(ClientIdentity identity, TelemetryIngestRequest request) {
        log.debug("Resolving active device by fingerprint prefix={}", maskFingerprint(identity.fingerprint()));
        Device device = deviceRegistryServices.resolveActiveDeviceByFingerprint(identity.fingerprint());
        log.debug("Resolved active deviceId={}", device.getDeviceId());

        payloadValidator.validateTemperature(request);

        TelemetryEvent event = telemetryEnricher.toTelemetryEvent(device, request);
        log.debug(
                "Publishing telemetry event: deviceId={}, sensorType={}, timestamp={}",
                event.deviceId(),
                event.sensorType(),
                event.timestamp()
        );
        telemetryPublisher.publishTelemetryEvent(event, identity);
    }

    private String maskFingerprint(String fingerprint) {
        if (fingerprint == null || fingerprint.isBlank()) {
            return "n/a";
        }
        return fingerprint.length() <= 8 ? fingerprint : fingerprint.substring(0, 8);
    }
}
