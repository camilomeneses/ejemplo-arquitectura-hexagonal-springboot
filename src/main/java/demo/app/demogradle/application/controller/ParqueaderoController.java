package demo.app.demogradle.application.controller;

import demo.app.demogradle.application.dto.IngresoVehiculoRequest;
import demo.app.demogradle.application.dto.VehiculoResponse;
import demo.app.demogradle.domain.model.Vehiculo;
import demo.app.demogradle.domain.port.in.ParqueaderoUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parqueadero")
@RequiredArgsConstructor
public class ParqueaderoController {

    private final ParqueaderoUseCase parqueaderoUseCase;

    @PostMapping("/ingresar")
    public ResponseEntity<VehiculoResponse> ingresarVehiculo(@Valid @RequestBody IngresoVehiculoRequest request) {
        try {
            Vehiculo vehiculo = parqueaderoUseCase.ingresarVehiculo(request.getPlaca(), request.getTipo());
            VehiculoResponse response = mapToResponse(vehiculo);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/sacar/{placa}")
    public ResponseEntity<VehiculoResponse> sacarVehiculo(@PathVariable String placa) {
        try {
            Vehiculo vehiculo = parqueaderoUseCase.sacarVehiculo(placa);
            int costo = parqueaderoUseCase.calcularCosto(placa);
            
            VehiculoResponse response = mapToResponse(vehiculo);
            response.setCosto(costo);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/activos")
    public ResponseEntity<List<VehiculoResponse>> consultarVehiculosActivos() {
        List<Vehiculo> vehiculos = parqueaderoUseCase.consultarVehiculosActivos();
        List<VehiculoResponse> responses = vehiculos.stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/historial")
    public ResponseEntity<List<VehiculoResponse>> consultarHistorial() {
        List<Vehiculo> vehiculos = parqueaderoUseCase.consultarHistorial();
        List<VehiculoResponse> responses = vehiculos.stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/costo/{placa}")
    public ResponseEntity<Integer> calcularCosto(@PathVariable String placa) {
        try {
            int costo = parqueaderoUseCase.calcularCosto(placa);
            return ResponseEntity.ok(costo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private VehiculoResponse mapToResponse(Vehiculo vehiculo) {
        return VehiculoResponse.builder()
                .placa(vehiculo.getPlaca())
                .tipo(vehiculo.getTipo())
                .fechaIngreso(vehiculo.getFechaIngreso())
                .fechaSalida(vehiculo.getFechaSalida())
                .activo(vehiculo.isActivo())
                .build();
    }
}
