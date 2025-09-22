package demo.app.demogradle.infrastructure.persistence.mapper;

import demo.app.demogradle.domain.model.Vehiculo;
import demo.app.demogradle.infrastructure.persistence.entity.VehiculoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VehiculoMapper {

    VehiculoEntity toEntity(Vehiculo vehiculo);

    Vehiculo toDomain(VehiculoEntity entity);
}
