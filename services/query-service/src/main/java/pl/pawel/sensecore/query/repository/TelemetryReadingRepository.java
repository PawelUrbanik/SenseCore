package pl.pawel.sensecore.query.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.pawel.sensecore.persistence.entity.TelemetryReading;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TelemetryReadingRepository extends JpaRepository<TelemetryReading, Long> {
    Optional<TelemetryReading> findFirstByDeviceIdAndSensorTypeOrderByTimestampDesc(String deviceId, String sensorType);

    @Query("""
                select tr from TelemetryReading tr
                where tr.deviceId = :deviceId
                and tr.sensorType = :sensorType
                and tr.timestamp between :from and :to
            """
    )
    List<TelemetryReading> findHistory(
            @Param("deviceId") String deviceId,
            @Param("sensorType") String sensorType,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );
}
