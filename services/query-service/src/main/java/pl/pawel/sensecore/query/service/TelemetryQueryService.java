package pl.pawel.sensecore.query.service;

import pl.pawel.sensecore.query.model.TelemetryReadingDto;

import java.time.Instant;
import java.util.List;

public interface TelemetryQueryService {
    public TelemetryReadingDto latest(String deviceId, String sensorType);
    public List<TelemetryReadingDto> history(String deviceId, String sensorType, Instant from, Instant to, int limit);
}
