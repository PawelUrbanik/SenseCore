package pl.pawel.sensecore.ingestionservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import pl.pawel.sensecore.contracts.SensorType;
import pl.pawel.sensecore.contracts.TelemetryEvent;
import pl.pawel.sensecore.contracts.Unit;
import pl.pawel.sensecore.ingestionservice.device.DeviceRepository;
import pl.pawel.sensecore.ingestionservice.security.CertUtils;
import pl.pawel.sensecore.ingestionservice.support.RabbitTestConfig;
import pl.pawel.sensecore.ingestionservice.support.TestcontainersConfig;
import pl.pawel.sensecore.persistence.entity.Device;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(RabbitTestConfig.class)
class TelemetryIngestIntegrationTest extends TestcontainersConfig {

    private static final String CERT_HEADER = "-----BEGIN CERTIFICATE-----\n" +
            "dGVzdA==\n" +
            "-----END CERTIFICATE-----";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${sensecore.rabbit.temperature-routing-key}")
    private String telemetryQueue;

    @BeforeEach
    void setUp() {
        deviceRepository.deleteAll();
        rabbitTemplate.execute(channel -> {
            channel.queuePurge(telemetryQueue);
            return null;
        });
    }

    @Test
    void ingest_accepts_valid_payload_and_publishes_event() throws Exception {
        String fingerprint = CertUtils.sha256FromClientCertHeader(CERT_HEADER);
        Device device = new Device();
        device.setDeviceId("dev-1");
        device.setStatus("ACTIVE");
        device.setFingerprint(fingerprint);
        deviceRepository.save(device);

        String body = """
                {
                  "value": 21.5,
                  "unit": "C",
                  "timestamp": "2026-02-19T10:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/ingest/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-SSL-Client-Verify", "SUCCESS")
                        .header("X-SSL-Client-Cert", CERT_HEADER)
                        .header("X-Forwarded-For", "203.0.113.7")
                        .content(body))
                .andExpect(status().isAccepted());

        Object message = rabbitTemplate.receiveAndConvert(telemetryQueue, 5000);
        assertThat(message).isInstanceOf(TelemetryEvent.class);
        TelemetryEvent event = (TelemetryEvent) message;

        assertThat(event.deviceId()).isEqualTo("dev-1");
        assertThat(event.sensorType()).isEqualTo(SensorType.TEMPERATURE);
        assertThat(event.unit()).isEqualTo(Unit.CELSIUS);
        assertThat(event.value()).isEqualTo(new BigDecimal("21.5"));
        assertThat(event.timestamp()).isEqualTo(Instant.parse("2026-02-19T10:00:00Z"));
    }

    @Test
    void ingest_rejects_missing_client_cert_header() throws Exception {
        String body = """
                {
                  "value": 20,
                  "unit": "C",
                  "timestamp": "2026-02-19T10:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/ingest/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-SSL-Client-Verify", "SUCCESS")
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Missing X-SSL-Client-Cert"));
    }

    @Test
    void ingest_rejects_temperature_out_of_range() throws Exception {
        String fingerprint = CertUtils.sha256FromClientCertHeader(CERT_HEADER);
        Device device = new Device();
        device.setDeviceId("dev-1");
        device.setStatus("ACTIVE");
        device.setFingerprint(fingerprint);
        deviceRepository.save(device);

        String body = """
                {
                  "value": 200,
                  "unit": "C",
                  "timestamp": "2026-02-19T10:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/ingest/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-SSL-Client-Verify", "SUCCESS")
                        .header("X-SSL-Client-Cert", CERT_HEADER)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("temperature out of range: MIN: -50 or MAX: 100"));
    }
}
