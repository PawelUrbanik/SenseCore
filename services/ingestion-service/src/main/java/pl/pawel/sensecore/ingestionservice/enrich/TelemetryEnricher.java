package pl.pawel.sensecore.ingestionservice.enrich;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import pl.pawel.sensecore.contracts.SensorType;
import pl.pawel.sensecore.contracts.TelemetryEvent;
import pl.pawel.sensecore.contracts.Unit;
import pl.pawel.sensecore.ingestionservice.api.dto.TelemetryIngestRequest;
import pl.pawel.sensecore.persistence.entity.Device;

import java.time.Instant;

@Component
@Log4j2
public class TelemetryEnricher {
    private static final String SCHEMA_VERSION = "v1";


    public TelemetryEvent toTelemetryEvent(Device device, TelemetryIngestRequest request) {
        log.debug("Fulfilling timestamp if empty in request");
        Instant ts = (request.timestamp() != null) ? request.timestamp() : Instant.now();
        log.debug("Parsing unit={}", request.unit());
        Unit unit = parseUnit(request.unit());

        log.debug("Resolving sensor type for unit={}", unit);
        SensorType sensorType = getSensorType(unit);

        return new TelemetryEvent(SCHEMA_VERSION,  device.getDeviceId(), sensorType, request.value(), unit, ts);
    }

    private Unit parseUnit(String unit) {
        return switch (unit) {
            case "C" -> Unit.CELSIUS;
            case "%" -> Unit.PERCENT;
            case "hPa" -> Unit.HECTOPASCAL;
            case "ppm" -> Unit.PPM;
            default -> throw new IllegalArgumentException("Invalid Unit");
        };
    }

    private SensorType getSensorType(Unit unit){
        return switch (unit) {
            case CELSIUS -> SensorType.TEMPERATURE;
            case PERCENT -> SensorType.HUMIDITY;
            case HECTOPASCAL -> SensorType.PRESSURE;
            case PPM -> SensorType.CO2;
            default -> throw new IllegalArgumentException("Invalid Unit");
        };
    }


}
