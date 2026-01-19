package pl.pawel.sensecore.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class TelemetryReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "sensor_type", nullable = false)
    private String sensorType;

    @Column(name = "value_numeric", nullable = false, precision = 18, scale = 6)
    private BigDecimal valueNumeric;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "ts", nullable = false)
    private Instant timestamp;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt = Instant.now();

}
