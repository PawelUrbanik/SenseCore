package pl.pawel.sensecore.ingestionservice.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record TelemetryIngestRequest(
        @NotNull(message = "value is required")
        BigDecimal value,
        @NotBlank(message = "unit is required")
        String unit,
        Instant timestamp
) {}
