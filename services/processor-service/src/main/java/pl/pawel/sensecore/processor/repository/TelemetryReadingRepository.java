package pl.pawel.sensecore.processor.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import pl.pawel.sensecore.persistence.entity.TelemetryReading;

public interface TelemetryReadingRepository extends JpaRepository<TelemetryReading, Long> {
}
