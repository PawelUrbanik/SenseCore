package pl.pawel.sensecore.ingestionservice.enrich;

import org.springframework.stereotype.Component;
import pl.pawel.sensecore.contracts.SensorType;
import pl.pawel.sensecore.contracts.TelemetryEvent;
import pl.pawel.sensecore.contracts.Unit;
import pl.pawel.sensecore.ingestionservice.api.dto.TelemetryIngestRequest;
import pl.pawel.sensecore.persistence.entity.Device;

import java.time.Instant;

@Component
public class TelemetryEnricher {
    private final String SCHEMA_VERSION = "v!";


    public TelemetryEvent toTelemetryEvent(Device device, TelemetryIngestRequest request) {
        Instant ts = (request.timestamp() != null) ? request.timestamp() : Instant.now();
        Unit unit = parseUnit(request.unit());
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
