package demo.app.demogradle.domain.port.out;

import demo.app.demogradle.domain.model.Vehiculo;

import java.util.List;
import java.util.Optional;

public interface VehiculoRepository {
    Vehiculo guardar(Vehiculo vehiculo);
    Optional<Vehiculo> buscarPorPlaca(String placa);
    List<Vehiculo> buscarVehiculosActivos();
    List<Vehiculo> buscarTodos();
    void eliminar(String placa);
}
