package pl.pawel.services.deviceregistryservice.service;

import org.springframework.stereotype.Service;
import pl.pawel.services.deviceregistryservice.model.DeviceDto;
import pl.pawel.services.deviceregistryservice.model.DeviceExtDto;
import pl.pawel.services.deviceregistryservice.model.DeviceListItemDto;
import pl.pawel.services.deviceregistryservice.repo.DeviceRepository;

import java.util.List;

@Service
public class DeviceManagementService {


    private final DeviceRepository deviceRepository;

    public DeviceManagementService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }


    public DeviceDto getActiveDeviceByFingerprint(String fingerprint) {
        return deviceRepository
                .findDeviceByFingerprint(fingerprint)
                .stream()
                .filter(device -> "ACTIVE".equals(device.getStatus()))
                .map(device -> new DeviceDto(device.getDeviceId(), device.getStatus(), device.getFingerprint()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown device fingerprint"));
    }

    public DeviceDto getDeviceById(String deviceId) {
        return deviceRepository
                .findDeviceByDeviceId(deviceId)
                .map(device -> new DeviceDto(device.getDeviceId(), device.getStatus(), device.getFingerprint()))
                .orElseThrow(() -> new IllegalArgumentException("Unknown device id"));
    }

    public DeviceExtDto getDeviceByIdExt(String deviceId) {
        return deviceRepository
                .findDeviceByDeviceId(deviceId)
                .map(device -> new DeviceExtDto(device.getDeviceId(), device.getStatus()))
                .orElseThrow(() -> new IllegalArgumentException("Unknown device id"));
    }

    public List<DeviceListItemDto> getAllDevices() {
        return deviceRepository
                .findAllByOrderByDeviceIdAsc()
                .stream()
                .map(device -> new DeviceListItemDto(
                        device.getDeviceId(),
                        device.getStatus(),
                        device.getCreatedAt()))
                .toList();
    }
}
