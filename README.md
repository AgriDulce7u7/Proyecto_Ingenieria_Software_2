# SIGCA-EPQ

Prototipo desarrollado para **Empresas Públicas del Quindío (EPQ)** en la asignatura **Ingeniería de Software 2**
de la Universidad del Quindío. Continúa el proyecto de Ingeniería de Software 1 e implementa dos funcionalidades
completas del Plan de Requisitos:

| Funcionalidad | Descripción |
|---|---|
| **F-01** Generar facturas mensuales | Facturación automática el primer día hábil del mes, liquidación por consumo medido y sincronización con el ERP financiero |
| **F-02** Registrar y dar seguimiento a PQR | Radicación de peticiones, quejas y reclamos, asignación automática de gestor, control de plazos legales, alertas y trazabilidad |

---

## Contenido

1. [Estructura del repositorio](#1-estructura-del-repositorio)
2. [Stack tecnológico](#2-stack-tecnológico)
3. [Puesta en marcha](#3-puesta-en-marcha)
4. [Pruebas](#4-pruebas)
5. [Solución de problemas](#5-solución-de-problemas)
6. [Flujo de trabajo con Git](#6-flujo-de-trabajo-con-git)
7. [Equipo](#7-equipo)

---

## 1. Estructura del repositorio

```
.
├── backend/                  API REST en Spring Boot → ver backend/README.md
│   ├── src/main/java/        Código fuente (módulos comun, pqr, facturacion)
│   └── src/test/java/        Pruebas unitarias y de capa web
├── frontend/                 Aplicación web en React (en construcción)
├── database/
│   └── init/
│       ├── 01_schema.sql     Esquema completo: PQR + Facturación
│       └── 02_seed_data.sql  Datos de prueba
├── postman/
│   ├── SIGCA-EPQ.postman_collection.json      Colección de pruebas de la API
│   └── SIGCA-EPQ-local.postman_environment.json
├── docker-compose.yml        Contenedor de PostgreSQL
└── .env.example              Plantilla de variables de entorno
```

La documentación técnica del backend (endpoints, arquitectura, patrones de diseño, principios SOLID, decisiones
y limitaciones) está en [`backend/README.md`](backend/README.md).

---

## 2. Stack tecnológico

| Capa | Tecnologías |
|---|---|
| Base de datos | PostgreSQL 16 en Docker |
| Backend | Java 21, Spring Boot 3.5 (Web, Data JPA, Validation, Actuator), springdoc-openapi |
| Frontend | React (en construcción) |
| Pruebas | JUnit 5, Mockito, Spring MockMvc, JaCoCo, Postman |

---

## 3. Puesta en marcha

### Requisitos

- **Docker Desktop**
- **JDK 21** (no hace falta instalar Maven: el backend incluye el wrapper `mvnw`)
- **Node.js** (para el frontend y, opcionalmente, para ejecutar Postman por consola)
- (Opcional) **Postman**, **DBeaver** o **pgAdmin**

### 3.1 Base de datos

Desde la raíz del repositorio:

```bash
cp .env.example .env
```

Edita `.env` y asigna un valor a todas las variables:

```
POSTGRES_DB=epq_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=tu_contraseña
POSTGRES_PORT=5440
```

> **Sobre el puerto:** si tienes PostgreSQL instalado en tu equipo, ya usa el `5432` (y a veces el `5433`).
> Usa un puerto libre para el contenedor. Puedes comprobarlo con `netstat -ano | findstr :5440` (Windows)
> o `lsof -i :5440` (Linux/macOS). Ver [Solución de problemas](#5-solución-de-problemas).

Levanta el contenedor:

```bash
docker compose up -d
docker ps
```

Debe aparecer el contenedor `epq_db` con estado `Up`. La primera vez, PostgreSQL ejecuta automáticamente
`01_schema.sql` (17 tablas) y `02_seed_data.sql` (datos de prueba).

Para verificar que los datos se cargaron:

```bash
docker exec -it epq_db psql -U postgres -d epq_db -c "SELECT COUNT(*) FROM contrato;"
```

Debe devolver `9`.

> Los scripts de `database/init` solo se ejecutan cuando el volumen está vacío. Para recrear la base desde cero:
> `docker compose down -v && docker compose up -d`

**Módulos del esquema**

- **PQR:** `tipo_solicitud`, `canal_atencion`, `estado_pqr`, `ciudadano`, `gestor`, `pqr`, `historial_pqr`, `notificacion`
- **Facturación:** `cliente`, `servicio`, `tarifa`, `contrato`, `medidor`, `lectura_medidor`, `lote_facturacion`, `factura`, `sincronizacion_erp`

**Conexión con DBeaver o pgAdmin:** host `localhost`, y el puerto, la base de datos, el usuario y la contraseña de tu `.env`.

### 3.2 Backend

```bash
cd backend
./mvnw spring-boot:run        # Windows (CMD/PowerShell): mvnw.cmd spring-boot:run
```

El backend lee las credenciales del mismo `.env` de la raíz. Cuando arranque:

| Recurso | URL |
|---|---|
| API | `http://localhost:8080/api` |
| Documentación Swagger | `http://localhost:8080/swagger-ui.html` |
| Estado de salud | `http://localhost:8080/actuator/health` → `{"status":"UP"}` |

### 3.3 Frontend

> En construcción. Esta sección se completará con la instalación y ejecución de la aplicación React.

---

## 4. Pruebas

El backend se valida en dos niveles complementarios:

| Nivel | Herramienta | Qué valida | Requiere BD |
|---|---|---|---|
| Unitarias y de capa web | JUnit 5, Mockito, MockMvc | Reglas de negocio, servicios, controladores, validaciones y manejo de errores, de forma aislada | No |
| Funcionales de la API | Postman | Flujos completos de extremo a extremo contra el backend real y PostgreSQL | Sí |

### 4.1 Pruebas unitarias

```bash
cd backend
./mvnw clean test
```

Resultado esperado: **328 pruebas, 0 fallos**. No necesitan la base de datos ni el backend en ejecución.

**Qué se prueba**

| Tipo | Clases | Enfoque |
|---|---|---|
| Dominio | `Pqr`, `EstadoPqrTipo`, `LoteFacturacion` | Máquina de estados, transiciones válidas e inválidas, reglas RN-04 y RN-06, sin mocks |
| Servicios | `RegistroPqrServiceImpl`, `GestionPqrServiceImpl`, `MonitoreoPlazosPqrServiceImpl`, `FacturadorContrato`, `SincronizadorFacturaErp`, servicios de consulta | Casos de uso con repositorios simulados (Mockito) y un `Clock` fijo para fechas deterministas |
| Capa web | `PqrController`, `CatalogoPqrController`, `FacturacionController` | `@WebMvcTest`: rutas, códigos HTTP, validación de DTOs, JSON y errores RFC 7807 |
| Eventos y notificaciones | Listeners de auditoría y notificación, `ServicioNotificacionPqr` | Trazabilidad (SWR-09) y que un fallo de notificación no afecte la operación |
| Tareas programadas | `TareaProgramadaTemplate`, facturación mensual, monitoreo de plazos | Condiciones de ejecución (primer día hábil, lote completado) y aislamiento de errores |
| Integración con el ERP | Adaptador simulado y adaptador REST | El adaptador REST se prueba contra un servidor HTTP local real (200, 204, 4xx, 5xx y ERP caído) |
| Infraestructura | Conversores JPA, calendario laboral colombiano | Conversión de enums y periodos; festivos y días hábiles |

**Convenciones**

- Cada clase de prueba agrupa los casos por operación con `@Nested` y describe cada caso con `@DisplayName` en español.
- Los datos de prueba se construyen con fábricas reutilizables (`DatosPruebaPqr`, `DatosPruebaFacturacion`) que
  crean entidades reales del dominio con los mismos valores del seed.
- Los casos de error verifican, además de la excepción, que el estado no cambie y que no se ejecuten efectos
  secundarios (guardar, publicar eventos, llamar al ERP).

### 4.2 Cobertura (JaCoCo)

`./mvnw test` genera el reporte automáticamente en:

```
backend/target/site/jacoco/index.html
```

En Windows se abre con `start target/site/jacoco/index.html` desde la carpeta `backend`.

**Cobertura actual**

| Métrica | Cobertura |
|---|---|
| Instrucciones | **94 %** |
| Ramas | **89 %** |

| Área | Instrucciones |
|---|---|
| Servicios, eventos, notificaciones, tareas, ERP, controladores y conversores | 94 – 100 % |
| Dominio PQR | 98 % |
| Dominio facturación | 83 % |
| Repositorios (`Specifications`) | 25 % |

Se excluyen del cálculo las clases sin lógica: la clase principal, la configuración de Spring, las clases
`*Properties` y los DTOs.

Los repositorios tienen baja cobertura unitaria a propósito: sus `Specifications` construyen consultas JPA que
solo tienen sentido contra PostgreSQL real, así que se validan con la colección de Postman, que ejercita todos
los filtros de búsqueda.

### 4.3 Pruebas de la API con Postman

Requiere la base de datos y el backend en ejecución (secciones 3.1 y 3.2).

1. En Postman, **Import** → selecciona los dos archivos de la carpeta `postman/`.
2. Arriba a la derecha, selecciona el entorno **SIGCA-EPQ Local**.
3. Clic derecho en la colección → **Run collection** → verifica que el entorno también esté seleccionado en el
   Runner → **Run**.

Resultado esperado: **52 peticiones y 244 aserciones**, todas exitosas.

| Carpeta | Contenido |
|---|---|
| 00 - Salud | Health check |
| 01 - PQR Catálogos | Tipos, canales, estados y gestores activos |
| 02 - PQR Flujo completo | Radicado → En trámite → reasignación → Resuelto → Cerrado, historial y notificaciones |
| 03 - PQR Casos de error | Validaciones (400), recursos inexistentes (404), transiciones inválidas (409) y reglas de negocio (422) |
| 04 - Facturación | Generación del lote, idempotencia, liquidación de `CT-ACU-0001` ($81.783,25) y sincronización con el ERP |
| 05 - Facturación Casos de error | Periodos inválidos, recursos inexistentes y facturas ya sincronizadas |

Las carpetas deben ejecutarse en orden, porque comparten variables (radicado, gestor, periodo y número de
factura). Los conteos de facturación asumen una base recién creada con el seed.

**Ejecución por consola (opcional)**, con [Newman](https://www.npmjs.com/package/newman):

```bash
npx newman run postman/SIGCA-EPQ.postman_collection.json \
    -e postman/SIGCA-EPQ-local.postman_environment.json
```

---

## 5. Solución de problemas

| Síntoma | Causa | Solución |
|---|---|---|
| El backend falla con `la autentificación password falló para el usuario «postgres»` (mensaje en español) | El backend se está conectando a un PostgreSQL instalado en Windows, no al contenedor. La imagen de Docker responde en inglés | Cambia `POSTGRES_PORT` en `.env` a un puerto libre (por ejemplo `5440`) y recrea el contenedor con `docker compose down` y `docker compose up -d` |
| `docker compose up` falla porque el puerto está ocupado | Otro servicio usa ese puerto | Revisa con `netstat -ano \| findstr :PUERTO` y usa uno libre |
| Hay datos incompletos o faltan tablas | El volumen ya existía antes de agregar algún script | `docker compose down -v && docker compose up -d` |
| Postman responde `getaddrinfo ENOTFOUND {{baseUrl}}` | No hay entorno seleccionado | Selecciona **SIGCA-EPQ Local** en el editor y en el Runner |
| Los conteos de facturación en Postman no coinciden | La base ya tenía lotes generados de ejecuciones anteriores | Recrea la base con `docker compose down -v && docker compose up -d` |

---

## 6. Flujo de trabajo con Git

- `main` contiene la versión integrada y estable.
- Cada integrante trabaja en una rama `feature/<descripcion>` y la integra mediante Pull Request.
- Los mensajes de commit siguen [Conventional Commits](https://www.conventionalcommits.org/es/v1.0.0/):

| Prefijo | Uso |
|---|---|
| `feat:` | Nueva funcionalidad |
| `fix:` | Corrección de un error |
| `test:` | Pruebas nuevas o modificadas |
| `build:` | Dependencias o configuración de Maven |
| `docs:` | Documentación |
| `refactor:` | Cambio de código sin alterar el comportamiento |

El alcance va entre paréntesis cuando aplica, por ejemplo `test(pqr): ...` o `fix(config): ...`.

- El archivo `.env` está en `.gitignore` y **nunca** debe subirse al repositorio.

---

## 7. Equipo

| Integrante | Responsabilidades |
|---|---|
| Angelica María Reyes | Base de datos y Docker Compose |
| Steven Severino | Backend, pruebas (unitarias) |
| Andrés Felipe Zambrano | Frontend, pruebas (unitarias y Postman) y documentación del proyecto |
