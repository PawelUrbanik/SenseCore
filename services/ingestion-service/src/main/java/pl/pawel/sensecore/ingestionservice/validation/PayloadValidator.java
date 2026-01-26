package pl.pawel.sensecore.ingestionservice.validation;

import org.springframework.stereotype.Component;
import pl.pawel.sensecore.ingestionservice.api.dto.TelemetryIngestRequest;

import java.math.BigDecimal;

@Component
public class PayloadValidator {

    private static final BigDecimal TEMPERATURE_MIN = new BigDecimal("-50");
    private static final BigDecimal TEMPERATURE_MAX = new BigDecimal("100");

    public void validateTemperature(TelemetryIngestRequest request) {
        if (request.value() == null) {
            throw new IllegalArgumentException("value is required");
        }
        if (request.value().compareTo(TEMPERATURE_MIN) < 0 || request.value().compareTo(TEMPERATURE_MAX) > 0) {
            throw  new IllegalArgumentException("temperature out of range: MIN: " + TEMPERATURE_MIN + " or MAX: " + TEMPERATURE_MAX);
        }
    }
}
