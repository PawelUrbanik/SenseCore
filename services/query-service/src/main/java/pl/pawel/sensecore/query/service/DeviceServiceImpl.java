package pl.pawel.sensecore.query.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pl.pawel.sensecore.query.model.DeviceDto;

import java.util.List;

@Slf4j
@Service
public class DeviceServiceImpl implements DeviceService {

    private final RestClient deviceRestClient;

    public DeviceServiceImpl(RestClient deviceRestClient) {
        this.deviceRestClient = deviceRestClient;
    }

    @Override
    public List<DeviceDto> getAllDevices() {
        log.debug("Fetching all devices");
        List<DeviceDto> allDevices = deviceRestClient.get()
                .uri("/devices")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        if (allDevices == null || allDevices.isEmpty()) {
            log.warn("No devices found");
        } else {
            log.debug("Found {} devices", allDevices.size());
        }
        return allDevices;
    }
}
