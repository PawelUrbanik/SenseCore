package pl.pawel.sensecore.query.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.pawel.sensecore.query.model.TelemetryReadingDto;
import pl.pawel.sensecore.query.service.TelemetryQueryService;

import java.time.Instant;
import java.util.List;

@Validated
@RestController
@RequestMapping("/telemetry")
public class TelemetryReadingController {

    private final TelemetryQueryService telemetryQueryService;

    public TelemetryReadingController(TelemetryQueryService telemetryQueryService) {
        this.telemetryQueryService = telemetryQueryService;
    }


    @GetMapping("/latest")
    public ResponseEntity<TelemetryReadingDto> latest(
            @RequestParam("deviceId") @NotBlank(message = "deviceId is required") String deviceId,
            @RequestParam("sensorType") @NotBlank(message = "sensorType is required") String sensorType) {
        TelemetryReadingDto latest = telemetryQueryService.latest(deviceId, sensorType);

        return latest == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(latest);
    }

    @GetMapping("/history")
    public List<TelemetryReadingDto> history(
            @RequestParam("deviceId") @NotBlank(message = "deviceId is required") String deviceId,
            @RequestParam("sensorType") @NotBlank(message = "sensorType is required") String sensorType,
            @RequestParam("from") @NotNull(message = "from is required") Instant from,
            @RequestParam("to") @NotNull(message = "to is required") Instant to,
            @RequestParam(defaultValue = "200", name = "limit") @Min(value = 1, message = "limit must be at least 1") int limit) {
        return telemetryQueryService.history(deviceId, sensorType, from, to, limit);
    }
}
