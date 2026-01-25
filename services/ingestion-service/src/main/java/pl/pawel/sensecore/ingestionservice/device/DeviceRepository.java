package pl.pawel.sensecore.ingestionservice.device;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.pawel.sensecore.persistence.entity.Device;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findDeviceByFingerprint(String fingerprint);
}
