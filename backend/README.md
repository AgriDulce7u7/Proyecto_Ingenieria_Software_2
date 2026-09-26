# SIGCA-EPQ · Backend

API REST del prototipo **SIGCA-EPQ** (Empresas Públicas del Quindío) para Ingeniería de Software 2.
Implementa las dos funcionalidades completas del Plan de Requisitos:

| Funcionalidad | Requisitos cubiertos |
|---|---|
| **F-01** Generar facturas mensuales | RF-05, CU-03, RN-02, SWR-01, SWR-02, SWR-03, SWR-04 (IS-01) |
| **F-02** Registrar y dar seguimiento a PQR | RF-08, RF-09, RF-10, CU-06, CU-07, CO-01, RN-04, RN-06, DE-02, SWR-05 … SWR-09 |

**Stack:** Java 21 · Spring Boot 3.5 (Web, Data JPA, Validation, Actuator) · PostgreSQL 16 · springdoc-openapi (Swagger).

---

## 1. Cómo ejecutarlo

Requisitos: **JDK 21** y **Docker**. No hace falta instalar Maven (se incluye el wrapper `mvnw`).

```bash
# 1. Desde la raíz del repositorio: variables de entorno y base de datos
cp .env.example .env          # completar POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD y POSTGRES_PORT
docker compose up -d          # crea el esquema (01_schema.sql) y carga los datos de prueba (02_seed_data.sql)

# 2. Backend
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

- API: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Salud: `http://localhost:8080/actuator/health`

El backend lee las credenciales del mismo archivo `.env` que usa Docker (no hay credenciales en el código).
Todas las variables del `.env` deben tener valor.

> Los scripts de `database/init` solo se ejecutan cuando el volumen está vacío. Si la base ya existía
> antes de agregar `02_seed_data.sql`, recréala con `docker compose down -v && docker compose up -d`.

