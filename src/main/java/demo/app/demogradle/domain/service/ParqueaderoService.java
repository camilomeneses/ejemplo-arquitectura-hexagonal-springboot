package demo.app.demogradle.domain.service;

import demo.app.demogradle.domain.model.Vehiculo;
import demo.app.demogradle.domain.model.TipoVehiculo;
import demo.app.demogradle.domain.port.in.ParqueaderoUseCase;
import demo.app.demogradle.domain.port.out.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * DOMAIN SERVICE en DDD
 * - Contiene lógica de negocio que no pertenece a una entidad específica
 * - Orquesta operaciones complejas del dominio
 * - No tiene estado, solo comportamiento
 * - Utiliza el repositorio (port) para persistencia
 */
@Service
@RequiredArgsConstructor
public class ParqueaderoService implements ParqueaderoUseCase {

    private final VehiculoRepository vehiculoRepository;

    /**
     * CASO DE USO: Ingresar Vehículo
     * Regla de negocio: No permitir vehículos duplicados activos
     */
    @Override
    public Vehiculo ingresarVehiculo(String placa, TipoVehiculo tipo) {
        // INVARIANTE DEL DOMINIO: Un vehículo no puede estar dos veces activo
        vehiculoRepository.buscarPorPlaca(placa)
                .filter(Vehiculo::isActivo)
                .ifPresent(v -> {
                    throw new IllegalStateException("El vehículo con placa " + placa + " ya está en el parqueadero");
                });

        // FACTORY METHOD del dominio
        Vehiculo vehiculo = Vehiculo.crear(placa, tipo);
        return vehiculoRepository.guardar(vehiculo);
    }

    /**
     * CASO DE USO: Sacar Vehículo
     * Regla de negocio: Solo se puede sacar un vehículo que esté activo
     */
    @Override
    public Vehiculo sacarVehiculo(String placa) {
        Vehiculo vehiculo = vehiculoRepository.buscarPorPlaca(placa)
                .filter(Vehiculo::isActivo)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo con placa " + placa + " no encontrado en el parqueadero"));

        Vehiculo vehiculoSalida = vehiculo.marcarSalida();
        return vehiculoRepository.guardar(vehiculoSalida);
    }

    /**
     * CASO DE USO: Consultar Vehículos Activos
     */
    @Override
    public List<Vehiculo> consultarVehiculosActivos() {
        return vehiculoRepository.buscarVehiculosActivos();
    }

    /**
     * CASO DE USO: Consultar Historial de Vehículos
     */
    @Override
    public List<Vehiculo> consultarHistorial() {
        return vehiculoRepository.buscarTodos();
    }

    /**
     * CASO DE USO: Calcular Costo de Estacionamiento
     * Regla de negocio: Solo se puede calcular el costo si el vehículo ya salió
     */
    @Override
    public int calcularCosto(String placa) {
        Vehiculo vehiculo = vehiculoRepository.buscarPorPlaca(placa)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo con placa " + placa + " no encontrado"));

        if (vehiculo.isActivo()) {
            throw new IllegalStateException("El vehículo aún está en el parqueadero. No se puede calcular el costo final.");
        }

        Duration duracion = Duration.between(vehiculo.getFechaIngreso(), vehiculo.getFechaSalida());
        long horas = duracion.toHours();
        if (horas == 0) {
            horas = 1; // Mínimo 1 hora
        }

        return (int) (horas * vehiculo.getTipo().getTarifaPorHora());
    }
}
