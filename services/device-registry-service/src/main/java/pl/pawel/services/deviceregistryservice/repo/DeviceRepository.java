package pl.pawel.services.deviceregistryservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pawel.services.deviceregistryservice.model.Device;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findDeviceByFingerprint(String fingerprint);

    Optional<Device> findDeviceByDeviceId(String deviceId);

    java.util.List<Device> findAllByOrderByDeviceIdAsc();
}
