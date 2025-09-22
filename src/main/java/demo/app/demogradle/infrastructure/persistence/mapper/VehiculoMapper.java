package demo.app.demogradle.infrastructure.persistence.mapper;

import demo.app.demogradle.domain.model.Vehiculo;
import demo.app.demogradle.infrastructure.persistence.entity.VehiculoEntity;

/**
 * Interface para mapear entre Domain Model y Entity
 * Implementación manual en VehiculoMapperImpl para evitar problemas con MapStruct
 */
public interface VehiculoMapper {

    VehiculoEntity toEntity(Vehiculo vehiculo);

    Vehiculo toDomain(VehiculoEntity entity);
}
