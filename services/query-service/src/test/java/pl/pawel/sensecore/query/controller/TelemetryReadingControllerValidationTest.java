package pl.pawel.sensecore.query.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import pl.pawel.sensecore.query.api.error.ApiExceptionHandler;
import pl.pawel.sensecore.query.service.TelemetryQueryService;

import org.springframework.beans.factory.annotation.Autowired;

@WebMvcTest(controllers = TelemetryReadingController.class)
@Import({ApiExceptionHandler.class, TelemetryReadingControllerValidationTest.ValidationConfig.class})
class TelemetryReadingControllerValidationTest {

    @TestConfiguration
    static class ValidationConfig {
        @Bean
        MethodValidationPostProcessor methodValidationPostProcessor() {
            return new MethodValidationPostProcessor();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TelemetryQueryService telemetryQueryService;

    @Test
    void latest_rejects_blank_device_id() throws Exception {
        mockMvc.perform(get("/telemetry/latest")
                        .param("deviceId", "  ")
                        .param("sensorType", "TEMP"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("deviceId")))
                .andExpect(jsonPath("$.message", containsString("required")));
    }

    @Test
    void latest_rejects_missing_sensor_type() throws Exception {
        mockMvc.perform(get("/telemetry/latest")
                        .param("deviceId", "dev-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("sensorType")));
    }

    @Test
    void history_rejects_limit_below_one() throws Exception {
        mockMvc.perform(get("/telemetry/history")
                        .param("deviceId", "dev-1")
                        .param("sensorType", "TEMP")
                        .param("from", "2026-02-01T00:00:00Z")
                        .param("to", "2026-02-01T01:00:00Z")
                        .param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("limit")));
    }

    @Test
    void history_rejects_invalid_from_format() throws Exception {
        mockMvc.perform(get("/telemetry/history")
                        .param("deviceId", "dev-1")
                        .param("sensorType", "TEMP")
                        .param("from", "not-an-instant")
                        .param("to", "2026-02-01T01:00:00Z")
                        .param("limit", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Failed to convert")));
    }
}
