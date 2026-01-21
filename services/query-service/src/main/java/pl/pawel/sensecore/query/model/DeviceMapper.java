package pl.pawel.sensecore.query.model;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import pl.pawel.sensecore.persistence.entity.Device;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DeviceMapper {

    DeviceDto toDto(Device device);
}
