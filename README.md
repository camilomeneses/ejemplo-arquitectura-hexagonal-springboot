# Guía Completa: Spring Boot + Gradle + Arquitectura Hexagonal
## De MVC con Maven a Hexagonal con Gradle

Esta documentación está diseñada para desarrolladores que vienen de **Spring Boot con Maven y patrón MVC** y quieren aprender **Arquitectura Hexagonal con Gradle**, con miras a desarrollar **microservicios**.

---

## 📋 Tabla de Contenidos
1. [Análisis de la Estructura Actual](#1-análisis-de-la-estructura-actual)
2. [Diferencias Clave: MVC vs Hexagonal](#2-diferencias-clave-mvc-vs-hexagonal)
3. [Gradle vs Maven: Comparación Práctica](#3-gradle-vs-maven-comparación-práctica)
4. [Configuración del Proyecto Actual](#4-configuración-del-proyecto-actual)
5. [Extensiones para Microservicios](#5-extensiones-para-microservicios)
6. [Casos de Uso Prácticos](#6-casos-de-uso-prácticos)
7. [Comandos y Tareas Gradle](#7-comandos-y-tareas-gradle)

---

## 1. Análisis de la Estructura Actual

### Estructura del Proyecto
```
src/main/java/demo/app/demogradle/
├── DemoGradleApplication.java     # Clase principal
├── application/                   # Capa de aplicación (equivale a controllers en MVC)
│   ├── controller/               # Controllers REST
│   ├── dto/                      # DTOs de entrada/salida
│   └── exception/                # Manejo de excepciones
├── domain/                       # Núcleo del negocio (equivale a service + entity en MVC)
│   ├── model/                    # Entidades de dominio
│   ├── port/                     # Interfaces (contratos)
│   └── service/                  # Lógica de negocio
└── infrastructure/               # Adaptadores externos (equivale a repository en MVC)
    └── persistence/              # Adaptadores de base de datos
```

### Mapeo MVC → Hexagonal
| MVC (Maven) | Hexagonal (Gradle) | Propósito |
|-------------|-------------------|-----------|
| `@Controller` | `application/controller/` | API REST endpoints |
| `@Service` | `domain/service/` | Lógica de negocio |
| `@Repository` | `infrastructure/persistence/` | Acceso a datos |
| `@Entity` (JPA) | `domain/model/` + `infrastructure/persistence/entity/` | Modelo de dominio + persistencia |

---

## 2. Diferencias Clave: MVC vs Hexagonal

### En MVC (lo que conoces):
```java
// Controller depende directamente del Service
@RestController
public class ProductController {
    @Autowired
    private ProductService productService; // Dependencia directa
    
    @GetMapping("/products")
    public List<Product> getProducts() {
        return productService.findAll(); // Llamada directa
    }
}

// Service depende directamente del Repository
@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository; // Dependencia directa
}
```

### En Hexagonal (lo nuevo):
```java
// Controller depende de una interfaz (Port)
@RestController
public class ProductController {
    private final ProductUseCase productUseCase; // Dependencia de interfaz
    
    @GetMapping("/products")
    public List<ProductDto> getProducts() {
        return productUseCase.findAll(); // Llamada a través de interfaz
    }
}

// UseCase define el contrato (Port)
public interface ProductUseCase {
    List<ProductDto> findAll();
}

// Service implementa el contrato y depende de otro Port
@Service
public class ProductService implements ProductUseCase {
    private final ProductRepository productRepository; // Interfaz, no implementación
}
```

---

## 3. Gradle vs Maven: Comparación Práctica

### Archivo de Configuración
**Maven (pom.xml):**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

**Gradle (build.gradle):**
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

### Comandos Equivalentes
| Acción | Maven | Gradle |
|--------|-------|--------|
| Compilar | `mvn compile` | `./gradlew compileJava` |
| Ejecutar tests | `mvn test` | `./gradlew test` |
| Empaquetar | `mvn package` | `./gradlew build` |
| Ejecutar aplicación | `mvn spring-boot:run` | `./gradlew bootRun` |
| Limpiar | `mvn clean` | `./gradlew clean` |

---

## 4. Configuración del Proyecto Actual

### Análisis del build.gradle
```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.6'
    id 'io.spring.dependency-management' version '1.1.7'
    id 'org.graalvm.buildtools.native' version '0.10.6'  // Para ejecutables nativos
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'     // API REST
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa' // Base de datos
    implementation 'org.springframework.boot:spring-boot-starter-validation' // Validaciones
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'                // Mapeo de objetos
    compileOnly 'org.projectlombok:lombok'                              // Reducir boilerplate
    runtimeOnly 'com.h2database:h2'                                     // BD en memoria
}
```

### Dependencias Actuales y su Propósito
- **spring-boot-starter-web**: APIs REST
- **spring-boot-starter-data-jpa**: Persistencia con JPA/Hibernate
- **spring-boot-starter-validation**: Validación de DTOs
- **mapstruct**: Mapeo automático entre entidades y DTOs
- **lombok**: Reduce código boilerplate (@Getter, @Setter, etc.)
- **h2**: Base de datos en memoria para desarrollo

---

## 5. Extensiones para Microservicios

### A. Cliente Feign (Consumir APIs)
**Agregar al build.gradle:**
```groovy
ext {
    set('springCloudVersion', "2023.0.0")
}

dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
    }
}
```

**Archivos a crear/modificar:**
1. **DemoGradleApplication.java**: Agregar `@EnableFeignClients`
2. **infrastructure/client/ExternalApiClient.java**: Cliente Feign
3. **domain/port/ExternalApiPort.java**: Interfaz del puerto
4. **infrastructure/client/ExternalApiAdapter.java**: Adaptador

**Ejemplo de implementación:**
```java
// 1. Habilitar Feign en la aplicación principal
@SpringBootApplication
@EnableFeignClients
public class DemoGradleApplication { ... }

// 2. Puerto (interfaz)
public interface ExternalApiPort {
    UserData getUserData(String userId);
}

// 3. Cliente Feign
@FeignClient(name = "external-api", url = "${external.api.url}")
public interface ExternalApiClient {
    @GetMapping("/users/{id}")
    UserResponse getUser(@PathVariable String id);
}

// 4. Adaptador
@Component
public class ExternalApiAdapter implements ExternalApiPort {
    private final ExternalApiClient client;
    private final UserMapper mapper;
    
    @Override
    public UserData getUserData(String userId) {
        UserResponse response = client.getUser(userId);
        return mapper.toUserData(response);
    }
}
```

### B. Spring Security
**Agregar al build.gradle:**
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server' // Para JWT
}
```

**Archivos a crear:**
- **infrastructure/config/SecurityConfig.java**: Configuración de seguridad
- **infrastructure/security/JwtAuthenticationFilter.java**: Filtro JWT personalizado

**Ejemplo básico:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        return http.build();
    }
}
```

### C. Observabilidad (Actuator + Prometheus)
**Agregar al build.gradle:**
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-registry-prometheus'
    implementation 'io.micrometer:micrometer-tracing-bridge-brave' // Para tracing
}
```

**Configurar en application.properties:**
```properties
# Actuator endpoints
management.endpoints.web.exposure.include=health,metrics,prometheus,info
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true

# Tracing
management.tracing.sampling.probability=1.0
```

### D. Ejecutable Nativo (GraalVM)
**Ya está configurado en tu build.gradle:**
```groovy
plugins {
    id 'org.graalvm.buildtools.native' version '0.10.6'
}
```

**Comandos para generar ejecutable nativo:**
```bash
# Generar hints de reflexión
./gradlew nativeCompile

# Ejecutar con agent para generar metadata
./gradlew bootRun -Pargs=--spring.profiles.active=native

# Compilar ejecutable nativo
./gradlew nativeCompile
```

---

## 6. Casos de Uso Prácticos

### Crear un Nuevo Endpoint (Ejemplo: Gestión de Usuarios)

**1. Crear modelo de dominio:**
```java
// domain/model/User.java
@Value
@Builder
public class User {
    String id;
    String name;
    String email;
    LocalDateTime createdAt;
}
```

**2. Crear puerto de salida:**
```java
// domain/port/UserRepository.java
public interface UserRepository {
    Optional<User> findById(String id);
    User save(User user);
    List<User> findAll();
}
```

**3. Crear puerto de entrada:**
```java
// domain/port/UserUseCase.java
public interface UserUseCase {
    UserDto createUser(CreateUserRequest request);
    UserDto getUserById(String id);
    List<UserDto> getAllUsers();
}
```

**4. Implementar el servicio:**
```java
// domain/service/UserService.java
@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Override
    public UserDto createUser(CreateUserRequest request) {
        User user = User.builder()
            .id(UUID.randomUUID().toString())
            .name(request.getName())
            .email(request.getEmail())
            .createdAt(LocalDateTime.now())
            .build();
        
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }
}
```

**5. Crear adaptador de persistencia:**
```java
// infrastructure/persistence/UserJpaAdapter.java
@Repository
@RequiredArgsConstructor
public class UserJpaAdapter implements UserRepository {
    private final UserJpaRepository jpaRepository;
    private final UserEntityMapper entityMapper;
    
    @Override
    public User save(User user) {
        UserEntity entity = entityMapper.toEntity(user);
        UserEntity savedEntity = jpaRepository.save(entity);
        return entityMapper.toDomain(savedEntity);
    }
}
```

**6. Crear controlador:**
```java
// application/controller/UserController.java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserUseCase userUseCase;
    
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDto user = userUseCase.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
```

---

## 7. Comandos y Tareas Gradle

### Comandos Básicos
```bash
# Compilar el proyecto
./gradlew build

# Ejecutar la aplicación
./gradlew bootRun

# Ejecutar tests
./gradlew test

# Limpiar build
./gradlew clean

# Ver dependencias
./gradlew dependencies

# Generar ejecutable nativo
./gradlew nativeCompile

# Ejecutar con perfil específico
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Tareas Personalizadas
Puedes agregar tareas personalizadas al build.gradle:
```groovy
task runIntegrationTests(type: Test) {
    useJUnitPlatform()
    include '**/integration/**'
}

task generateDocumentation(type: Javadoc) {
    source = sourceSets.main.allJava
    classpath = configurations.compileClasspath
}
```

---

## 8. Próximos Pasos Recomendados

### Para Microservicios
1. **Spring Cloud Gateway**: API Gateway
2. **Spring Cloud Config**: Configuración centralizada
3. **Spring Cloud Discovery**: Service discovery (Eureka)
4. **Spring Cloud Circuit Breaker**: Resilience patterns

### Para Producción
1. **Docker**: Containerización
2. **Kubernetes**: Orquestación
3. **Helm**: Gestión de despliegues
4. **Jenkins/GitHub Actions**: CI/CD

---

## 9. Recursos Adicionales

- **Documentación Oficial Spring Boot**: https://spring.io/projects/spring-boot
- **Gradle User Guide**: https://docs.gradle.org/current/userguide/userguide.html
- **Arquitectura Hexagonal**: https://alistair.cockburn.us/hexagonal-architecture/
- **Spring Cloud**: https://spring.io/projects/spring-cloud
- **GraalVM Native Image**: https://www.graalvm.org/latest/reference-manual/native-image/

---

*Esta documentación te guiará en la transición de MVC/Maven a Hexagonal/Gradle y te preparará para el desarrollo de microservicios modernos.*
