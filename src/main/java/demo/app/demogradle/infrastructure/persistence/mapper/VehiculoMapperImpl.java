package demo.app.demogradle.infrastructure.persistence.mapper;

import demo.app.demogradle.domain.model.Vehiculo;
import demo.app.demogradle.infrastructure.persistence.entity.VehiculoEntity;
import org.springframework.stereotype.Component;

/**
 * Implementación manual del VehiculoMapper
 * Reemplaza la implementación automática de MapStruct para resolver problemas de beans
 */
@Component
public class VehiculoMapperImpl implements VehiculoMapper {

    @Override
    public VehiculoEntity toEntity(Vehiculo vehiculo) {
        if (vehiculo == null) {
            return null;
        }
        
        return VehiculoEntity.builder()
                .placa(vehiculo.getPlaca())
                .tipo(vehiculo.getTipo())
                .fechaIngreso(vehiculo.getFechaIngreso())
                .fechaSalida(vehiculo.getFechaSalida())
                .activo(vehiculo.isActivo())
                .build();
    }

    @Override
    public Vehiculo toDomain(VehiculoEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Vehiculo.builder()
                .placa(entity.getPlaca())
                .tipo(entity.getTipo())
                .fechaIngreso(entity.getFechaIngreso())
                .fechaSalida(entity.getFechaSalida())
                .activo(entity.isActivo())
                .build();
    }
}