Pruebas: `./mvnw clean test` ejecuta las 328 pruebas unitarias y genera el reporte de cobertura en
`target/site/jacoco/index.html`. La estrategia de pruebas, la cobertura y la colección de Postman se
documentan en el [README principal](../README.md#4-pruebas).

### Configuración (`src/main/resources/application.yml`)

| Propiedad | Por defecto | Descripción |
|---|---|---|
| `app.pqr.horas-alerta-vencimiento` | `48` | Anticipación de la alerta al gestor (SWR-07) |
| `app.pqr.monitoreo-cron` | cada 15 min | Revisión de plazos y vencimientos |
| `app.facturacion.ejecucion-automatica` | `true` | Facturación automática del primer día hábil (SWR-01) |
| `app.facturacion.cron` | `0 0 1 * * *` | Hora diaria en que se evalúa si corresponde facturar |
| `app.facturacion.dias-para-vencimiento` | `15` | Días entre emisión y vencimiento de la factura |
| `app.facturacion.tiempo-maximo-lote-minutos` | `60` | Límite de SWR-02 (se reporta si se excede) |
| `app.erp.modo` | `simulado` | `simulado` o `rest` (ERP real vía HTTP) |
| `app.erp.max-intentos` | `3` | Reintentos por factura hacia el ERP |
| `app.erp.simulado.tasa-fallo` | `0.0` | Probabilidad de fallo del ERP simulado (para demostrar errores) |

---

## 2. Endpoints

### F-02 · PQR (`/api/pqr`)

| Método | Ruta | Uso |
|---|---|---|
| POST | `/api/pqr` | Ciudadano registra una PQR → radicado `YYYYMM-NNNN`, fecha límite y gestor asignado |
| GET | `/api/pqr/consulta/{radicado}` | Consulta **pública sin login** del estado (SWR-08): estado, fecha de recepción y fecha estimada |
| GET | `/api/pqr?estado=&tipo=&gestorId=&documento=` | Bandeja del gestor con filtros |
| GET | `/api/pqr/{radicado}` | Detalle, incluye `accionesDisponibles` según el estado |
| GET | `/api/pqr/{radicado}/historial` | Trazabilidad/auditoría (SWR-09) |
| GET | `/api/pqr/{radicado}/notificaciones` | Notificaciones enviadas |
| PATCH | `/api/pqr/{radicado}/tomar` | Radicado → En trámite |
| PATCH | `/api/pqr/{radicado}/reasignar` | Cambia el gestor responsable |
| PATCH | `/api/pqr/{radicado}/responder` | En trámite / Vencido → Resuelto (respuesta obligatoria, RN-06) |
| PATCH | `/api/pqr/{radicado}/cerrar` | Resuelto → Cerrado |
| POST | `/api/pqr/monitoreo` | Ejecuta a demanda el monitoreo de plazos (alertas 48 h y vencimientos) |
| GET | `/api/pqr/catalogos/{tipos-solicitud\|canales\|estados}` | Catálogos para formularios |
| GET | `/api/gestores` · `/api/gestores/{id}/notificaciones` | Gestores activos y su bandeja de alertas |

Ejemplo de registro:

```json
POST /api/pqr
{
  "tipoDocumento": "CC", "numeroDocumento": "1094000111", "nombreCompleto": "Ana Gómez",
  "correo": "ana@correo.com", "telefono": "3001234567", "direccion": "Cra 14 # 20-30, Armenia",
  "tipoSolicitud": "RECLAMO", "canal": "WEB",
  "asunto": "Cobro elevado en la factura",
  "descripcion": "El valor facturado este mes duplica mi consumo habitual sin explicación."
}
```

### F-01 · Facturación (`/api/facturacion`)

| Método | Ruta | Uso |
|---|---|---|
| GET | `/programacion` | Próxima ejecución automática, primer día hábil y estado del periodo pendiente |
| POST | `/lotes` | Genera el lote. Cuerpo opcional `{"periodo":"2026-08"}`; sin cuerpo factura el mes anterior |
| GET | `/lotes` · `/lotes/{periodo}` | Historial y detalle de lotes (duración, totales, estado de sincronización) |
| GET | `/lotes/{periodo}/facturas` | Facturas de un lote |
| POST | `/lotes/{periodo}/sincronizacion` | Reintenta el envío al ERP de las facturas pendientes o con error |
| GET | `/facturas?periodo=&contrato=&estado=&documento=` | Búsqueda de facturas |
| GET | `/facturas/{numero}` | Detalle: lecturas, consumo, tarifa y liquidación |
| POST | `/facturas/{numero}/sincronizacion` | Sincroniza una factura puntual con el ERP |
| GET | `/facturas/{numero}/sincronizaciones` | Bitácora de intentos con el ERP |
| GET | `/contratos?estado=ACTIVO` | Contratos de servicio |

**Demo con los datos de prueba** (`POST /api/facturacion/lotes` sin cuerpo): se evalúan los 7 contratos
activos del mes anterior, se generan **6 facturas** (p. ej. `CT-ACU-0001`: 21,50 m³ × $3.215,50 + $12.650 =
**$81.783,25**), se reporta **1 incidencia** (`CT-ACU-0005` sin lectura) y los 2 contratos suspendido/inactivo
quedan excluidos. Volver a ejecutarlo no duplica facturas.

Los errores siguen el formato estándar RFC 7807 (`ProblemDetail`): 400 datos inválidos, 404 no encontrado,
409 transición de estado inválida, 422 regla de negocio incumplida.

---

## 3. Arquitectura

Arquitectura por capas organizada **por módulo funcional** (package-by-feature):

```
co.edu.uniquindio.epq
├── comun/          calendario laboral colombiano, configuración, excepciones, conversores JPA, plantilla de tareas
├── pqr/            F-02
│   ├── dominio/        entidades ricas (Pqr) y máquina de estados (EstadoPqrTipo)
│   ├── repositorio/    Spring Data JPA + Specifications
│   ├── servicio/       interfaces (ISP) + impl/, plazo/, asignacion/, radicado/
│   ├── evento/         eventos de dominio y listeners (auditoría, notificaciones)
│   ├── notificacion/   canal de notificación (adaptador)
│   ├── tarea/          monitoreo programado de plazos
│   └── web/            controladores REST y DTOs
└── facturacion/    F-01
    ├── dominio/        Contrato, Tarifa, LecturaMedidor, LoteFacturacion, Factura, SincronizacionErp
    ├── repositorio/
    ├── servicio/       calculo/, numeracion/, generacion/, sincronizacion/, impl/
    ├── erp/            puerto y adaptadores del ERP financiero (IS-01)
    ├── tarea/          facturación automática mensual
    └── web/
```

### Patrones de diseño aplicados

| Patrón | Dónde | Para qué |
|---|---|---|
| **State** | `EstadoPqrTipo` | Cada estado define sus transiciones válidas (DE-02); `Pqr` rechaza cambios ilegales con 409 |
| **Strategy** | `PoliticaPlazoRespuesta` (`PlazoPeticion`, `PlazoQuejaReclamo`) | Plazo normativo por tipo de PQR (RN-04) |
| **Strategy** | `EstrategiaAsignacionGestor` (`AsignacionPorMenorCarga`) | Política de asignación automática del gestor |
| **Strategy** | `EstrategiaCalculoFactura` (`CalculoPorConsumoMedido`) + `SelectorEstrategiaCalculo` | Fórmula de liquidación (SWR-03); una fórmula por estrato se agrega sin tocar el proceso |
| **Observer** | `PqrRegistradaEvento`, `PqrAsignadaEvento`, `PqrEstadoCambiadoEvento` + listeners | Auditoría (SWR-09) y notificaciones desacopladas del caso de uso |
| **Adapter / Port** | `ErpFinancieroGateway` (simulado / REST), `CanalNotificacion` | Aíslan sistemas externos; se cambian por configuración |
| **Facade** | `GeneracionFacturacionServiceImpl` | Un punto de entrada que coordina lote, liquidación y sincronización ERP |
| **Template Method** | `TareaProgramadaTemplate` | Esqueleto común de las tareas programadas (condición, medición, log, aislamiento de errores) |
| **Specification** | `PqrSpecifications`, `FacturaSpecifications` | Filtros de búsqueda combinables |
| **Factory Method** | `Pqr.radicar`, `Factura.emitir`, `LoteFacturacion.abrir` | Creación de entidades siempre en un estado válido |
| **DTO / Mapper** | `web/dto`, `PqrMapper`, `FacturacionMapper` | La API no expone entidades JPA ni datos personales innecesarios |

### Principios SOLID

- **S — Responsabilidad única:** `FacturadorContrato` liquida un contrato, `GestorLoteFacturacion` administra el lote,
  `SincronizadorFacturaErp` habla con el ERP, `CalculadoraPlazoRespuesta` calcula plazos; los controladores solo traducen HTTP.
- **O — Abierto/cerrado:** nuevas políticas de plazo, asignación, cálculo tarifario, numeración o canales de notificación
  se agregan como nuevas implementaciones (Spring las inyecta) sin modificar el código existente. `ConvertidorCodigoPersistible`
  sirve para cualquier enum nuevo.
- **L — Sustitución de Liskov:** los adaptadores `ErpFinancieroSimuladoAdapter` y `ErpFinancieroRestAdapter` son
  intercambiables; cualquier `CalendarioLaboral` o `EstrategiaCalculoFactura` cumple el mismo contrato.
- **I — Segregación de interfaces:** servicios pequeños por rol (`RegistroPqrService`, `ConsultaPqrService`,
  `GestionPqrService`, `MonitoreoPlazosPqrService`; `GeneracionFacturacionService`, `SincronizacionErpService`,
  `ConsultaFacturacionService`) separando comandos de consultas.
- **D — Inversión de dependencias:** todo se inyecta por constructor contra abstracciones (`CalendarioLaboral`,
  `ErpFinancieroGateway`, `GeneradorRadicado`, `GeneradorNumeroFactura`, `Clock`), lo que permite pruebas con un reloj fijo y mocks.

### Decisiones relevantes

- **Plazos PQR (RN-04):** 10 días hábiles para peticiones y 15 para quejas/reclamos, contados desde el día siguiente
  excluyendo fines de semana y festivos colombianos (Ley Emiliani y festivos de Pascua calculados, sin tablas manuales).
- **Alerta de 48 h (SWR-07):** se envía una sola vez por PQR; al vencer el plazo la PQR pasa a *Vencido* y se audita
  para el reporte a la SSPD (CU-06 9a). Se admite respuesta extemporánea (DE-02).
- **Facturación (SWR-01/02):** la tarea diaria factura el mes de consumo anterior a partir del primer día hábil
  mientras el lote no esté completado (recupera ejecuciones fallidas). Cada contrato se liquida en su propia
  transacción: un error no detiene el lote y queda como incidencia. La generación es idempotente
  (número `FAC-yyyyMM-NNNNNN`, una factura por contrato y periodo).
- **ERP (SWR-04):** cada intento se registra en `sincronizacion_erp` con reintentos configurables; la factura queda
  `SINCRONIZADA` o `ERROR_SINCRONIZACION` y puede reintentarse después.

---

## 4. Limitaciones del prototipo

- **Sin autenticación ni roles (RBAC):** los endpoints de back-office quedan abiertos; en los que registran acciones,
  el usuario se envía en la petición. Se incorporaría con Spring Security.
- **Tarifa por estrato y municipio (RN-08):** el esquema no tiene estrato/municipio en `tarifa`; se aplica la tarifa
  vigente por servicio. La estrategia de cálculo permite añadirlo sin cambiar el proceso.
- **Evidencia adjunta en la respuesta de PQR (RN-06):** el esquema no tiene tabla de adjuntos; se exige la respuesta formal escrita.
- **Gestor por municipio (RF-08):** `gestor` no tiene municipio; se asigna al gestor activo con menor carga.
- **Notificaciones y ERP simulados:** el correo se registra en la tabla `notificacion` y en el log; el ERP real se
  habilita con `app.erp.modo=rest`.
- **Una sola instancia:** el radicado consecutivo y el bloqueo del lote de facturación se sincronizan en memoria.
