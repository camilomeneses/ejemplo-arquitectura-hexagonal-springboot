package demo.app.demogradle.infrastructure.persistence.adapter;

import demo.app.demogradle.domain.model.TipoVehiculo;
import demo.app.demogradle.domain.model.Vehiculo;
import demo.app.demogradle.infrastructure.persistence.entity.VehiculoEntity;
import demo.app.demogradle.infrastructure.persistence.repository.VehiculoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * INTEGRATION TESTS - Arquitectura Hexagonal
 * 
 * ✅ CARACTERÍSTICAS DE INTEGRATION TEST EN HEXAGONAL:
 * - Prueba la integración de JPA con infraestructura REAL
 * - Utiliza Spring Context (@DataJpaTest)
 * - Base de datos H2 en memoria (infraestructura real pero controlada)
 * - Prueba el repositorio JPA directamente
 * - Verifica consultas SQL generadas por JPA
 *
 * 🎯 QUÉ ESTAMOS PROBANDO:
 * - VehiculoJpaRepository (Spring Data JPA)
 * - Persistencia real en H2
 * - Consultas SQL generadas por JPA
 * - Mapeo de entidades JPA
 */
@DataJpaTest
class VehiculoJpaRepositoryIntegrationTest {

    @Autowired
    private VehiculoJpaRepository jpaRepository; // ← Spring Data JPA

    @Autowired
    private TestEntityManager entityManager; // ← Para setup de datos

    /**
     * INTEGRATION TEST: Guardar entidad
     * Prueba persistencia real Entity → Database
     */
    @Test
    void deberiaGuardarVehiculoCorrectamente() {
        // Given - ARRANGE
        VehiculoEntity vehiculo = VehiculoEntity.builder()
                .placa("ABC123")
                .tipo(TipoVehiculo.CARRO)
                .fechaIngreso(LocalDateTime.now())
                .activo(true)
                .build();

        // When - ACT
        VehiculoEntity vehiculoGuardado = jpaRepository.save(vehiculo);

        // Then - ASSERT
        // Verificar que se guardó correctamente
        assertNotNull(vehiculoGuardado);
        assertEquals("ABC123", vehiculoGuardado.getPlaca());
        assertEquals(TipoVehiculo.CARRO, vehiculoGuardado.getTipo());
        assertTrue(vehiculoGuardado.isActivo());
        
        // Verificar en la base de datos directamente
        VehiculoEntity entityEnBD = entityManager.find(VehiculoEntity.class, "ABC123");
        assertNotNull(entityEnBD);
        assertEquals("ABC123", entityEnBD.getPlaca());
        assertEquals(TipoVehiculo.CARRO, entityEnBD.getTipo());
        assertTrue(entityEnBD.isActivo());
    }

    /**
     * INTEGRATION TEST: Buscar por placa
     * Prueba consulta Database → Entity
     */
    @Test
    void deberiaBuscarVehiculoPorPlaca() {
        // Given - ARRANGE
        // Insertamos datos directamente en BD
        VehiculoEntity entity = VehiculoEntity.builder()
                .placa("XYZ789")
                .tipo(TipoVehiculo.MOTO)
                .fechaIngreso(LocalDateTime.now())
                .activo(true)
                .build();
        entityManager.persistAndFlush(entity);

        // When - ACT
        Optional<VehiculoEntity> resultado = jpaRepository.findById("XYZ789");

        // Then - ASSERT
        assertTrue(resultado.isPresent());
        VehiculoEntity vehiculo = resultado.get();
        assertEquals("XYZ789", vehiculo.getPlaca());
        assertEquals(TipoVehiculo.MOTO, vehiculo.getTipo());
        assertTrue(vehiculo.isActivo());
    }

    /**
     * INTEGRATION TEST: Consulta personalizada
     * Prueba @Query en VehiculoJpaRepository
     */
    @Test
    void deberiaBuscarVehiculosActivos() {
        // Given - ARRANGE
        // Limpiar cualquier dato previo
        jpaRepository.deleteAll();
        entityManager.flush();

        // Vehículo activo con placa válida de 6 caracteres
        VehiculoEntity vehiculoActivo = VehiculoEntity.builder()
                .placa("ACT001")
                .tipo(TipoVehiculo.CARRO)
                .fechaIngreso(LocalDateTime.now())
                .activo(true)
                .build();
        
        // Vehículo inactivo con placa válida de 6 caracteres
        VehiculoEntity vehiculoInactivo = VehiculoEntity.builder()
                .placa("INA001")
                .tipo(TipoVehiculo.MOTO)
                .fechaIngreso(LocalDateTime.now().minusHours(2))
                .fechaSalida(LocalDateTime.now())
                .activo(false)
                .build();
        
        entityManager.persist(vehiculoActivo);
        entityManager.persist(vehiculoInactivo);
        entityManager.flush();
        entityManager.clear(); // Limpiar el contexto de persistencia

        // When - ACT
        List<VehiculoEntity> vehiculosActivos = jpaRepository.findByActivoTrue();

        // Then - ASSERT
        assertEquals(1, vehiculosActivos.size());
        VehiculoEntity vehiculo = vehiculosActivos.get(0);
        assertEquals("ACT001", vehiculo.getPlaca());
        assertTrue(vehiculo.isActivo());
    }

