package pl.pawel.sensecore.ingestionservice.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessagePostProcessor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import pl.pawel.sensecore.contracts.TelemetryEvent;
import pl.pawel.sensecore.ingestionservice.messaging.properties.RabbitProps;
import pl.pawel.sensecore.ingestionservice.security.ClientIdentity;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TelemetryPublisher {
    private static final String HEADER_CLIENT_IP = "x-client-ip";
    private static final String HEADER_REQUEST_ID = "x-request-id";
    private static final String HEADER_TRACE_ID = "x-trace-id";
    private static final String HEADER_DEVICE_ID = "x-device-id";

    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_TRACE_ID = "traceId";
    private static final String MDC_DEVICE_ID = "deviceId";

    private final AmqpTemplate amqp;
    private final RabbitProps props;

    public TelemetryPublisher(AmqpTemplate amqp, RabbitProps props) {
        this.amqp = amqp;
        this.props = props;
    }


    public void publishTelemetryEvent(TelemetryEvent event, ClientIdentity identity) {

        Map<String, Object> headers = new HashMap<>();
        if (identity.ip() != null) {
            headers.put(HEADER_CLIENT_IP, identity.ip());
        }
        putIfPresent(headers, HEADER_REQUEST_ID, MDC.get(MDC_REQUEST_ID));
        putIfPresent(headers, HEADER_TRACE_ID, MDC.get(MDC_TRACE_ID));
        putIfPresent(headers, HEADER_DEVICE_ID, MDC.get(MDC_DEVICE_ID));

        MessagePostProcessor mpp = msg -> {
            headers.forEach((k, v) -> msg.getMessageProperties().setHeader(k, v));
            return msg;
        };

        String exchange = props.telemetryExchange();
        String routingKey = props.temperatureRoutingKey();

        try {
            amqp.convertAndSend(exchange, routingKey, event, mpp);
            log.debug(
                    "Telemetry event published: exchange={}, routingKey={}, deviceId={}, sensorType={}",
                    exchange,
                    routingKey,
                    event.deviceId(),
                    event.sensorType()
            );
        } catch (AmqpException ex) {
            log.error(
                    "Failed to publish telemetry event: exchange={}, routingKey={}, deviceId={}, sensorType={}, clientIp={}",
                    exchange,
                    routingKey,
                    event.deviceId(),
                    event.sensorType(),
                    identity.ip(),
                    ex
            );
            throw ex;
        }
    }

    private void putIfPresent(Map<String, Object> headers, String key, String value) {
        if (value != null && !value.isBlank()) {
            headers.put(key, value);
        }
    }
}
