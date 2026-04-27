package pl.pawel.sensecore.ingestionservice.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import pl.pawel.sensecore.contracts.TelemetryEvent;
import pl.pawel.sensecore.ingestionservice.api.dto.TelemetryIngestRequest;
import pl.pawel.sensecore.ingestionservice.device.DeviceDto;
import pl.pawel.sensecore.ingestionservice.device.DeviceRegistryService;
import pl.pawel.sensecore.ingestionservice.enrich.TelemetryEnricher;
import pl.pawel.sensecore.ingestionservice.messaging.TelemetryPublisher;
import pl.pawel.sensecore.ingestionservice.security.ClientIdentity;
import pl.pawel.sensecore.ingestionservice.validation.PayloadValidator;

@Slf4j
@Service
public class IngestionService {
    private static final String MDC_DEVICE_ID = "deviceId";

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
        DeviceDto device = deviceRegistryServices.resolveActiveDeviceByFingerprint(identity.fingerprint());
        String previousDeviceId = MDC.get(MDC_DEVICE_ID);
        MDC.put(MDC_DEVICE_ID, device.deviceId());
        try {
            log.debug("Resolved active deviceId={}", device.deviceId());

            payloadValidator.validateTemperature(request);

            TelemetryEvent event = telemetryEnricher.toTelemetryEvent(device, request);
            log.debug(
                    "Publishing telemetry event: deviceId={}, sensorType={}, timestamp={}",
                    event.deviceId(),
                    event.sensorType(),
                    event.timestamp()
            );
            telemetryPublisher.publishTelemetryEvent(event, identity);
        } finally {
            restoreMdcValue(MDC_DEVICE_ID, previousDeviceId);
        }
    }

    private String maskFingerprint(String fingerprint) {
        if (fingerprint == null || fingerprint.isBlank()) {
            return "n/a";
        }
        return fingerprint.length() <= 8 ? fingerprint : fingerprint.substring(0, 8);
    }

    private void restoreMdcValue(String key, String previousValue) {
        if (previousValue == null) {
            MDC.remove(key);
            return;
        }
        MDC.put(key, previousValue);
    }
}
