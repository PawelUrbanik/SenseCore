package pl.pawel.sensecore.ingestionservice.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TelemetryIngestRequest(
        BigDecimal value,
        String unit,
        Instant timestamp
) {}
