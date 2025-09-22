package demo.app.demogradle.domain.service;

import demo.app.demogradle.domain.model.TipoVehiculo;
import demo.app.demogradle.domain.model.Vehiculo;
import demo.app.demogradle.domain.port.out.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParqueaderoServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @InjectMocks
    private ParqueaderoService parqueaderoService;

    @BeforeEach
    void setUp() {
        // Setup básico para cada test
    }

    @Test
    void deberiaIngresarVehiculoExitosamente() {
        // Given
        String placa = "ABC123";
        TipoVehiculo tipo = TipoVehiculo.CARRO;
        
        when(vehiculoRepository.buscarPorPlaca(placa)).thenReturn(Optional.empty());
        when(vehiculoRepository.guardar(any(Vehiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Vehiculo resultado = parqueaderoService.ingresarVehiculo(placa, tipo);

        // Then
        assertNotNull(resultado);
        assertEquals(placa, resultado.getPlaca());
        assertEquals(tipo, resultado.getTipo());
        assertTrue(resultado.isActivo());
        assertNotNull(resultado.getFechaIngreso());
        assertNull(resultado.getFechaSalida());
        
        verify(vehiculoRepository, times(1)).buscarPorPlaca(placa);
        verify(vehiculoRepository, times(1)).guardar(any(Vehiculo.class));
    }

    @Test
    void deberiaLanzarExcepcionCuandoVehiculoYaEstaEnParqueadero() {
        // Given
        String placa = "ABC123";
        TipoVehiculo tipo = TipoVehiculo.CARRO;
        Vehiculo vehiculoExistente = Vehiculo.crear(placa, tipo);
        
        when(vehiculoRepository.buscarPorPlaca(placa)).thenReturn(Optional.of(vehiculoExistente));

        // When & Then
        IllegalStateException excepcion = assertThrows(IllegalStateException.class, 
            () -> parqueaderoService.ingresarVehiculo(placa, tipo));
        
        assertTrue(excepcion.getMessage().contains("ya está en el parqueadero"));
        verify(vehiculoRepository, times(1)).buscarPorPlaca(placa);
        verify(vehiculoRepository, never()).guardar(any(Vehiculo.class));
    }

    @Test
    void deberiaSacarVehiculoExitosamente() {
        // Given
        String placa = "ABC123";
        Vehiculo vehiculoActivo = Vehiculo.crear(placa, TipoVehiculo.CARRO);
        
        when(vehiculoRepository.buscarPorPlaca(placa)).thenReturn(Optional.of(vehiculoActivo));
        when(vehiculoRepository.guardar(any(Vehiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Vehiculo resultado = parqueaderoService.sacarVehiculo(placa);

        // Then
        assertNotNull(resultado);
        assertEquals(placa, resultado.getPlaca());
        assertFalse(resultado.isActivo());
        assertNotNull(resultado.getFechaSalida());
        
        verify(vehiculoRepository, times(1)).buscarPorPlaca(placa);
        verify(vehiculoRepository, times(1)).guardar(any(Vehiculo.class));
    }

    @Test
    void deberiaLanzarExcepcionCuandoVehiculoNoExiste() {
        // Given
        String placa = "ABC123";
        
        when(vehiculoRepository.buscarPorPlaca(placa)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, 
            () -> parqueaderoService.sacarVehiculo(placa));
        
        assertTrue(excepcion.getMessage().contains("no encontrado"));
        verify(vehiculoRepository, times(1)).buscarPorPlaca(placa);
        verify(vehiculoRepository, never()).guardar(any(Vehiculo.class));
    }
}
