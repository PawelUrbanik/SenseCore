package pl.pawel.services.deviceregistryservice.model;

import java.time.Instant;

public record DeviceListItemDto(String deviceId, String status, Instant createdAt) {
}
