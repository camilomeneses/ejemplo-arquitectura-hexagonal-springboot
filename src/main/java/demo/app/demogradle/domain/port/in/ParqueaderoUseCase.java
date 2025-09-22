package demo.app.demogradle.domain.port.in;

import demo.app.demogradle.domain.model.Vehiculo;
import demo.app.demogradle.domain.model.TipoVehiculo;

import java.util.List;

public interface ParqueaderoUseCase {
    Vehiculo ingresarVehiculo(String placa, TipoVehiculo tipo);
    Vehiculo sacarVehiculo(String placa);
    List<Vehiculo> consultarVehiculosActivos();
    List<Vehiculo> consultarHistorial();
    int calcularCosto(String placa);
}
