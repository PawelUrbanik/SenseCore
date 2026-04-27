package pl.pawel.sensecore.query.service;

import pl.pawel.sensecore.query.model.DeviceDto;

import java.util.List;

public interface DeviceService {
    List<DeviceDto> getAllDevices();
}
