package demo.app.demogradle.application.dto;

import demo.app.demogradle.domain.model.TipoVehiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class IngresoVehiculoRequest {
    
    @NotBlank(message = "La placa es obligatoria")
    @Pattern(regexp = "^[A-Z0-9]{6,7}$", message = "La placa debe tener entre 6 y 7 caracteres alfanuméricos")
    private String placa;
    
    @NotNull(message = "El tipo de vehículo es obligatorio")
    private TipoVehiculo tipo;
}
