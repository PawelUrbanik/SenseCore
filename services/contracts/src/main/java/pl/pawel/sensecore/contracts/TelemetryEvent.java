package pl.pawel.sensecore.contracts;

import java.math.BigDecimal;
import java.time.Instant;

public record TelemetryEvent(
        String schemaVersion,
        String deviceId,
        SensorType sensorType,
        BigDecimal value,
        Unit unit,
        Instant timestamp
) {}