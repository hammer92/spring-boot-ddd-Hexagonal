# Sistema de Tutorías

## Descripción General

El Sistema de Tutorías es una aplicación desarrollada con Spring Boot que facilita la gestión de tutorías entre tutores y tutorados dentro de una organización. La plataforma permite solicitar tutorías sobre habilidades específicas, gestionar el ciclo de vida completo de las tutorías, programar sesiones, y proporcionar retroalimentación.

El sistema está diseñado siguiendo los principios de la Arquitectura Hexagonal (Ports and Adapters), lo que permite una clara separación de responsabilidades y facilita la mantenibilidad y escalabilidad del código.

## Arquitectura del Sistema

### Arquitectura Hexagonal

El proyecto implementa la Arquitectura Hexagonal (también conocida como Ports and Adapters), que organiza el código en tres capas principales:

1. **Dominio**: Contiene las entidades de negocio, reglas y lógica de dominio.
2. **Aplicación**: Orquesta los casos de uso mediante servicios que coordinan las operaciones del dominio.
3. **Infraestructura**: Implementa los adaptadores para interactuar con el mundo exterior (API REST, base de datos, etc.).

### Estructura de Paquetes

La estructura del proyecto sigue un enfoque modular por funcionalidad:

```
com.pragma
├── chapter/                  # Módulo de capítulos/departamentos
├── feedbacks/                # Módulo de retroalimentación
├── shared/                   # Componentes compartidos
├── skills/                   # Módulo de habilidades
├── tutoring_sessions/        # Módulo de sesiones de tutoría
├── tutorings/                # Módulo de tutorías
├── tutorings_requests/       # Módulo de solicitudes de tutoría
└── usuarios/                 # Módulo de usuarios
```

Cada módulo sigue la estructura de la arquitectura hexagonal:

```
módulo/
├── application/
│   └── service/              # Servicios de aplicación
├── domain/
│   ├── model/                # Entidades de dominio
│   └── port/
│       ├── input/            # Puertos de entrada (casos de uso)
│       └── output/           # Puertos de salida (repositorios)
└── infrastructure/
    └── adapter/
        ├── input/
        │   └── rest/         # Controladores REST
        │       ├── dto/      # Objetos de transferencia de datos
        │       └── mapper/   # Mapeadores DTO <-> Entidad
        └── output/
            └── persistence/  # Implementación de repositorios
                ├── entity/   # Entidades JPA
                ├── mapper/   # Mapeadores Entidad <-> Modelo
                └── repository/ # Repositorios Spring Data
```

## Diagrama de Componentes

```mermaid
graph TD
    subgraph "Capa de Presentación"
        REST[API REST]
    end
    
    subgraph "Capa de Aplicación"
        US[Servicios de Aplicación]
    end
    
    subgraph "Capa de Dominio"
        UC[Casos de Uso]
        DM[Modelos de Dominio]
        RP[Puertos de Repositorio]
    end
    
    subgraph "Capa de Infraestructura"
        PA[Adaptadores de Persistencia]
        DB[(Base de Datos SQLite)]
    end
    
    REST --> US
    US --> UC
    UC --> DM
    UC --> RP
    RP --> PA
    PA --> DB
```

## Diagrama de Flujo de Datos

```mermaid
sequenceDiagram
    actor Usuario
    participant Controller as Controller
    participant Service as Service
    participant UseCase as UseCase
    participant Repository as Repository
    participant Database as Database
    
    Usuario->>Controller: Solicitud HTTP
    Controller->>Service: Llamada al servicio
    Service->>UseCase: Ejecuta caso de uso
    UseCase->>Repository: Solicita datos
    Repository->>Database: Consulta
    Database-->>Repository: Resultado
    Repository-->>UseCase: Datos
    UseCase-->>Service: Resultado procesado
    Service-->>Controller: Respuesta
    Controller-->>Usuario: Respuesta HTTP
```

## Diagrama de Entidades

