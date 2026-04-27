package pl.pawel.services.deviceregistryservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pl.pawel.services.deviceregistryservice.model.DeviceDto;
import pl.pawel.services.deviceregistryservice.model.DeviceExtDto;
import pl.pawel.services.deviceregistryservice.model.DeviceListItemDto;
import pl.pawel.services.deviceregistryservice.service.DeviceManagementService;

import java.util.List;

@Slf4j
@RestController
public class DeviceRegistryController {

    private final DeviceManagementService deviceManagementService;

    public DeviceRegistryController(DeviceManagementService deviceManagementService) {
        this.deviceManagementService = deviceManagementService;
    }

   /** Internal use */

    @GetMapping("/internal/devices/by-fingerprint/{fingerprint}")
    public ResponseEntity<DeviceDto> getActiveDeviceByFingerprint(@PathVariable String fingerprint) {
        try {
            deviceManagementService.getActiveDeviceByFingerprint(fingerprint);
        } catch (IllegalArgumentException e) {
            log.debug("Device with fingerprint {} not found", fingerprint);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(deviceManagementService.getActiveDeviceByFingerprint(fingerprint));
    }

    @GetMapping("/internal/devices/{deviceId}")
    public ResponseEntity<DeviceDto> getDeviceById(@PathVariable String deviceId) {
        try {
            deviceManagementService.getDeviceById(deviceId);
        } catch (IllegalArgumentException e) {
            log.debug("Device with id {} not found", deviceId);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(deviceManagementService.getDeviceById(deviceId));
    }

    /** external use */
    @GetMapping("/devices/{deviceId}")
    public ResponseEntity<DeviceExtDto> getDeviceByIdExt(@PathVariable String deviceId) {
        try {
            deviceManagementService.getDeviceById(deviceId);
        } catch (IllegalArgumentException e) {
            log.debug("Device with id {} not found", deviceId);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(deviceManagementService.getDeviceByIdExt(deviceId));
    }


    @GetMapping("/devices")
    public ResponseEntity<List<DeviceListItemDto>> getActiveDevices() {
        return ResponseEntity.ok(deviceManagementService.getAllDevices());
    }
}
