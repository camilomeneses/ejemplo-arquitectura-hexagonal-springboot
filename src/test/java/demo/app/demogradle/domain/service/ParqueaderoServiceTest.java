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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UNIT TESTS - Arquitectura Hexagonal
 *
 * ✅ CARACTERÍSTICAS DE UNIT TEST EN HEXAGONAL:
 * - Prueba SOLO la lógica del DOMAIN SERVICE
 * - Mock de PUERTOS (VehiculoRepository), NO de infraestructura
 * - Sin Spring Context (@ExtendWith(MockitoExtension.class))
 * - Sin base de datos real, sin HTTP, sin frameworks
 * - Enfoque en REGLAS DE NEGOCIO del dominio
 *
 * 🎯 QUÉ ESTAMOS PROBANDO:
 * - Lógica de negocio del ParqueaderoService
 * - Invariantes del dominio (no duplicados activos)
 * - Comportamiento de los casos de uso
 * - Interacción correcta con el puerto de salida
 */
@ExtendWith(MockitoExtension.class)
class ParqueaderoServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository; // ← PUERTO mockeado, NO infraestructura

    @InjectMocks
    private ParqueaderoService parqueaderoService; // ← DOMAIN SERVICE bajo prueba

    @BeforeEach
    void setUp() {
        // Setup básico para cada test
    }

    /**
     * TEST UNITARIO: Caso exitoso de ingreso
     * Verifica que la regla de negocio se ejecute correctamente
     */
    @Test
    void deberiaIngresarVehiculoExitosamente() {
        // Given - ARRANGE
        String placa = "ABC123";
        TipoVehiculo tipo = TipoVehiculo.CARRO;
        
        // Mock del PUERTO de salida (no de infraestructura)
        when(vehiculoRepository.buscarPorPlaca(placa)).thenReturn(Optional.empty());
        when(vehiculoRepository.guardar(any(Vehiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When - ACT
        Vehiculo resultado = parqueaderoService.ingresarVehiculo(placa, tipo);

        // Then - ASSERT
        // Verificamos COMPORTAMIENTO del dominio
        assertNotNull(resultado);
        assertEquals(placa, resultado.getPlaca());
        assertEquals(tipo, resultado.getTipo());
        assertTrue(resultado.isActivo());
        assertNotNull(resultado.getFechaIngreso());
        assertNull(resultado.getFechaSalida());
        
        // Verificamos interacción con el PUERTO
        verify(vehiculoRepository, times(1)).buscarPorPlaca(placa);
        verify(vehiculoRepository, times(1)).guardar(any(Vehiculo.class));
    }

    /**
     * TEST UNITARIO: Regla de negocio - No duplicados
     * Verifica INVARIANTE del dominio
     */
    @Test
    void deberiaLanzarExcepcionCuandoVehiculoYaEstaEnParqueadero() {
        // Given - ARRANGE
        String placa = "ABC123";
        TipoVehiculo tipo = TipoVehiculo.CARRO;
        Vehiculo vehiculoExistente = Vehiculo.crear(placa, tipo);
        
        when(vehiculoRepository.buscarPorPlaca(placa)).thenReturn(Optional.of(vehiculoExistente));

        // When & Then - ACT & ASSERT
        IllegalStateException excepcion = assertThrows(IllegalStateException.class,
            () -> parqueaderoService.ingresarVehiculo(placa, tipo));
        
        // Verificamos el MENSAJE del dominio
        assertTrue(excepcion.getMessage().contains("ya está en el parqueadero"));
        verify(vehiculoRepository, times(1)).buscarPorPlaca(placa);
        verify(vehiculoRepository, never()).guardar(any(Vehiculo.class));
    }

    /**
     * TEST UNITARIO: Caso exitoso de salida
     * Verifica el cambio de estado en el dominio
     */
    @Test
    void deberiaSacarVehiculoExitosamente() {
        // Given - ARRANGE
        String placa = "ABC123";
        Vehiculo vehiculoActivo = Vehiculo.crear(placa, TipoVehiculo.CARRO);
        
        when(vehiculoRepository.buscarPorPlaca(placa)).thenReturn(Optional.of(vehiculoActivo));
        when(vehiculoRepository.guardar(any(Vehiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When - ACT
        Vehiculo resultado = parqueaderoService.sacarVehiculo(placa);

        // Then - ASSERT
        assertNotNull(resultado);
        assertEquals(placa, resultado.getPlaca());
        assertFalse(resultado.isActivo()); // ← Estado cambió
        assertNotNull(resultado.getFechaSalida()); // ← Fecha de salida asignada

        verify(vehiculoRepository, times(1)).buscarPorPlaca(placa);
        verify(vehiculoRepository, times(1)).guardar(any(Vehiculo.class));
    }

    /**
     * TEST UNITARIO: Validación de existencia
     * Verifica manejo de errores del dominio
     */
    @Test
    void deberiaLanzarExcepcionCuandoVehiculoNoExiste() {
        // Given - ARRANGE
        String placa = "ABC123";
        
        when(vehiculoRepository.buscarPorPlaca(placa)).thenReturn(Optional.empty());

        // When & Then - ACT & ASSERT
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
            () -> parqueaderoService.sacarVehiculo(placa));
        
        assertTrue(excepcion.getMessage().contains("no encontrado"));
        verify(vehiculoRepository, times(1)).buscarPorPlaca(placa);
        verify(vehiculoRepository, never()).guardar(any(Vehiculo.class));
    }

    /**
     * TEST UNITARIO ADICIONAL: Cálculo de costo
     * Verifica lógica de negocio compleja
     */
    @Test
    void deberiaCalcularCostoCorrectamente() {
        // Given - ARRANGE
        String placa = "ABC123";
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime hace2Horas = ahora.minusHours(2);

        // Creamos un vehículo que ya salió hace 2 horas
        Vehiculo vehiculoInactivo = Vehiculo.builder()
                .placa(placa)
                .tipo(TipoVehiculo.CARRO)
                .fechaIngreso(hace2Horas)
                .fechaSalida(ahora)
                .activo(false)
                .build();

        when(vehiculoRepository.buscarPorPlaca(placa)).thenReturn(Optional.of(vehiculoInactivo));

        // When - ACT
        int costo = parqueaderoService.calcularCosto(placa);

        // Then - ASSERT
        // CARRO = 3000/hora * 2 horas = 6000
        assertEquals(6000, costo);
        verify(vehiculoRepository, times(1)).buscarPorPlaca(placa);
    }

    /**
     * TEST UNITARIO: Validación de estado para cálculo
     * No se puede calcular costo de vehículo activo
     */
    @Test
    void deberiaLanzarExcepcionAlCalcularCostoDeVehiculoActivo() {
        // Given - ARRANGE
        String placa = "ABC123";
        Vehiculo vehiculoActivo = Vehiculo.crear(placa, TipoVehiculo.MOTO);

        when(vehiculoRepository.buscarPorPlaca(placa)).thenReturn(Optional.of(vehiculoActivo));

        // When & Then - ACT & ASSERT
        IllegalStateException excepcion = assertThrows(IllegalStateException.class,
            () -> parqueaderoService.calcularCosto(placa));

        assertTrue(excepcion.getMessage().contains("aún está en el parqueadero"));
        verify(vehiculoRepository, times(1)).buscarPorPlaca(placa);
    }

    /**
     * TEST UNITARIO: Consulta de vehículos activos
     * Verifica delegación al repositorio
     */
    @Test
    void deberiaConsultarVehiculosActivos() {
        // Given - ARRANGE
        List<Vehiculo> vehiculosActivos = Arrays.asList(
            Vehiculo.crear("ABC123", TipoVehiculo.CARRO),
            Vehiculo.crear("XYZ789", TipoVehiculo.MOTO)
        );

        when(vehiculoRepository.buscarVehiculosActivos()).thenReturn(vehiculosActivos);

        // When - ACT
        List<Vehiculo> resultado = parqueaderoService.consultarVehiculosActivos();

        // Then - ASSERT
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(Vehiculo::isActivo));
        verify(vehiculoRepository, times(1)).buscarVehiculosActivos();
    }
}
