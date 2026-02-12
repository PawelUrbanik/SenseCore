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
        log.debug("Looking for device with fingerprint: " + identity.fingerprint());
        Device device = deviceRegistryServices.resolveActiveDeviceByFingerprint(identity.fingerprint());
        log.debug("Found device: " + device);

        payloadValidator.validateTemperature(request);

        TelemetryEvent event = telemetryEnricher.toTelemetryEvent(device, request);
        log.debug("Publishing event: " + event);
        telemetryPublisher.publishTelemetryEvent(event, identity);
    }
}
