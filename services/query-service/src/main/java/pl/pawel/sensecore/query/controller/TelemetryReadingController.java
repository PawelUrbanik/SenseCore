package pl.pawel.sensecore.query.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.pawel.sensecore.query.model.TelemetryReadingDto;
import pl.pawel.sensecore.query.service.TelemetryQueryService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/telemetry")
public class TelemetryReadingController {

    private final TelemetryQueryService telemetryQueryService;

    public TelemetryReadingController(TelemetryQueryService telemetryQueryService) {
        this.telemetryQueryService = telemetryQueryService;
    }


    @GetMapping("/latest")
    public ResponseEntity<TelemetryReadingDto> latest(
            @RequestParam("deviceId") String deviceId,
            @RequestParam("sensorType") String sensorType) {
        TelemetryReadingDto latest = telemetryQueryService.latest(deviceId, sensorType);

        return latest == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(latest);
    }

    @GetMapping("/history")
    public List<TelemetryReadingDto> history(
            @RequestParam("deviceId") String deviceId,
            @RequestParam("sensorType") String sensorType,
            @RequestParam("from") Instant from,
            @RequestParam("to") Instant to,
            @RequestParam(defaultValue = "200", name = "limit") int limit) {
        return telemetryQueryService.history(deviceId, sensorType, from, to, limit);
    }
}
