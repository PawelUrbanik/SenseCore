package pl.pawel.sensecore.ingestionservice.validation;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import pl.pawel.sensecore.ingestionservice.api.dto.TelemetryIngestRequest;

import java.math.BigDecimal;

@Component
@Log4j2
public class PayloadValidator {

    private static final BigDecimal TEMPERATURE_MIN = new BigDecimal("-50");
    private static final BigDecimal TEMPERATURE_MAX = new BigDecimal("100");

    public void validateTemperature(TelemetryIngestRequest request) {
        log.debug("Validating temperature value");
        if (request.value() == null) {
            log.error("Temperature is null");
            throw new IllegalArgumentException("value is required");
        }
        log.debug("Validating temperature range, min="+ TEMPERATURE_MIN + ", max=" + TEMPERATURE_MAX + ", actual="+ request.value());
        if (request.value().compareTo(TEMPERATURE_MIN) < 0 || request.value().compareTo(TEMPERATURE_MAX) > 0) {
            log.error("Temperature out of range!");
            throw  new IllegalArgumentException("temperature out of range: MIN: " + TEMPERATURE_MIN + " or MAX: " + TEMPERATURE_MAX);
        }
    }
}
