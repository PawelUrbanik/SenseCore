package pl.pawel.sensecore.processor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.pawel.sensecore.contracts.SensorType;
import pl.pawel.sensecore.contracts.TelemetryEvent;
import pl.pawel.sensecore.contracts.Unit;
import pl.pawel.sensecore.persistence.entity.Device;
import pl.pawel.sensecore.persistence.entity.TelemetryReading;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import pl.pawel.sensecore.processor.repository.DeviceRepository;
import pl.pawel.sensecore.processor.repository.TelemetryReadingRepository;

@ExtendWith(MockitoExtension.class)
class TelemetryProcessorServiceTest {
    private static final String CORRECT_DEVICE_ID = "arduino-esp-01";

    @Mock
    DeviceRepository deviceRepository;

    @Mock
    TelemetryReadingRepository telemetryReadingRepository;

    @InjectMocks
    TelemetryProcessorServiceImpl service;


    @Test
    void shouldProcessCorrectMessage(){
        Instant timestamp = Instant.now();
        Device device = new Device();
        when(deviceRepository.findByDeviceId(CORRECT_DEVICE_ID)).thenReturn(
                Optional.of(device)
        );
        ArgumentCaptor<TelemetryReading> captor = ArgumentCaptor.forClass(TelemetryReading.class);

        TelemetryEvent event = prepareEvent(timestamp);

        service.process(event);
        verify(telemetryReadingRepository).save(captor.capture());

        TelemetryReading saved = captor.getValue();
        System.out.println(saved);
        assertAll(
                () -> assertEquals(CORRECT_DEVICE_ID, saved.getDeviceId()),
                () -> assertEquals(event.sensorType().getValue().toLowerCase(), saved.getSensorType().toLowerCase()),
                () -> assertEquals(event.value(), saved.getValueNumeric()),
                () -> assertEquals(event.unit(), Unit.fromSymbol(saved.getUnit())),
                () -> assertEquals(event.timestamp(), saved.getTimestamp())
        );
    }

    @Test
    void shouldThrowExceptionWhenEventIsNull() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> service.process(null));
        verifyNoInteractions(deviceRepository, telemetryReadingRepository);
    }

    @Test
    void shouldThrowExceptionWhenSchemaVersionIsMissing() {
        Instant timestamp = Instant.now();
        TelemetryEvent event = new TelemetryEvent(
                null,
                CORRECT_DEVICE_ID,
                SensorType.TEMPERATURE,
                BigDecimal.valueOf(26),
                Unit.CELSIUS,
                timestamp
        );

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> service.process(event));
        verifyNoInteractions(deviceRepository, telemetryReadingRepository);
    }

    @Test
    void shouldThrowExceptionWhenSchemaVersionIsBlank() {
        Instant timestamp = Instant.now();
        TelemetryEvent event = new TelemetryEvent(
                "   ",
                CORRECT_DEVICE_ID,
                SensorType.TEMPERATURE,
                BigDecimal.valueOf(26),
                Unit.CELSIUS,
                timestamp
        );

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> service.process(event));
        verifyNoInteractions(deviceRepository, telemetryReadingRepository);
    }

    @Test
    void shouldThrowExceptionWhenDeviceIdIsMissing() {
        Instant timestamp = Instant.now();
        TelemetryEvent event = new TelemetryEvent(
                "v1",
                null,
                SensorType.TEMPERATURE,
                BigDecimal.valueOf(26),
                Unit.CELSIUS,
                timestamp
        );

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> service.process(event));
        verifyNoInteractions(deviceRepository, telemetryReadingRepository);
    }

    @Test
    void shouldThrowExceptionWhenDeviceIdIsBlank() {
        Instant timestamp = Instant.now();
        TelemetryEvent event = new TelemetryEvent(
                "v1",
                "   ",
                SensorType.TEMPERATURE,
                BigDecimal.valueOf(26),
                Unit.CELSIUS,
                timestamp
        );

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> service.process(event));
        verifyNoInteractions(deviceRepository, telemetryReadingRepository);
    }

    @Test
    void shouldThrowExceptionWhenSensorTypeIsMissing() {
        Instant timestamp = Instant.now();
        TelemetryEvent event = new TelemetryEvent(
                "v1",
                CORRECT_DEVICE_ID,
                null,
                BigDecimal.valueOf(26),
                Unit.CELSIUS,
                timestamp
        );

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> service.process(event));
        verifyNoInteractions(deviceRepository, telemetryReadingRepository);
    }

    @Test
    void shouldThrowExceptionWhenUnitIsMissing() {
        Instant timestamp = Instant.now();
        TelemetryEvent event = new TelemetryEvent(
                "v1",
                CORRECT_DEVICE_ID,
                SensorType.TEMPERATURE,
                BigDecimal.valueOf(26),
                null,
                timestamp
        );

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> service.process(event));
        verifyNoInteractions(deviceRepository, telemetryReadingRepository);
    }

    @Test
    void shouldThrowExceptionWhenValueIsMissing() {
        Instant timestamp = Instant.now();
        TelemetryEvent event = new TelemetryEvent(
                "v1",
                CORRECT_DEVICE_ID,
                SensorType.TEMPERATURE,
                null,
                Unit.CELSIUS,
                timestamp
        );

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> service.process(event));
        verifyNoInteractions(deviceRepository, telemetryReadingRepository);
    }

    @Test
    void shouldThrowExceptionWhenTimestampIsMissing() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> service.process(
                new TelemetryEvent(
                        "v1",
                        CORRECT_DEVICE_ID,
                        SensorType.TEMPERATURE,
                        BigDecimal.valueOf(26),
                        Unit.CELSIUS,
                        null
                )
        ));
        verifyNoInteractions(deviceRepository, telemetryReadingRepository);
    }

    @Test
    void shouldThrowExceptionWhenUnitIsNotCompatibleWithSensorType() {
        Instant timestamp = Instant.now();
        // TEMPERATURE nie jest zgodny z PERCENT
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> service.process(
                new TelemetryEvent(
                        "v1",
                        CORRECT_DEVICE_ID,
                        SensorType.TEMPERATURE,
                        BigDecimal.valueOf(26),
                        Unit.PERCENT,
                        timestamp
                )
        ));
        verifyNoInteractions(deviceRepository, telemetryReadingRepository);
    }

    @Test
    void shouldThrowExceptionWhenDeviceIdNotFoundInRepository() {
        Instant timestamp = Instant.now();
        when(deviceRepository.findByDeviceId("unknown-device")).thenReturn(Optional.empty());

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> service.process(
                new TelemetryEvent(
                        "v1",
                        "unknown-device",
                        SensorType.TEMPERATURE,
                        BigDecimal.valueOf(26),
                        Unit.CELSIUS,
                        timestamp
                )
        ));

        verify(deviceRepository).findByDeviceId("unknown-device");
        verifyNoInteractions(telemetryReadingRepository);
    }

    private TelemetryEvent prepareEvent(Instant timestamp) {
        return new TelemetryEvent(
                "v1",
                CORRECT_DEVICE_ID,
                SensorType.TEMPERATURE,
                BigDecimal.valueOf(26),
                Unit.CELSIUS,
                timestamp
        );
    }
}