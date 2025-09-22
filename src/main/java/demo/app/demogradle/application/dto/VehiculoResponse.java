package demo.app.demogradle.application.dto;

import demo.app.demogradle.domain.model.TipoVehiculo;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VehiculoResponse {
    private String placa;
    private TipoVehiculo tipo;
    private LocalDateTime fechaIngreso;
    private LocalDateTime fechaSalida;
    private boolean activo;
    private Integer costo;
}
