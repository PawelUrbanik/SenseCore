package pl.pawel.sensecore.ingestionservice.device;

import org.springframework.stereotype.Service;
import pl.pawel.sensecore.persistence.entity.Device;

@Service
public class DeviceRegistryService {


    private final DeviceRepository deviceRepository;

    public DeviceRegistryService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public Device resolveActiveDeviceByFingerprint(String fingerprint) {
        Device device = deviceRepository
                .findDeviceByFingerprint(fingerprint)
                .orElseThrow(() -> new IllegalArgumentException("Unknown device certificate fingerprint"));


        String status = device.getStatus() == null ? "" : device.getStatus().trim().toUpperCase();

        if (!"ACTIVE".equals(status)) {
            throw new IllegalArgumentException("Device is not active: status=" + status);
        }

        return device;
    }
}