```mermaid
classDiagram
    direction LR

    class Chapter {
        +String id
        +String name
    }
    
    class User {
        +String id
        +String firstName
        +String lastName
        +String email
        +Chapter chapter
        +RolUsuario rol
        +int activeTutoringLimit
    }

    class Skill {
        +String id
        +String name
    }

    class Tutoring {
        +String id
        +User tutor
        +User tutee
        +List~Skill~ skills
        +Date startDate
        +Date expectedEndDate
        +TutoringStatus status
        +String objectives
    }

    class TutoringSession {
        +String id
        +Tutoring tutoring
        +String datetime
        +int durationMinutes
        +String locationLink
        +String topicsCovered
        +String notes
        +TutoringsSessionStatus sessionStatus
    }

    class TutoringRequest {
        +String id
        +User tutee
        +List~Skill~ skills
        +String needsDescription
        +Date requestDate
        +RequestStatus requestStatus
        +String assignedTutoringId
    }

    class Feedback {
        +String id
        +User evaluator
        +Date evaluationDate
        +Tutoring tutoring
        +String score
        +String comments
    }

    User "1" -- "1" Chapter : pertenece a
    User "1" -- "0..N" TutoringRequest : solicita
    User "1" -- "0..N" Tutoring : es tutor en
    User "1" -- "0..N" Tutoring : es tutorado en
    User "1" -- "0..N" Feedback : proporciona
    
    Tutoring "1" -- "0..N" TutoringSession : tiene
    Tutoring "1" -- "0..N" Feedback : recibe
    Tutoring "1" -- "0..N" Skill : aborda
    
    TutoringRequest "0..1" -- "0..1" Tutoring : resulta en
    TutoringRequest "1" -- "0..N" Skill : requiere
```

## Módulos Principales

### 1. Usuarios (Users)

Gestiona la información de los usuarios del sistema, que pueden tener diferentes roles:
- **Tutor**: Puede impartir tutorías en sus áreas de experiencia.
- **Tutorado**: Puede solicitar tutorías en habilidades que desea desarrollar.
- **Administrador**: Gestiona el sistema.

### 2. Habilidades (Skills)

Representa las competencias o conocimientos que pueden ser objeto de tutoría.

### 3. Capítulos (Chapters)

Representa los departamentos o áreas de la organización a las que pertenecen los usuarios.

### 4. Solicitudes de Tutoría (Tutoring Requests)

Gestiona las peticiones de tutoría realizadas por los tutorados, con estados:
- **Enviada**: Estado inicial de una solicitud.
- **Aprobada**: La solicitud ha sido aprobada pero aún no asignada a un tutor.
- **Asignada**: Se ha asignado un tutor y se ha creado una tutoría.
- **Rechazada**: La solicitud ha sido rechazada.

### 5. Tutorías (Tutorings)

Representa las tutorías activas entre un tutor y un tutorado, con estados:
- **Activa**: La tutoría está en curso.
- **Completada**: La tutoría ha finalizado satisfactoriamente.
- **Cancelada**: La tutoría ha sido cancelada.

### 6. Sesiones de Tutoría (Tutoring Sessions)

Gestiona las sesiones individuales dentro de una tutoría, con estados:
- **Programada**: La sesión está planificada.
- **Realizada**: La sesión se ha llevado a cabo.
- **Cancelada**: La sesión ha sido cancelada.

### 7. Retroalimentación (Feedbacks)

Permite a los usuarios proporcionar evaluaciones y comentarios sobre las tutorías.

### 8. Estadísticas (Statistics)

Proporciona métricas y estadísticas del sistema para dashboards administrativos, incluyendo:
- Solicitudes por estado
- Tutorías por estado
- Tutores activos por capítulo

## Flujos Principales

### Flujo de Solicitud y Asignación de Tutoría

1. Un tutorado crea una solicitud de tutoría especificando las habilidades requeridas.
2. La solicitud se marca como "Enviada".
3. Un administrador revisa la solicitud y la aprueba o rechaza.
4. Si es aprobada, se asigna a un tutor disponible.
5. Se crea una tutoría vinculada a la solicitud.
6. La solicitud se marca como "Asignada".

### Flujo de Gestión de Tutorías

1. Una vez creada, la tutoría tiene estado "Activa".
2. Se pueden programar múltiples sesiones de tutoría.
3. Al finalizar, el tutor o tutorado puede marcar la tutoría como "Completada".
4. En cualquier momento, la tutoría puede ser "Cancelada".
5. Los usuarios pueden proporcionar retroalimentación sobre la tutoría.

## Requisitos de Instalación

### Prerrequisitos

- Java 21
- Maven 3.9+
- SQLite (incluido como dependencia)

