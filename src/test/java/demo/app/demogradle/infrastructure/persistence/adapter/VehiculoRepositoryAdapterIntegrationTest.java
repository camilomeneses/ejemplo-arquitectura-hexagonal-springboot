package demo.app.demogradle.infrastructure.persistence.adapter;

import demo.app.demogradle.domain.model.TipoVehiculo;
import demo.app.demogradle.domain.model.Vehiculo;
import demo.app.demogradle.infrastructure.persistence.entity.VehiculoEntity;
import demo.app.demogradle.infrastructure.persistence.mapper.VehiculoMapper;
import demo.app.demogradle.infrastructure.persistence.repository.VehiculoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * INTEGRATION TESTS - Arquitectura Hexagonal
 * 
 * ✅ CARACTERÍSTICAS DE INTEGRATION TEST EN HEXAGONAL:
 * - Prueba la integración del ADAPTADOR con infraestructura REAL
 * - Utiliza Spring Context (@DataJpaTest)
 * - Base de datos H2 en memoria (infraestructura real pero controlada)
 * - Prueba el mapeo entre Domain Model y Entity
 * - Verifica que el adaptador implemente correctamente el PUERTO
 * 
 * 🎯 QUÉ ESTAMOS PROBANDO:
 * - VehiculoRepositoryAdapter (ADAPTADOR)
 * - VehiculoMapper (conversión Domain ↔ Entity)
 * - VehiculoJpaRepository (Spring Data JPA)
 * - Persistencia real en H2
 * - Consultas SQL generadas por JPA
 */
@DataJpaTest
@Import({VehiculoRepositoryAdapter.class, VehiculoMapper.class})
class VehiculoRepositoryAdapterIntegrationTest {

    @Autowired
    private VehiculoRepositoryAdapter repositoryAdapter; // ← ADAPTADOR bajo prueba

    @Autowired
    private VehiculoJpaRepository jpaRepository; // ← Spring Data JPA

    @Autowired
    private TestEntityManager entityManager; // ← Para setup de datos

    @Autowired
    private VehiculoMapper mapper; // ← Mapper bajo prueba

    /**
     * INTEGRATION TEST: Guardar vehículo
     * Prueba persistencia real Domain → Entity → Database
     */
    @Test
    void deberiaGuardarVehiculoCorrectamente() {
        // Given - ARRANGE
        Vehiculo vehiculoDominio = Vehiculo.crear("ABC123", TipoVehiculo.CARRO);

        // When - ACT
        Vehiculo vehiculoGuardado = repositoryAdapter.guardar(vehiculoDominio);

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
     * Prueba consulta Database → Entity → Domain
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
        Optional<Vehiculo> resultado = repositoryAdapter.buscarPorPlaca("XYZ789");

        // Then - ASSERT
        assertTrue(resultado.isPresent());
        Vehiculo vehiculo = resultado.get();
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
        // Vehículo activo
        VehiculoEntity vehiculoActivo = VehiculoEntity.builder()
                .placa("ACTIVO1")
                .tipo(TipoVehiculo.CARRO)
                .fechaIngreso(LocalDateTime.now())
                .activo(true)
                .build();
        
        // Vehículo inactivo
        VehiculoEntity vehiculoInactivo = VehiculoEntity.builder()
                .placa("INACTIVO1")
                .tipo(TipoVehiculo.MOTO)
                .fechaIngreso(LocalDateTime.now().minusHours(2))
                .fechaSalida(LocalDateTime.now())
                .activo(false)
                .build();
        
        entityManager.persist(vehiculoActivo);
        entityManager.persist(vehiculoInactivo);
        entityManager.flush();

        // When - ACT
        List<Vehiculo> vehiculosActivos = repositoryAdapter.buscarVehiculosActivos();

        // Then - ASSERT
        assertEquals(1, vehiculosActivos.size());
        Vehiculo vehiculo = vehiculosActivos.get(0);
        assertEquals("ACTIVO1", vehiculo.getPlaca());
        assertTrue(vehiculo.isActivo());
    }

    /**
     * INTEGRATION TEST: Mapeo Domain ↔ Entity
     * Prueba VehiculoMapper directamente
     */
    @Test
    void deberiaMapearCorrectamenteDominioAEntity() {
        // Given - ARRANGE
        Vehiculo vehiculoDominio = Vehiculo.builder()
                .placa("MAP123")
                .tipo(TipoVehiculo.CARRO)
                .fechaIngreso(LocalDateTime.now())
                .activo(true)
                .build();

        // When - ACT
        VehiculoEntity entity = mapper.toEntity(vehiculoDominio);

        // Then - ASSERT
        assertNotNull(entity);
        assertEquals(vehiculoDominio.getPlaca(), entity.getPlaca());
        assertEquals(vehiculoDominio.getTipo(), entity.getTipo());
        assertEquals(vehiculoDominio.getFechaIngreso(), entity.getFechaIngreso());
        assertEquals(vehiculoDominio.isActivo(), entity.isActivo());
    }

    /**
     * INTEGRATION TEST: Mapeo Entity → Domain
     * Prueba conversión inversa
     */
    @Test
    void deberiaMapearCorrectamenteEntityADominio() {
        // Given - ARRANGE
        VehiculoEntity entity = VehiculoEntity.builder()
                .placa("ENT123")
                .tipo(TipoVehiculo.MOTO)
                .fechaIngreso(LocalDateTime.now())
                .fechaSalida(null)
                .activo(true)
                .build();

        // When - ACT
        Vehiculo vehiculoDominio = mapper.toDomain(entity);

        // Then - ASSERT
        assertNotNull(vehiculoDominio);
        assertEquals(entity.getPlaca(), vehiculoDominio.getPlaca());
        assertEquals(entity.getTipo(), vehiculoDominio.getTipo());
        assertEquals(entity.getFechaIngreso(), vehiculoDominio.getFechaIngreso());
        assertEquals(entity.isActivo(), vehiculoDominio.isActivo());
    }

    /**
     * INTEGRATION TEST: Actualización de estado
     * Prueba flujo completo de actualizar vehículo
     */
    @Test
    void deberiaActualizarEstadoVehiculo() {
        // Given - ARRANGE
        // Crear y persistir vehículo activo
        Vehiculo vehiculoInicial = Vehiculo.crear("UPD123", TipoVehiculo.CARRO);
        Vehiculo vehiculoGuardado = repositoryAdapter.guardar(vehiculoInicial);
        
        // Simular salida del vehículo
        Vehiculo vehiculoConSalida = vehiculoGuardado.marcarSalida();

        // When - ACT
        Vehiculo vehiculoActualizado = repositoryAdapter.guardar(vehiculoConSalida);

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
        entityManager.persistAndFlush(entity);

        // When - ACT
        repositoryAdapter.eliminar("DEL123");
        entityManager.clear(); // Limpiar cache de primer nivel

        // Then - ASSERT
        VehiculoEntity entityEliminada = entityManager.find(VehiculoEntity.class, "DEL123");
        assertNull(entityEliminada);
    }
}
