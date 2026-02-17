package pl.pawel.sensecore.processor;

import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;
import pl.pawel.sensecore.contracts.TelemetryEvent;
import pl.pawel.sensecore.processor.service.TelemetryProcessorService;

import java.util.Map;

@Component
@Log4j2
public class TelemetryConsumer {
    private static final String HEADER_REQUEST_ID = "x-request-id";
    private static final String HEADER_TRACE_ID = "x-trace-id";

    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_TRACE_ID = "traceId";
    private static final String MDC_DEVICE_ID = "deviceId";

    private final TelemetryProcessorService telemetryProcessorService;

    public TelemetryConsumer(TelemetryProcessorService telemetryProcessorService) {
        this.telemetryProcessorService = telemetryProcessorService;
    }

    @RabbitListener(queues = "${SENSECORE_TEMPERATURE_QUEUE:sensecore.telemetry.temperature}")
    public void onMessage(TelemetryEvent payload, @Headers Map<String, Object> headers) {
        String previousRequestId = MDC.get(MDC_REQUEST_ID);
        String previousTraceId = MDC.get(MDC_TRACE_ID);
        String previousDeviceId = MDC.get(MDC_DEVICE_ID);
        try {
            putIfPresent(MDC_REQUEST_ID, headerValue(headers, HEADER_REQUEST_ID));
            putIfPresent(MDC_TRACE_ID, headerValue(headers, HEADER_TRACE_ID));
            putIfPresent(MDC_DEVICE_ID, payload.deviceId());

            log.debug("Received telemetry: deviceId={}, sensorType={}, value={}, unit={}, ts={}",
                    payload.deviceId(), payload.sensorType(), payload.value(), payload.unit(), payload.timestamp());

            telemetryProcessorService.process(payload);
        } finally {
            restoreMdcValue(MDC_REQUEST_ID, previousRequestId);
            restoreMdcValue(MDC_TRACE_ID, previousTraceId);
            restoreMdcValue(MDC_DEVICE_ID, previousDeviceId);
        }
    }

    private String headerValue(Map<String, Object> headers, String name) {
        Object value = headers.get(name);
        if (value == null) {
            return null;
        }
        return String.valueOf(value).trim();
    }

    private void putIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value);
        }
    }

    private void restoreMdcValue(String key, String previousValue) {
        if (previousValue == null) {
            MDC.remove(key);
            return;
        }
        MDC.put(key, previousValue);
    }
}
