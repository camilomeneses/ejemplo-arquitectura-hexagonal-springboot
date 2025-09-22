package demo.app.demogradle.application.controller;

import demo.app.demogradle.DemoGradleApplication;
import demo.app.demogradle.application.dto.IngresoVehiculoRequest;
import demo.app.demogradle.domain.model.TipoVehiculo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * END-TO-END INTEGRATION TESTS - Arquitectura Hexagonal
 *
 * ✅ CARACTERÍSTICAS DE E2E INTEGRATION TEST EN HEXAGONAL:
 * - Prueba TODO el flujo desde HTTP hasta Database
 * - Spring Boot Test completo (@SpringBootTest)
 * - Todos los adaptadores y puertos REALES
 * - Base de datos H2 real (pero en memoria)
 * - Serialización/Deserialización JSON real
 * - Validaciones (@Valid) reales
 * - Transacciones reales
 *
 * 🎯 QUÉ ESTAMOS PROBANDO:
 * - Flujo completo: HTTP → Controller → UseCase → Service → Repository → DB
 * - Integración de TODOS los componentes
 * - Comportamiento real del sistema
 * - Contratos de API reales
 * - Manejo de errores end-to-end
 *
 * 📋 SOLUCIÓN CORRECTA PARA E2E TESTS:
 * - @SpringBootTest: Carga el contexto completo de Spring (resuelve DI)
 * - @AutoConfigureMockMvc: Configura MockMvc para pruebas HTTP
 * - @ActiveProfiles("test"): Usa configuración específica de test
 * - @Transactional: Maneja transacciones de BD en tests
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ParqueaderoControllerE2ETest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * E2E TEST: Flujo completo de ingreso exitoso
   * Prueba desde HTTP Request hasta persistencia en BD
   */
  @Test
  void deberiaIngresarVehiculoCompletamenteE2E() throws Exception {
    // Given - ARRANGE
    IngresoVehiculoRequest request = new IngresoVehiculoRequest();
    request.setPlaca("ABC123");
    request.setTipo(TipoVehiculo.CARRO);

    // When & Then - ACT & ASSERT
    mockMvc.perform(post("/api/parqueadero/ingresar")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.placa").value("ABC123"))
        .andExpect(jsonPath("$.tipo").value("CARRO"))
        .andExpect(jsonPath("$.activo").value(true))
        .andExpect(jsonPath("$.fechaIngreso").isNotEmpty())
        .andExpect(jsonPath("$.fechaSalida").isEmpty())
        .andExpect(jsonPath("$.costo").isEmpty());
  }

  /**
   * E2E TEST: Validación de entrada real
   * Prueba @Valid en controller con datos inválidos
   */
  @Test
  void deberiaRechazarPlacaInvalidaE2E() throws Exception {
    // Given - ARRANGE
    IngresoVehiculoRequest request = new IngresoVehiculoRequest();
    request.setPlaca("AB12"); // Muy corta - inválida
    request.setTipo(TipoVehiculo.CARRO);

    // When & Then - ACT & ASSERT
    mockMvc.perform(post("/api/parqueadero/ingresar")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  /**
   * E2E TEST: Regla de negocio - No duplicados
   * Prueba invariante del dominio end-to-end
   */
  @Test
  void deberiaRechazarVehiculoDuplicadoE2E() throws Exception {
    // Given - ARRANGE
    IngresoVehiculoRequest request = new IngresoVehiculoRequest();
    request.setPlaca("DUP123");
    request.setTipo(TipoVehiculo.MOTO);

    // Primer ingreso (exitoso)
    mockMvc.perform(post("/api/parqueadero/ingresar")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    // When & Then - ACT & ASSERT
    // Segundo ingreso (debe fallar)
    mockMvc.perform(post("/api/parqueadero/ingresar")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict()); // 409 Conflict
  }

  /**
   * E2E TEST: Flujo completo ingresar → consultar → sacar
   * Prueba múltiples operaciones secuenciales
   */
  @Test
  void deberiaEjecutarFlujoCompletoE2E() throws Exception {
    // Given - ARRANGE
    IngresoVehiculoRequest request = new IngresoVehiculoRequest();
    request.setPlaca("FLOW123");
    request.setTipo(TipoVehiculo.CARRO);

    // Step 1: Ingresar vehículo
    mockMvc.perform(post("/api/parqueadero/ingresar")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    // Step 2: Consultar vehículos activos
    mockMvc.perform(get("/api/parqueadero/activos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
        .andExpect(jsonPath("$[?(@.placa=='FLOW123')].activo").value(contains(true)));

    // Step 3: Intentar calcular costo (debe fallar - vehículo activo)
    mockMvc.perform(get("/api/parqueadero/costo/FLOW123"))
        .andExpect(status().isBadRequest()); // 400 Bad Request

    // Step 4: Sacar vehículo
    mockMvc.perform(put("/api/parqueadero/sacar/FLOW123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.placa").value("FLOW123"))
        .andExpect(jsonPath("$.activo").value(false))
        .andExpect(jsonPath("$.fechaSalida").isNotEmpty())
        .andExpect(jsonPath("$.costo").isNumber());

    // Step 5: Calcular costo (ahora debe funcionar)
    mockMvc.perform(get("/api/parqueadero/costo/FLOW123"))
        .andExpect(status().isOk())
        .andExpect(content().string(matchesPattern("\\d+"))); // Número

    // Step 6: Verificar en historial
    mockMvc.perform(get("/api/parqueadero/historial"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.placa=='FLOW123')].activo").value(contains(false)));
  }

  /**
   * E2E TEST: Manejo de errores 404
   * Prueba vehículo no encontrado
   */
  @Test
  void deberiaRetornar404ParaVehiculoNoExistenteE2E() throws Exception {
    // When & Then - ACT & ASSERT
    mockMvc.perform(put("/api/parqueadero/sacar/NOEXISTE"))
        .andExpect(status().isNotFound()); // 404 Not Found

    mockMvc.perform(get("/api/parqueadero/costo/NOEXISTE"))
        .andExpect(status().isNotFound()); // 404 Not Found
  }

  /**
   * E2E TEST: Consultas sin datos
   * Prueba respuestas vacías correctas
   */
  @Test
  void deberiaRetornarListasVaciasCorrectamenteE2E() throws Exception {
    // When & Then - ACT & ASSERT
    mockMvc.perform(get("/api/parqueadero/activos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));

    mockMvc.perform(get("/api/parqueadero/historial"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  /**
   * E2E TEST: Múltiples vehículos
   * Prueba comportamiento con varios vehículos
   */
  @Test
  void deberiaManejarMultiplesVehiculosE2E() throws Exception {
    // Given - ARRANGE
    // Ingresar múltiples vehículos con placas únicas para este test
    String[] placas = {"MULT01", "MULT02", "MULT03"};
    TipoVehiculo[] tipos = {TipoVehiculo.CARRO, TipoVehiculo.MOTO, TipoVehiculo.CARRO};

    // Step 1: Ingresar todos los vehículos
    for (int i = 0; i < placas.length; i++) {
      IngresoVehiculoRequest request = new IngresoVehiculoRequest();
      request.setPlaca(placas[i]);
      request.setTipo(tipos[i]);

      mockMvc.perform(post("/api/parqueadero/ingresar")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated());
    }

    // Step 2: Verificar que nuestros vehículos específicos están activos
    mockMvc.perform(get("/api/parqueadero/activos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.placa=='MULT01')]").exists())
        .andExpect(jsonPath("$[?(@.placa=='MULT02')]").exists())
        .andExpect(jsonPath("$[?(@.placa=='MULT03')]").exists())
        .andExpect(jsonPath("$[?(@.placa=='MULT01')].activo").value(contains(true)))
        .andExpect(jsonPath("$[?(@.placa=='MULT02')].activo").value(contains(true)))
        .andExpect(jsonPath("$[?(@.placa=='MULT03')].activo").value(contains(true)));

    // Step 3: Sacar uno específico
    mockMvc.perform(put("/api/parqueadero/sacar/MULT02"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.placa").value("MULT02"))
        .andExpect(jsonPath("$.activo").value(false));

    // Step 4: Verificar estado después de sacar uno
    mockMvc.perform(get("/api/parqueadero/activos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.placa=='MULT01')]").exists())
        .andExpect(jsonPath("$[?(@.placa=='MULT03')]").exists())
        .andExpect(jsonPath("$[?(@.placa=='MULT01')].activo").value(contains(true)))
        .andExpect(jsonPath("$[?(@.placa=='MULT03')].activo").value(contains(true)));

    // Step 5: Verificar que MULT02 ya no está en activos
    mockMvc.perform(get("/api/parqueadero/activos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.placa=='MULT02')]").doesNotExist());

    // Step 6: Verificar en historial que están todos (activos e inactivos)
    mockMvc.perform(get("/api/parqueadero/historial"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.placa=='MULT01')]").exists())
        .andExpect(jsonPath("$[?(@.placa=='MULT02')]").exists())
        .andExpect(jsonPath("$[?(@.placa=='MULT03')]").exists());
  }
}

/**
 * 📊 COMPARACIÓN DE TIPOS DE TEST EN ARQUITECTURA HEXAGONAL:
 *
 * 🔬 UNIT TESTS (ParqueaderoServiceTest):
 * ✅ Mock de puertos (VehiculoRepository)
 * ✅ Sin Spring Context
 * ✅ Solo lógica de negocio
 * ✅ Rápidos (< 50ms)
 * ✅ Aislados completamente
 *
 * 🧪 INTEGRATION TESTS (VehiculoRepositoryAdapterIntegrationTest):
 * ✅ Spring Context parcial (@DataJpaTest)
 * ✅ Base de datos H2 real
 * ✅ Prueba adaptadores específicos
 * ✅ Mapeo Domain ↔ Entity
 * ✅ Consultas SQL reales
 *
 * 🌐 E2E TESTS (ParqueaderoControllerE2ETest):
 * ✅ Spring Context completo (@SpringBootTest)
 * ✅ Todo el stack real
 * ✅ HTTP requests reales
 * ✅ Serialización JSON real
 * ✅ Flujos de usuario completos
 *
 * 🎯 PIRÁMIDE DE TESTING HEXAGONAL:
 *
 *           /\
 *          /  \     ← E2E Tests (pocos, lentos, valiosos)
 *         /____\
 *        /      \   ← Integration Tests (moderados)
 *       /________\
 *      /          \ ← Unit Tests (muchos, rápidos, específicos)
 *     /__________\
 *
 * ✅ 70% Unit Tests - Lógica de negocio pura
 * ✅ 20% Integration Tests - Adaptadores y mapeo
 * ✅ 10% E2E Tests - Flujos de usuario críticos
 */
