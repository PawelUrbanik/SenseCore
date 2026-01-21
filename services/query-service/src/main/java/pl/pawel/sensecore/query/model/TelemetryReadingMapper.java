package pl.pawel.sensecore.query.model;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import pl.pawel.sensecore.persistence.entity.TelemetryReading;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TelemetryReadingMapper {
    TelemetryReadingDto toDto(TelemetryReading telemetryReading);

}