    /**
     * INTEGRATION TEST: Actualización de estado
     * Prueba flujo completo de actualizar vehículo
     */
    @Test
    void deberiaActualizarEstadoVehiculo() {
        // Given - ARRANGE
        // Crear y persistir vehículo activo
        VehiculoEntity vehiculoInicial = VehiculoEntity.builder()
                .placa("UPD123")
                .tipo(TipoVehiculo.CARRO)
                .fechaIngreso(LocalDateTime.now())
                .activo(true)
                .build();

        VehiculoEntity vehiculoGuardado = jpaRepository.save(vehiculoInicial);

        // Simular salida del vehículo
        vehiculoGuardado.setActivo(false);
        vehiculoGuardado.setFechaSalida(LocalDateTime.now());

        // When - ACT
        VehiculoEntity vehiculoActualizado = jpaRepository.save(vehiculoGuardado);

        // Then - ASSERT
        assertNotNull(vehiculoActualizado);
        assertEquals("UPD123", vehiculoActualizado.getPlaca());
        assertFalse(vehiculoActualizado.isActivo());
        assertNotNull(vehiculoActualizado.getFechaSalida());
        
        // Verificar en BD
        VehiculoEntity entityActualizada = entityManager.find(VehiculoEntity.class, "UPD123");
        assertFalse(entityActualizada.isActivo());
        assertNotNull(entityActualizada.getFechaSalida());
    }

    /**
     * INTEGRATION TEST: Eliminación
     * Prueba operación de eliminación física
     */
    @Test
    void deberiaEliminarVehiculo() {
        // Given - ARRANGE
        VehiculoEntity entity = VehiculoEntity.builder()
                .placa("DEL123")
                .tipo(TipoVehiculo.MOTO)
                .fechaIngreso(LocalDateTime.now())
                .activo(true)
                .build();

        // Persistir y obtener el ID para asegurar que existe
        VehiculoEntity savedEntity = jpaRepository.save(entity);
        entityManager.flush();

        // Verificar que se guardó correctamente
        assertTrue(jpaRepository.existsById("DEL123"));

        // When - ACT
        jpaRepository.deleteById("DEL123");
        entityManager.flush();
        entityManager.clear(); // Limpiar cache de primer nivel

        // Then - ASSERT
        VehiculoEntity entityEliminada = entityManager.find(VehiculoEntity.class, "DEL123");
        assertNull(entityEliminada);

        // Verificar también con el repositorio
        assertFalse(jpaRepository.existsById("DEL123"));
    }

    /**
     * INTEGRATION TEST: Verificar que la consulta personalizada funciona
     * Prueba la anotación @Query
     */
    @Test
    void deberiaEjecutarConsultaPersonalizadaCorrectamente() {
        // Given - ARRANGE
        VehiculoEntity vehiculo1 = VehiculoEntity.builder()
                .placa("QUERY1")
                .tipo(TipoVehiculo.CARRO)
                .fechaIngreso(LocalDateTime.now())
                .activo(true)
                .build();

        VehiculoEntity vehiculo2 = VehiculoEntity.builder()
                .placa("QUERY2")
                .tipo(TipoVehiculo.MOTO)
                .fechaIngreso(LocalDateTime.now())
                .activo(true)
                .build();

        VehiculoEntity vehiculo3 = VehiculoEntity.builder()
                .placa("QUERY3")
                .tipo(TipoVehiculo.CARRO)
                .fechaIngreso(LocalDateTime.now())
                .activo(false)
                .build();

        entityManager.persist(vehiculo1);
        entityManager.persist(vehiculo2);
        entityManager.persist(vehiculo3);
        entityManager.flush();

        // When - ACT
        List<VehiculoEntity> activos = jpaRepository.findByActivoTrue();

        // Then - ASSERT
        assertEquals(2, activos.size());
        assertTrue(activos.stream().allMatch(VehiculoEntity::isActivo));
        assertTrue(activos.stream().anyMatch(v -> "QUERY1".equals(v.getPlaca())));
        assertTrue(activos.stream().anyMatch(v -> "QUERY2".equals(v.getPlaca())));
    }
}
