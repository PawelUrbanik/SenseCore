package pl.pawel.sensecore.query.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.pawel.sensecore.query.model.DeviceDto;
import pl.pawel.sensecore.query.model.DeviceMapper;
import pl.pawel.sensecore.query.repository.DeviceRepository;

import java.util.List;

@RestController
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper mapper;

    public DeviceController(DeviceRepository deviceRepository, DeviceMapper mapper) {
        this.deviceRepository = deviceRepository;
        this.mapper = mapper;
    }


    @GetMapping
    public List<DeviceDto> devices(){
        return deviceRepository.findAllByOrderByDeviceIdAsc()
                .stream()
                .map(mapper::toDto)
                .toList();
    }
}
