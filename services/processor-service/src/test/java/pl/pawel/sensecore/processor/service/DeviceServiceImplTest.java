package pl.pawel.sensecore.processor.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import pl.pawel.sensecore.processor.model.DeviceDto;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeviceServiceImplTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void findByDeviceId_returnsEmpty_whenRegistryReturnsNotFound() {
        RestClient restClient = mock(RestClient.class);
        RestClient.RequestHeadersUriSpec requestUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(requestUriSpec);
        when(requestUriSpec.uri("/devices/{deviceId}", "missing-device")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(DeviceDto.class)).thenThrow(HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                HttpHeaders.EMPTY,
                new byte[0],
                StandardCharsets.UTF_8));

        DeviceServiceImpl service = new DeviceServiceImpl(restClient);

        Optional<DeviceDto> result = service.findByDeviceId("missing-device");

        assertTrue(result.isEmpty());
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void findByDeviceId_rethrows_whenRegistryReturnsServerError() {
        RestClient restClient = mock(RestClient.class);
        RestClient.RequestHeadersUriSpec requestUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(requestUriSpec);
        when(requestUriSpec.uri("/devices/{deviceId}", "dev-1")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(DeviceDto.class)).thenThrow(HttpServerErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                HttpHeaders.EMPTY,
                new byte[0],
                StandardCharsets.UTF_8));

        DeviceServiceImpl service = new DeviceServiceImpl(restClient);

        HttpServerErrorException thrown = assertThrows(
                HttpServerErrorException.class,
                () -> service.findByDeviceId("dev-1"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrown.getStatusCode());
    }
}
