package pl.pawel.sensecore.query.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import pl.pawel.sensecore.persistence.entity.TelemetryReading;
import pl.pawel.sensecore.query.model.TelemetryReadingDto;
import pl.pawel.sensecore.query.model.TelemetryReadingMapper;
import pl.pawel.sensecore.query.repository.TelemetryReadingRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetryQueryServiceImplTest {

    @Mock
    private TelemetryReadingRepository readingRepository;

    @Mock
    private TelemetryReadingMapper mapper;

    @InjectMocks
    private TelemetryQueryServiceImpl service;

    private TelemetryReadingDto testDto;
    private TelemetryReading mockEntity;

    @BeforeEach
    void setUp() {
        testDto = new TelemetryReadingDto("device-1", "temperature", BigDecimal.TEN, "°C", Instant.now());
        mockEntity = new TelemetryReading();
    }

    @Test
    void shouldReturnLatestReadingWhenDataExists() {
        String deviceId = "device-1";
        String sensorType = "TEMPERATURE";

        when(readingRepository.findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(deviceId, "temperature"))
                .thenReturn(Optional.of(mockEntity));
        when(mapper.toDto(mockEntity)).thenReturn(testDto);

        TelemetryReadingDto result = service.latest(deviceId, sensorType);

        assertNotNull(result);
        assertEquals(testDto, result);
        assertEquals("device-1", result.deviceId());
        assertEquals("temperature", result.sensorType());
        verify(readingRepository).findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(deviceId, "temperature");
        verify(mapper).toDto(mockEntity);
    }

    @Test
    void shouldReturnNullWhenNoLatestReading() {
        String deviceId = "device-1";
        String sensorType = "HUMIDITY";

        when(readingRepository.findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(deviceId, "humidity"))
                .thenReturn(Optional.empty());

        TelemetryReadingDto result = service.latest(deviceId, sensorType);

        assertNull(result);
        verify(readingRepository).findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(deviceId, "humidity");
        verify(mapper, never()).toDto(any());
    }

    @Test
    void shouldNormalizeSensorTypeToLowercase() {
        String deviceId = "device-1";
        String sensorType = "  TEMPERATURE  ";

        when(readingRepository.findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(deviceId, "temperature"))
                .thenReturn(Optional.of(mockEntity));
        when(mapper.toDto(mockEntity)).thenReturn(testDto);

        service.latest(deviceId, sensorType);

        verify(readingRepository).findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(eq(deviceId), eq("temperature"));
    }

    @Test
    void shouldNormalizeMixedCaseSensorType() {
        String deviceId = "device-1";
        String sensorType = "HuMiDiTy";

        when(readingRepository.findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(deviceId, "humidity"))
                .thenReturn(Optional.of(mockEntity));
        when(mapper.toDto(mockEntity)).thenReturn(testDto);

        service.latest(deviceId, sensorType);

        verify(readingRepository).findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(eq(deviceId), eq("humidity"));
    }

    @Test
    void shouldReturnHistoryWithValidLimit() {
        String deviceId = "device-1";
        String sensorType = "TEMPERATURE";
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        when(readingRepository.findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), any(PageRequest.class)))
                .thenReturn(List.of(mockEntity, mockEntity));
        when(mapper.toDto(any())).thenReturn(testDto);

        List<TelemetryReadingDto> result = service.history(deviceId, sensorType, from, to, 100);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(readingRepository).findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), eq(PageRequest.of(0, 100)));
        verify(mapper, times(2)).toDto(any());
    }

    @Test
    void shouldUseSafeLimitWhenLimitIsZero() {
        String deviceId = "device-1";
        String sensorType = "TEMPERATURE";
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        when(readingRepository.findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), any(PageRequest.class)))
                .thenReturn(List.of());

        service.history(deviceId, sensorType, from, to, 0);

        verify(readingRepository).findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), eq(PageRequest.of(0, 200)));
    }

    @Test
    void shouldUseSafeLimitWhenLimitIsNegative() {
        String deviceId = "device-1";
        String sensorType = "TEMPERATURE";
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        when(readingRepository.findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), any(PageRequest.class)))
                .thenReturn(List.of());

        service.history(deviceId, sensorType, from, to, -50);

        verify(readingRepository).findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), eq(PageRequest.of(0, 200)));
    }

    @Test
    void shouldUseSafeLimitWhenLimitExceedsMaximum() {
        String deviceId = "device-1";
        String sensorType = "TEMPERATURE";
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        when(readingRepository.findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), any(PageRequest.class)))
                .thenReturn(List.of());

        service.history(deviceId, sensorType, from, to, 3000);

        verify(readingRepository).findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), eq(PageRequest.of(0, 200)));
    }

    @Test
    void shouldUseLimitOf2000AsMaximum() {
        String deviceId = "device-1";
        String sensorType = "TEMPERATURE";
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        when(readingRepository.findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), any(PageRequest.class)))
                .thenReturn(List.of());

        service.history(deviceId, sensorType, from, to, 2000);

        verify(readingRepository).findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), eq(PageRequest.of(0, 2000)));
    }

    @Test
    void shouldUseExactLimitWhenWithinRange() {
        String deviceId = "device-1";
        String sensorType = "TEMPERATURE";
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        when(readingRepository.findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), any(PageRequest.class)))
                .thenReturn(List.of());

        service.history(deviceId, sensorType, from, to, 500);

        verify(readingRepository).findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), eq(PageRequest.of(0, 500)));
    }

    @Test
    void shouldReturnEmptyHistoryWhenNoData() {
        String deviceId = "device-1";
        String sensorType = "TEMPERATURE";
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        when(readingRepository.findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), any(PageRequest.class)))
                .thenReturn(List.of());

        List<TelemetryReadingDto> result = service.history(deviceId, sensorType, from, to, 100);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(readingRepository).findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), any(PageRequest.class));
    }

    @Test
    void shouldHandleNullSensorTypeInLatest() {
        String deviceId = "device-1";

        when(readingRepository.findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(deviceId, null))
                .thenReturn(Optional.empty());

        TelemetryReadingDto result = service.latest(deviceId, null);

        assertNull(result);
        verify(readingRepository).findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(deviceId, null);
    }

    @Test
    void shouldThrowExceptionWhenRepositoryThrowsExceptionInLatest() {
        String deviceId = "device-1";
        String sensorType = "TEMPERATURE";

        when(readingRepository.findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(deviceId, "temperature"))
                .thenThrow(new RuntimeException("Database connection error"));

        assertThrows(RuntimeException.class, () -> service.latest(deviceId, sensorType));
        verify(readingRepository).findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(deviceId, "temperature");
        verify(mapper, never()).toDto(any());
    }

    @Test
    void shouldThrowExceptionWhenRepositoryThrowsExceptionInHistory() {
        String deviceId = "device-1";
        String sensorType = "TEMPERATURE";
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        when(readingRepository.findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), any(PageRequest.class)))
                .thenThrow(new RuntimeException("Database query failed"));

        assertThrows(RuntimeException.class, () -> service.history(deviceId, sensorType, from, to, 100));
        verify(readingRepository).findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), any(PageRequest.class));
        verify(mapper, never()).toDto(any());
    }

    @Test
    void shouldThrowExceptionWhenMapperThrowsExceptionInLatest() {
        String deviceId = "device-1";
        String sensorType = "TEMPERATURE";

        when(readingRepository.findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(deviceId, "temperature"))
                .thenReturn(Optional.of(mockEntity));
        when(mapper.toDto(mockEntity)).thenThrow(new IllegalArgumentException("Invalid entity"));

        assertThrows(IllegalArgumentException.class, () -> service.latest(deviceId, sensorType));
        verify(mapper).toDto(mockEntity);
    }

    @Test
    void shouldThrowExceptionWhenMapperThrowsExceptionInHistory() {
        String deviceId = "device-1";
        String sensorType = "TEMPERATURE";
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        when(readingRepository.findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), any(PageRequest.class)))
                .thenReturn(List.of(mockEntity));
        when(mapper.toDto(any())).thenThrow(new IllegalArgumentException("Mapping error"));

        assertThrows(IllegalArgumentException.class, () -> service.history(deviceId, sensorType, from, to, 100));
        verify(mapper).toDto(any());
    }

    @Test
    void shouldHandleMultipleReadingsInHistory() {
        String deviceId = "device-1";
        String sensorType = "HUMIDITY";
        Instant from = Instant.now().minusSeconds(7200);
        Instant to = Instant.now();

        TelemetryReading entity1 = new TelemetryReading();
        entity1.setDeviceId("device-1");
        entity1.setSensorType("humidity");
        entity1.setValueNumeric(BigDecimal.valueOf(40));
        entity1.setUnit("%");
        entity1.setTimestamp(from);

        TelemetryReading entity2 = new TelemetryReading();
        entity2.setDeviceId("device-1");
        entity2.setSensorType("humidity");
        entity2.setValueNumeric(BigDecimal.valueOf(50));
        entity2.setUnit("%");
        entity2.setTimestamp(from.plusSeconds(1));

        TelemetryReading entity3 = new TelemetryReading();
        entity3.setDeviceId("device-1");
        entity3.setSensorType("humidity");
        entity3.setValueNumeric(BigDecimal.valueOf(60));
        entity3.setUnit("%");
        entity3.setTimestamp(from.plusSeconds(2));

        TelemetryReadingDto dto1 = new TelemetryReadingDto("device-1", "humidity", BigDecimal.valueOf(40), "%", from);
        TelemetryReadingDto dto2 = new TelemetryReadingDto("device-1", "humidity", BigDecimal.valueOf(50), "%", from.plusSeconds(1));
        TelemetryReadingDto dto3 = new TelemetryReadingDto("device-1", "humidity", BigDecimal.valueOf(60), "%", from.plusSeconds(2));

        when(readingRepository.findHistory(eq(deviceId), eq("humidity"), eq(from), eq(to), any(PageRequest.class)))
                .thenReturn(List.of(entity1, entity2, entity3));

        when(mapper.toDto(any(TelemetryReading.class))).thenAnswer(invocation -> {
            TelemetryReading reading = invocation.getArgument(0);
            return new TelemetryReadingDto(
                    reading.getDeviceId(),
                    reading.getSensorType(),
                    reading.getValueNumeric(),
                    reading.getUnit(),
                    reading.getTimestamp()
            );
        });

        List<TelemetryReadingDto> result = service.history(deviceId, sensorType, from, to, 50);

        assertNotNull(result);
        assertEquals(3, result.size());

        assertEquals("device-1", result.get(0).deviceId());
        assertEquals("humidity", result.get(0).sensorType());
        assertEquals(BigDecimal.valueOf(40), result.get(0).value());
        assertEquals("%", result.get(0).unit());

        assertEquals("device-1", result.get(1).deviceId());
        assertEquals("humidity", result.get(1).sensorType());
        assertEquals(BigDecimal.valueOf(50), result.get(1).value());

        assertEquals("device-1", result.get(2).deviceId());
        assertEquals("humidity", result.get(2).sensorType());
        assertEquals(BigDecimal.valueOf(60), result.get(2).value());

        verify(mapper, times(3)).toDto(any(TelemetryReading.class));
    }

    @Test
    void shouldNormalizeDeviceIdNotTouched() {
        String deviceId = "ArDuInO-ESP-01";
        String sensorType = "TEMPERATURE";

        when(readingRepository.findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc("ArDuInO-ESP-01", "temperature"))
                .thenReturn(Optional.of(mockEntity));
        when(mapper.toDto(mockEntity)).thenReturn(testDto);

        service.latest(deviceId, sensorType);

        verify(readingRepository).findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc("ArDuInO-ESP-01", "temperature");
    }

    @Test
    void shouldHandleSpecialCharactersInDeviceId() {
        String deviceId = "device-_123-test";
        String sensorType = "PRESSURE";

        when(readingRepository.findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(deviceId, "pressure"))
                .thenReturn(Optional.of(mockEntity));
        when(mapper.toDto(mockEntity)).thenReturn(testDto);

        TelemetryReadingDto result = service.latest(deviceId, sensorType);

        assertNotNull(result);
        verify(readingRepository).findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(deviceId, "pressure");
    }

    @Test
    void shouldHandleLimitEqualsOne() {
        String deviceId = "device-1";
        String sensorType = "TEMPERATURE";
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        when(readingRepository.findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), any(PageRequest.class)))
                .thenReturn(List.of());

        service.history(deviceId, sensorType, from, to, 1);

        verify(readingRepository).findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), eq(PageRequest.of(0, 1)));
    }

    @Test
    void shouldHandleLimitJustBelowMaximum() {
        String deviceId = "device-1";
        String sensorType = "TEMPERATURE";
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        when(readingRepository.findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), any(PageRequest.class)))
                .thenReturn(List.of());

        service.history(deviceId, sensorType, from, to, 1999);

        verify(readingRepository).findHistory(eq(deviceId), eq("temperature"), eq(from), eq(to), eq(PageRequest.of(0, 1999)));
    }

    @Test
    void shouldVerifyMapperCalledOnceForEachEntity() {
        String deviceId = "device-1";
        String sensorType = "TEMPERATURE";

        when(readingRepository.findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(deviceId, "temperature"))
                .thenReturn(Optional.of(mockEntity));
        when(mapper.toDto(mockEntity)).thenReturn(testDto);

        service.latest(deviceId, sensorType);

        verify(mapper, times(1)).toDto(mockEntity);
        verify(mapper, times(1)).toDto(any());
    }
}
