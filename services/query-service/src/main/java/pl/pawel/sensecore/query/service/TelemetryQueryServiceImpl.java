package pl.pawel.sensecore.query.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pl.pawel.sensecore.query.model.TelemetryReadingDto;
import pl.pawel.sensecore.query.model.TelemetryReadingMapper;
import pl.pawel.sensecore.query.repository.TelemetryReadingRepository;

import java.time.Instant;
import java.util.List;

@Log4j2
@Service
public class TelemetryQueryServiceImpl implements TelemetryQueryService {

    private final TelemetryReadingRepository readingRepository;
    private final TelemetryReadingMapper mapper;

    public TelemetryQueryServiceImpl(TelemetryReadingRepository readingRepository, TelemetryReadingMapper mapper) {
        this.readingRepository = readingRepository;
        this.mapper = mapper;
    }


    @Override
    public TelemetryReadingDto latest(String deviceId, String sensorType) {
        log.debug("Reading latest telemetry event with deviceId: " + deviceId + " and sensorType: " + sensorType);
        return readingRepository.findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(deviceId, normalizeSensorType(sensorType))
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public List<TelemetryReadingDto> history(String deviceId, String sensorType, Instant from, Instant to, int limit) {
        int safeLimit = (limit <= 0 || limit > 2000) ? 200 : limit;
        log.debug("Reading history events with deviceId: "  + deviceId + ", sensorType: " + sensorType + ", from " + from +", to " + to + ", limit " + safeLimit);
        return readingRepository.findHistory(deviceId, normalizeSensorType(sensorType), from, to, PageRequest.of(0, safeLimit))
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    private String normalizeSensorType(String sensorType) {
        return sensorType == null ? null : sensorType.trim().toLowerCase();
    }
}
