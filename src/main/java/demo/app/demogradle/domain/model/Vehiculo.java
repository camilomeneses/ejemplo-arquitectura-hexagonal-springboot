package demo.app.demogradle.domain.model;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Vehiculo {
    private final String placa;
    private final TipoVehiculo tipo;
    private final LocalDateTime fechaIngreso;
    private final LocalDateTime fechaSalida;
    private final boolean activo;

    public static Vehiculo crear(String placa, TipoVehiculo tipo) {
        validarPlaca(placa);
        return Vehiculo.builder()
                .placa(placa.toUpperCase())
                .tipo(tipo)
                .fechaIngreso(LocalDateTime.now())
                .activo(true)
                .build();
    }

    public Vehiculo marcarSalida() {
        if (!activo) {
            throw new IllegalStateException("El vehículo ya ha salido del parqueadero");
        }
        return Vehiculo.builder()
                .placa(this.placa)
                .tipo(this.tipo)
                .fechaIngreso(this.fechaIngreso)
                .fechaSalida(LocalDateTime.now())
                .activo(false)
                .build();
    }

    private static void validarPlaca(String placa) {
        if (placa == null || placa.trim().isEmpty()) {
            throw new IllegalArgumentException("La placa no puede estar vacía");
        }
        if (placa.length() < 6 || placa.length() > 7) {
            throw new IllegalArgumentException("La placa debe tener entre 6 y 7 caracteres");
        }
    }
}
