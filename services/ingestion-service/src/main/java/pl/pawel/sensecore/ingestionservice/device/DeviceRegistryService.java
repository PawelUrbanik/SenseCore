package pl.pawel.sensecore.ingestionservice.device;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class DeviceRegistryService {

    private final RestClient deviceRegistryRestClient;

    public DeviceRegistryService(@Qualifier("deviceRegistryRestClient") RestClient deviceRegistryRestClient) {
        this.deviceRegistryRestClient = deviceRegistryRestClient;
    }

    public DeviceDto resolveActiveDeviceByFingerprint(String fingerprint) {
        return deviceRegistryRestClient.get()
                .uri("/internal/devices/by-fingerprint/{fingerprint}", fingerprint)
                .retrieve()
                .body(DeviceDto.class);
    }

}
