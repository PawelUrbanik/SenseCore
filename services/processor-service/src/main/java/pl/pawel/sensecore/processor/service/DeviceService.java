package pl.pawel.sensecore.processor.service;

import pl.pawel.sensecore.processor.model.DeviceDto;

import java.util.Optional;

public interface DeviceService {
    Optional<DeviceDto> findByDeviceId(String deviceId);
}
