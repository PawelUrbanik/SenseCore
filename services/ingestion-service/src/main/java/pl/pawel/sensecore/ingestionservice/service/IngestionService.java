package pl.pawel.sensecore.ingestionservice.service;

import org.springframework.stereotype.Service;
import pl.pawel.sensecore.contracts.TelemetryEvent;
import pl.pawel.sensecore.ingestionservice.api.dto.TelemetryIngestRequest;
import pl.pawel.sensecore.ingestionservice.device.DeviceRegistryService;
import pl.pawel.sensecore.ingestionservice.enrich.TelemetryEnricher;
import pl.pawel.sensecore.ingestionservice.messaging.TelemetryPublisher;
import pl.pawel.sensecore.ingestionservice.security.ClientIdentity;
import pl.pawel.sensecore.ingestionservice.validation.PayloadValidator;
import pl.pawel.sensecore.persistence.entity.Device;

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
        Device device = deviceRegistryServices.resolveActiveDeviceByFingerprint(identity.fingerprint());

        payloadValidator.validateTemperature(request);

        TelemetryEvent event = telemetryEnricher.toTelemetryEvent(device, request);
        telemetryPublisher.publishTelemetryEvent(event, identity);
    }
}
