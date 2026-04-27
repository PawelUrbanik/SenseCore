package pl.pawel.sensecore.processor.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import pl.pawel.sensecore.processor.model.DeviceDto;

import java.util.Optional;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final RestClient restClient;

    public DeviceServiceImpl(@Qualifier("deviceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }


    @Override
    public Optional<DeviceDto> findByDeviceId(String deviceId) {
        try {
            DeviceDto deviceDto = restClient.get()
                    .uri("/devices/{deviceId}", deviceId)
                    .retrieve()
                    .body(DeviceDto.class);
            return Optional.ofNullable(deviceDto);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw ex;
        }
    }
}
