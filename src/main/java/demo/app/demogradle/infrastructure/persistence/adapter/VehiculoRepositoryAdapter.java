package demo.app.demogradle.infrastructure.persistence.adapter;

import demo.app.demogradle.domain.model.Vehiculo;
import demo.app.demogradle.domain.port.out.VehiculoRepository;
import demo.app.demogradle.infrastructure.persistence.entity.VehiculoEntity;
import demo.app.demogradle.infrastructure.persistence.mapper.VehiculoMapper;
import demo.app.demogradle.infrastructure.persistence.repository.VehiculoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VehiculoRepositoryAdapter implements VehiculoRepository {

    private final VehiculoJpaRepository jpaRepository;
    private final VehiculoMapper mapper;

    @Override
    public Vehiculo guardar(Vehiculo vehiculo) {
        VehiculoEntity entity = mapper.toEntity(vehiculo);
        VehiculoEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Vehiculo> buscarPorPlaca(String placa) {
        return jpaRepository.findById(placa.toUpperCase())
                .map(mapper::toDomain);
    }

    @Override
    public List<Vehiculo> buscarVehiculosActivos() {
        return jpaRepository.findByActivoTrue()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Vehiculo> buscarTodos() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void eliminar(String placa) {
        jpaRepository.deleteById(placa.toUpperCase());
    }
}