### Configuración

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/tu-usuario/sistematutorias.git
   cd sistematutorias
   ```

2. Compilar el proyecto:
   ```bash
   mvn clean install
   ```

3. Ejecutar la aplicación:
   
   **Desarrollo local (SQLite):**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```
   
   **Desarrollo con MySQL:**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```
   
   **Script de desarrollo:**
   ```bash
   ./run-dev.sh
   ```

### Configuración con Docker

También puedes ejecutar la aplicación usando Docker:

**Usando Docker Compose (Recomendado):**
```bash
# Levantar toda la infraestructura (MySQL + App)
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down
```

**Usando Docker directamente:**
```bash
# Construir la imagen
docker build -t sistematutorias .

# Ejecutar el contenedor
docker run -p 8080:8080 sistematutorias
```

**Configuración de Base de Datos:**
- **Local**: SQLite (archivo `mydatabase.db`)
- **Desarrollo**: MySQL 8.0 con Docker Compose
- **Variables de entorno**: Configurables para diferentes entornos

## API REST

La API REST está completamente documentada con OpenAPI 3.0. La documentación incluye:
- Especificaciones completas de endpoints
- Esquemas de datos
- Ejemplos de request/response
- Códigos de estado HTTP
- Autenticación con Google ID

**Acceso a la documentación:**
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec**: Ver archivo `openapi-complete.yaml`

**Autenticación:**
La API utiliza autenticación basada en Google User ID enviado en el header `Authorization`. Todos los endpoints requieren autenticación excepto los de monitoreo (`/actuator/*`). Ver la sección [Autenticación con Google ID](#autenticación-con-google-id) para detalles completos.

### Endpoints Principales

**Endpoints Autenticados (requieren Google ID en header Authorization):**
- **Usuarios**: `/api/v1/users`
- **Habilidades**: `/api/v1/skills`
- **Capítulos**: `/api/chapter`
- **Solicitudes de Tutoría**: `/api/v1/tutoring-requests`
- **Tutorías**: `/api/v1/tutorings`
- **Sesiones de Tutoría**: `/api/v1/tutoring-sessions`
- **Retroalimentación**: `/api/v1/feedbacks`
- **Estadísticas**: `/api/v1/statistics`

**Endpoints Públicos (sin autenticación):**
- **Monitoreo**: `/actuator/health`

## Pruebas y Calidad de Código

El proyecto incluye pruebas unitarias completas y análisis de cobertura:

**Ejecutar pruebas:**
```bash
mvn test
```

**Generar reporte de cobertura:**
```bash
mvn clean test jacoco:report
```

**Ver reporte de cobertura:**
El reporte se genera en `target/site/jacoco/index.html`

**Cobertura actual:**
- Pruebas unitarias para todos los servicios
- Pruebas de controladores REST
- Pruebas de adaptadores de persistencia
- Pruebas de integración para autenticación
- Exclusión de mappers automáticos de la cobertura

## Tecnologías Utilizadas

- **Spring Boot 3.5.0**: Framework principal
- **Java 21**: Versión de Java
- **Spring Data JPA**: Persistencia de datos
- **SQLite**: Base de datos para desarrollo local
- **MySQL 8.0**: Base de datos para producción
- **Lombok**: Reducción de código boilerplate
- **MapStruct 1.5.5**: Mapeo entre objetos
- **Spring Boot Validation**: Validación de datos
- **Spring Boot Actuator**: Monitoreo y métricas
- **JUnit 5**: Pruebas unitarias
- **JaCoCo**: Cobertura de código
- **Docker**: Contenerización
- **OpenAPI 3.0**: Documentación de API

## Notas para Desarrolladores

### Arquitectura y Patrones
- El proyecto sigue los principios SOLID y Clean Architecture
- Implementa Arquitectura Hexagonal (Ports and Adapters)
- Utiliza el patrón Repository para acceso a datos
- Separación clara entre capas de dominio, aplicación e infraestructura

### Convenciones de Código
- DTOs para comunicación entre capas
- MapStruct para conversiones automáticas
- Lombok para reducir boilerplate
- Validación en múltiples niveles (API y dominio)
- Manejo centralizado de excepciones

### Configuración de Perfiles
- **local**: SQLite para desarrollo rápido
- **dev**: MySQL para entorno de desarrollo
- **test**: Configuración específica para pruebas

### Autenticación y Seguridad
- Interceptor personalizado para autenticación Google
- Context de usuario para operaciones autenticadas
- Configuración flexible de rutas protegidas
- Middleware de identificación de usuarios basado en Google ID

### Monitoreo
- Spring Boot Actuator habilitado
- Health checks disponibles en `/actuator/health`

## Autenticación con Google ID

### Configuración de Autenticación

El sistema utiliza un middleware de autenticación que identifica a los usuarios mediante Google User IDs enviados en el header `Authorization` de las peticiones HTTP. Este middleware se aplica automáticamente a todos los endpoints de la API.

#### Configuración del Sistema

La autenticación se configura automáticamente al iniciar la aplicación. El interceptor `GoogleAuthInterceptor` se registra para interceptar todas las peticiones a rutas que coincidan con el patrón `/api/**`.

**Propiedades de configuración disponibles:**

```properties
# application.properties
# Header de autorización (por defecto: Authorization)
app.auth.header-name=Authorization

# Patrones de rutas protegidas (por defecto: /api/**)
app.auth.protected-paths=/api/**
```

### Cómo Incluir Google ID en Peticiones API

Para realizar peticiones autenticadas a la API, debes incluir el Google User ID en el header `Authorization`:

#### Formato del Header

```http
Authorization: google_user_id_aqui
```

#### Ejemplos de Peticiones

**Usando cURL:**
```bash
curl -X GET "http://localhost:8080/api/v1/users" \
  -H "Authorization: 108234567890123456789" \
  -H "Content-Type: application/json"
```

**Usando JavaScript (fetch):**
```javascript
fetch('http://localhost:8080/api/v1/users', {
  method: 'GET',
  headers: {
    'Authorization': '108234567890123456789',
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log(data));
```

**Usando Postman:**
1. Agregar header `Authorization` con valor del Google User ID
2. Realizar la petición normalmente

#### Obtención del Google User ID

El Google User ID se obtiene típicamente del token JWT de Google después de la autenticación. En aplicaciones frontend:

```javascript
// Ejemplo con Google Sign-In
google.accounts.id.initialize({
  client_id: 'tu-client-id.googleusercontent.com',
  callback: (response) => {
    const payload = JSON.parse(atob(response.credential.split('.')[1]));
    const googleUserId = payload.sub; // Este es el ID que debes enviar
    
    // Usar googleUserId en peticiones API
    makeApiRequest(googleUserId);
  }
});
```

### Uso de UserContext para Desarrolladores

El sistema proporciona la clase `UserContext` para acceder a la información del usuario autenticado desde cualquier parte del código.

#### Acceso al Usuario Actual

```java
import com.pragma.shared.context.UserContext;
import com.pragma.usuarios.domain.model.User;

@RestController
public class MiController {
    
    @GetMapping("/mi-endpoint")
    public ResponseEntity<?> miEndpoint() {
        // Obtener el usuario autenticado
        User currentUser = UserContext.getCurrentUser();
        
        if (currentUser != null) {
            // Usar información del usuario
            String userId = currentUser.getId();
            String email = currentUser.getEmail();
            RolUsuario role = currentUser.getRol();
            
            // Lógica de negocio...
        }
        
        return ResponseEntity.ok().build();
    }
}
```

#### Métodos Disponibles en UserContext

```java
// Establecer usuario (usado internamente por el interceptor)
UserContext.setCurrentUser(user);

// Obtener usuario actual
User user = UserContext.getCurrentUser();

// Limpiar contexto (usado internamente por el interceptor)
UserContext.clear();
```

#### Consideraciones Importantes

1. **Thread Safety**: `UserContext` utiliza `ThreadLocal`, por lo que es seguro en entornos multi-hilo
2. **Limpieza Automática**: El contexto se limpia automáticamente después de cada petición
3. **Disponibilidad**: El usuario solo está disponible durante el procesamiento de peticiones autenticadas
4. **Null Safety**: Siempre verificar que `getCurrentUser()` no retorne `null`

#### Ejemplo de Servicio con UserContext

```java
@Service
public class MiServicio {
    
    public void operacionQueRequiereUsuario() {
        User currentUser = UserContext.getCurrentUser();
        
        if (currentUser == null) {
            throw new AuthenticationException("Usuario no autenticado");
        }
        
        // Verificar permisos basados en el rol
        if (currentUser.getRol() == RolUsuario.ADMINISTRADOR) {
            // Lógica para administrador
        } else if (currentUser.getRol() == RolUsuario.TUTOR) {
            // Lógica para tutor
        }
        
        // Continuar con la lógica de negocio...
    }
}
```

### Guía de Resolución de Problemas

#### Errores Comunes y Soluciones

**1. Error 401 - "Authorization header is required"**

**Causa:** No se incluyó el header `Authorization` en la petición.

**Solución:**
```bash
# ❌ Incorrecto
curl -X GET "http://localhost:8080/api/v1/users"

# ✅ Correcto
curl -X GET "http://localhost:8080/api/v1/users" \
  -H "Authorization: 108234567890123456789"
```

**2. Error 401 - "Invalid authorization header format"**

**Causa:** El header `Authorization` está vacío o tiene formato incorrecto.

**Solución:**
- Verificar que el Google User ID no esté vacío
- Asegurar que no hay espacios extra o caracteres especiales
- El valor debe ser solo el Google User ID (sin prefijos como "Bearer")

**3. Error 403 - "User not registered in the system"**

**Causa:** El Google User ID no corresponde a ningún usuario registrado en la base de datos.

**Solución:**
1. Verificar que el usuario existe en la tabla `users`
2. Confirmar que el campo `google_user_id` coincide exactamente
3. Registrar el usuario si es necesario:

```sql
-- Verificar usuario existente
SELECT * FROM users WHERE google_user_id = '108234567890123456789';

-- Registrar nuevo usuario (ejemplo)
INSERT INTO users (id, first_name, last_name, email, google_user_id, chapter_id, rol, active_tutoring_limit)
VALUES ('user-id', 'Nombre', 'Apellido', 'email@ejemplo.com', '108234567890123456789', 'chapter-id', 'TUTORADO', 3);
```

**4. Error 500 - "Internal server error occurred"**

**Causa:** Error en la base de datos o problema interno del sistema.

**Solución:**
1. Revisar logs de la aplicación
2. Verificar conectividad con la base de datos
3. Comprobar configuración de la aplicación

#### Debugging y Logs

**Habilitar logs de autenticación:**

```properties
# application.properties
logging.level.com.pragma.shared.security=DEBUG
logging.level.com.pragma.shared.context=DEBUG
```

**Logs típicos de autenticación exitosa:**
```
DEBUG - GoogleAuthInterceptor: Processing authentication for user: 108234567890123456789
DEBUG - UserContext: Setting current user: user@ejemplo.com
INFO  - Authentication successful for user: user@ejemplo.com
```

**Logs típicos de errores de autenticación:**
```
WARN  - GoogleAuthInterceptor: Missing Authorization header
WARN  - GoogleAuthInterceptor: User not found for Google ID: 108234567890123456789
ERROR - GoogleAuthInterceptor: Database error during user lookup
```

#### Herramientas de Diagnóstico

**1. Verificar configuración del interceptor:**
```bash
# Endpoint de health check (no requiere autenticación)
curl http://localhost:8080/actuator/health
```

**2. Probar autenticación con usuario conocido:**
```bash
# Usar un Google ID que sepas que existe en la BD
curl -X GET "http://localhost:8080/api/v1/users" \
  -H "Authorization: GOOGLE_ID_CONOCIDO" \
  -v  # Verbose para ver headers de respuesta
```

**3. Verificar base de datos:**
```sql
-- Listar usuarios con Google ID
SELECT id, email, google_user_id, rol FROM users WHERE google_user_id IS NOT NULL;

-- Verificar usuario específico
SELECT * FROM users WHERE google_user_id = 'TU_GOOGLE_ID';
```

#### Configuración de Desarrollo

Para desarrollo y pruebas, puedes configurar un usuario de prueba:

```sql
-- Usuario de prueba para desarrollo
INSERT INTO users (id, first_name, last_name, email, google_user_id, chapter_id, rol, active_tutoring_limit)
VALUES 
('dev-user-1', 'Usuario', 'Desarrollo', 'dev@test.com', 'dev-google-id-123', 'chapter-1', 'ADMINISTRADOR', 5);
```

Luego usar en peticiones de desarrollo:
```bash
curl -X GET "http://localhost:8080/api/v1/users" \
  -H "Authorization: dev-google-id-123"
```

## Contribución

Para contribuir al proyecto:

1. Crear una rama para tu funcionalidad: `git checkout -b feature/nueva-funcionalidad`
2. Realizar cambios y pruebas
3. Enviar un Pull Request

## Licencia

Este proyecto está licenciado bajo [Licencia Propietaria].