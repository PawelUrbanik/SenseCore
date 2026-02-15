package pl.pawel.sensecore.ingestionservice.messaging;

import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.stereotype.Service;
import pl.pawel.sensecore.contracts.TelemetryEvent;
import pl.pawel.sensecore.ingestionservice.messaging.properties.RabbitProps;
import pl.pawel.sensecore.ingestionservice.security.ClientIdentity;

import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class TelemetryPublisher {

    private final AmqpTemplate amqp;
    private final RabbitProps props;

    public TelemetryPublisher(AmqpTemplate amqp, RabbitProps props) {
        this.amqp = amqp;
        this.props = props;
    }


    public void publishTelemetryEvent(TelemetryEvent event, ClientIdentity identity) {

        Map<String, Object> headers = new HashMap<>();
        if (identity.ip() != null) headers.put("x-client-ip", identity.ip());

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
}
