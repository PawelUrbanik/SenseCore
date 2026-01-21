package pl.pawel.sensecore.query.model;


import java.math.BigDecimal;
import java.time.Instant;

public record TelemetryReadingDto(String deviceId, String sensorType, BigDecimal value, String unit, Instant timestamp) {}
