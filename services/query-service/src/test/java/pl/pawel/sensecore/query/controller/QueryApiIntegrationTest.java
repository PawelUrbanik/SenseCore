package pl.pawel.sensecore.query.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.pawel.sensecore.persistence.entity.TelemetryReading;
import pl.pawel.sensecore.query.model.DeviceDto;
import pl.pawel.sensecore.query.repository.TelemetryReadingRepository;
import pl.pawel.sensecore.query.service.DeviceService;
import pl.pawel.sensecore.query.support.TestcontainersConfig;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class QueryApiIntegrationTest extends TestcontainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TelemetryReadingRepository telemetryReadingRepository;

    @MockBean
    private DeviceService deviceService;

    @BeforeEach
    void setUp() {
        telemetryReadingRepository.deleteAll();
    }

    @Test
    void devices_returns_sorted_by_device_id() throws Exception {
        when(deviceService.getAllDevices()).thenReturn(List.of(
                new DeviceDto("a-device", "ACTIVE", "2026-02-19T10:00:00Z"),
                new DeviceDto("b-device", "ACTIVE", "2026-02-19T11:00:00Z")
        ));

        mockMvc.perform(get("/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].deviceId").value("a-device"))
                .andExpect(jsonPath("$[1].deviceId").value("b-device"));
    }

    @Test
    void latest_returns_most_recent_reading() throws Exception {
        TelemetryReading older = new TelemetryReading();
        older.setDeviceId("dev-1");
        older.setSensorType("temperature");
        older.setValueNumeric(new BigDecimal("20.0"));
        older.setUnit("C");
        older.setTimestamp(Instant.parse("2026-02-19T09:00:00Z"));
        telemetryReadingRepository.save(older);

        TelemetryReading newer = new TelemetryReading();
        newer.setDeviceId("dev-1");
        newer.setSensorType("temperature");
        newer.setValueNumeric(new BigDecimal("21.0"));
        newer.setUnit("C");
        newer.setTimestamp(Instant.parse("2026-02-19T10:00:00Z"));
        telemetryReadingRepository.save(newer);

        mockMvc.perform(get("/telemetry/latest")
                        .param("deviceId", "dev-1")
                        .param("sensorType", "TEMPERATURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("dev-1"))
                .andExpect(jsonPath("$.sensorType").value("temperature"))
                .andExpect(jsonPath("$.unit").value("C"))
                .andExpect(jsonPath("$.timestamp").value("2026-02-19T10:00:00Z"));
    }

    @Test
    void history_returns_limited_results() throws Exception {
        for (int i = 0; i < 3; i++) {
            TelemetryReading reading = new TelemetryReading();
            reading.setDeviceId("dev-1");
            reading.setSensorType("temperature");
            reading.setValueNumeric(new BigDecimal("20.0").add(new BigDecimal(i)));
            reading.setUnit("C");
            reading.setTimestamp(Instant.parse("2026-02-19T10:0" + i + ":00Z"));
            telemetryReadingRepository.save(reading);
        }

        mockMvc.perform(get("/telemetry/history")
                        .param("deviceId", "dev-1")
                        .param("sensorType", "temperature")
                        .param("from", "2026-02-19T10:00:00Z")
                        .param("to", "2026-02-19T10:10:00Z")
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
