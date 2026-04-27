package pl.pawel.services.deviceregistryservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.pawel.services.deviceregistryservice.model.Device;
import pl.pawel.services.deviceregistryservice.repo.DeviceRepository;

import java.time.Instant;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class DeviceRegistryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviceRepository deviceRepository;

    @BeforeEach
    void setUp() {
        deviceRepository.deleteAll();
    }

    @Test
    void devices_returns_sorted_list_items_with_created_at() throws Exception {
        Device deviceB = new Device();
        deviceB.setDeviceId("b-device");
        deviceB.setStatus("ACTIVE");
        deviceB.setCreatedAt(Instant.parse("2026-02-19T11:00:00Z"));
        deviceRepository.save(deviceB);

        Device deviceA = new Device();
        deviceA.setDeviceId("a-device");
        deviceA.setStatus("REVOKED");
        deviceA.setCreatedAt(Instant.parse("2026-02-19T10:00:00Z"));
        deviceRepository.save(deviceA);

        mockMvc.perform(get("/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].deviceId").value("a-device"))
                .andExpect(jsonPath("$[0].status").value("REVOKED"))
                .andExpect(jsonPath("$[0].createdAt").value("2026-02-19T10:00:00Z"))
                .andExpect(jsonPath("$[1].deviceId").value("b-device"))
                .andExpect(jsonPath("$[1].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].createdAt").value("2026-02-19T11:00:00Z"));
    }
}
