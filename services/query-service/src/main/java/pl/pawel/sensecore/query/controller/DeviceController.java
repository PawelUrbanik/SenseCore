package pl.pawel.sensecore.query.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.pawel.sensecore.query.model.DeviceDto;
import pl.pawel.sensecore.query.service.DeviceService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }


    @GetMapping
    public List<DeviceDto> devices() {
        log.debug("Fetching all devices");
        List<DeviceDto> devices = deviceService.getAllDevices();
        devices.forEach(device ->
                log.debug("Found device: id={}, status={}, createdAt={}",
                        device.deviceId(),
                        device.status(),
                        device.createdAt()));
        return devices;
    }
}
