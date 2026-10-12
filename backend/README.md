# SIGCA-EPQ · Backend

API REST del prototipo **SIGCA-EPQ** (Empresas Públicas del Quindío) para Ingeniería de Software 2.
La **entrega 2** implementa las ocho funcionalidades del Plan de Requisitos (`Parcial_2Plan_de_Requisitos_EPQ.xlsx`):

| Funcionalidad | Requisitos cubiertos |
|---|---|
| **F-01** Generar facturas mensuales | RF-05, CU-03, RN-02, RN-08, SWR-01 … SWR-04 (IS-01), **SWR-10** |
| **F-02** Registrar y dar seguimiento a PQR | RF-08 … RF-10, CU-06, CU-07, RN-04, RN-06, DE-02, SWR-05 … SWR-09, **SWR-11**, SyR-02b, SyR-02f |
| **F-03** Registrar usuario del servicio | RF-01 … RF-03, CU-01, IS-03 (RNEC), SWR-12 … SWR-16 |
| **F-04** Gestionar contrato de servicio | RF-04, CU-02, RN-01, RN-08, SWR-17 … SWR-20 |
| **F-05** Registrar pago de factura | RF-06, CU-04, RN-05, IS-02 (pasarela), SWR-21 … SWR-24 |
| **F-06** Consultar factura e historial de consumos | RF-07, CU-05, SWR-25 … SWR-28 |
| **F-07** Gestionar cobro persuasivo | RF-11, RF-12, CU-08, RN-03, RN-07, SWR-29 … SWR-32 |
| **F-08** Consultar dashboard gerencial | RF-13, CU-09, SWR-33 … SWR-35 |
| Transversal | RF-14 (roles), RF-15 (log de auditoría), RNF-05 (control de acceso) |

> **Corrección de la entrega 1 — plazos de PQR en días hábiles.** El backend ya calculaba los plazos en días
> hábiles, pero los **datos semilla** usaban intervalos de días calendario (`+ INTERVAL '15 days'`), así que las
> PQR de ejemplo mostraban fechas límite que contaban sábados y domingos. Ahora `database/init/00_funciones_calendario.sql`
> replica en SQL el mismo calendario laboral colombiano del backend (festivos fijos, Ley Emiliani y festivos de
> Pascua) y el seed usa `epq_sumar_dias_habiles(...)`: **10 días hábiles para peticiones y 15 para quejas y
> reclamos, contados desde el día siguiente a la radicación, sin sábados, domingos ni festivos** (SWR-06, RN-04).
> Se agregaron pruebas que lo demuestran (radicación en sábado, Semana Santa y fin de año).

**Stack:** Java 21 · Spring Boot 3.5 (Web, Data JPA, Validation, Security, OAuth2 Resource Server, Actuator) ·
PostgreSQL 16 · JWT (HS256) · OpenPDF (PDF) · Apache POI (Excel) · springdoc-openapi (Swagger).

---

## 1. Cómo ejecutarlo

Requisitos: **JDK 21** y **Docker**. No hace falta instalar Maven (se incluye el wrapper `mvnw`).

```bash
# 1. Desde la raíz del repositorio
cp .env.example .env          # completar POSTGRES_* y (opcional) JWT_SECRET
docker compose down -v        # IMPORTANTE si ya tenía la base de la entrega 1: los scripts nuevos solo corren con el volumen vacío
docker compose up -d          # ejecuta database/init/00 … 04

# 2. Backend
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

- API: `http://localhost:8080/api` · Swagger UI: `http://localhost:8080/swagger-ui.html` (botón **Authorize** con el token)
- Salud: `http://localhost:8080/actuator/health`

| Script | Contenido |
|---|---|
| `00_funciones_calendario.sql` | Funciones del calendario laboral colombiano (`epq_festivos`, `epq_es_dia_habil`, `epq_sumar_dias_habiles`…) |
| `01_schema.sql` / `02_seed_data.sql` | Esquema y datos de la entrega 1 (plazos de PQR corregidos a días hábiles) |
| `03_schema_entrega2.sql` | Municipios, funcionarios y roles, log de auditoría, predios, historial de contratos, pagos, intentos de pago en línea, acuerdos de pago y cuotas, procesos de cobro, evidencias de PQR, notificaciones internas y secuencias |
| `04_seed_entrega2.sql` | 10 municipios, 9 funcionarios, 6 usuarios con predios, 24 meses de lecturas y facturas, pagos, escenarios de mora y acuerdos, 44 PQR históricas |

### Usuarios de prueba

| Usuario | Rol | Contraseña |
|---|---|---|
| `admin` | Administrador del sistema | `Epq2026*` |
| `asesor` | Asesor de Comercialización | `Epq2026*` |
| `lgiraldo` · `aocampo` · `mcastano` | Gestor de PQR (Montenegro · Salento · Filandia) | `Epq2026*` |
| `phenao` | Jefe de la Oficina de PQR | `Epq2026*` |
| `cartera` | Gestor de Cartera | `Epq2026*` |
| `gerente` · `directivo` | Gerente · Directivo | `Epq2026*` |
| Ciudadano `1094950001` (y USR-000002 … USR-000005) | Portal de autogestión | `Ciudadano2026*` |

```http
POST /api/auth/login              {"usuario":"admin","contrasena":"Epq2026*"}
POST /api/auth/ciudadano/login    {"numeroDocumento":"1094950001","contrasena":"Ciudadano2026*"}
→ {"token":"eyJ…","tipoToken":"Bearer","expiraEn":"…","usuario":{"rol":"ADMINISTRADOR","permisos":[…],…}}
```

Las demás peticiones envían `Authorization: Bearer <token>`. Tres contraseñas erradas seguidas bloquean la
cuenta 15 minutos (423).

### Configuración (`src/main/resources/application.yml`)

| Propiedad | Por defecto | Descripción |
|---|---|---|
| `app.seguridad.jwt.secreto` | `${JWT_SECRET:…}` | Clave HS256 (≥ 32 bytes). **Cambiarla fuera de desarrollo** |
| `app.seguridad.jwt.expiracion-minutos` | `480` | Duración de la sesión |
| `app.seguridad.max-intentos-fallidos` / `minutos-bloqueo` | `3` / `15` | Bloqueo temporal de cuentas |
| `app.pqr.horas-alerta-vencimiento` | `48` | Alerta al gestor y al jefe (SWR-07, SWR-11) |
| `app.pqr.evidencias.exigir-para-responder` | `true` | RN-06: no se responde sin soporte adjunto |
| `app.pqr.evidencias.tamano-maximo-bytes` | `5242880` | Tamaño máximo por archivo (PDF, JPG, PNG, DOC, DOCX) |
| `app.rnec.modo` | `simulado` | `simulado` o `rest`. En el simulado, un documento que termina en `99` simula la RNEC caída y en `00` una identidad no encontrada |
| `app.pasarela.modo` / `timeout-segundos` | `simulada` / `30` | Pasarela de pagos (`simulada` o `rest`) |
| `app.cartera.periodos-para-suspension` | `2` | RN-03 |
| `app.cartera.cuota-minima` / `min-cuotas` / `max-cuotas` | `50000` / `2` / `12` | Cálculo automático del acuerdo (SWR-30) |
| `app.cartera.monitoreo-cron` | `0 30 2 * * *` | Vencimiento de facturas, acuerdos incumplidos y candidatos a suspensión |
| `app.facturacion.*`, `app.erp.*` | — | Igual que en la entrega 1 |

---

## 2. Control de acceso (RF-14, RNF-05)

Autenticación **sin estado con JWT** (Spring Security + OAuth2 Resource Server). Los permisos se declaran con
meta-anotaciones legibles sobre cada endpoint (`@AccesoComercial`, `@AccesoCartera`, `@AccesoGerencial`…), que
envuelven `@PreAuthorize`. Un gestor de PQR solo puede **tomar y responder** en nombre de su propio registro de
gestor (`@autorizacionPqr.puedeActuarComo(...)`); el jefe y el administrador, en nombre de cualquiera.

| Operación | Admin | Asesor | Gestor PQR | Jefe PQR | Cartera | Gerente | Directivo | Ciudadano |
|---|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
| Radicar y consultar PQR por radicado | público | | | | | | | |
| Bandeja, detalle, evidencias, cerrar PQR | ✓ | | ✓ | ✓ | | | | |
| Tomar / responder PQR | ✓ | | solo las suyas | ✓ | | | | |
| Reasignar, monitoreo, alertas del equipo | ✓ | | | ✓ | | | | |
| Indicador de cumplimiento de plazos PQR | ✓ | | | ✓ | | ✓ | ✓ | |
| Consultar usuarios y contratos | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | |
| Registrar/modificar usuarios y contratos (SWR-15, SWR-20) | ✓ | ✓ | | | | | | |
| Consultar facturas y pagos | ✓ | ✓ | | | ✓ | ✓ | ✓ | |
| Generar lote, sincronizar ERP, anular factura, pago presencial | ✓ | ✓ | | | | | | |
| Reporte de cartera morosa (SWR-29) | | | | | ✓ | ✓ | | |
| Acuerdos de pago, procesos de cobro, monitoreo de cartera | ✓ | | | | ✓ | | | |
| Pagar cuota de un acuerdo | ✓ | ✓ | | | ✓ | | | |
| Dashboard gerencial (SWR-33) | | | | | | ✓ | ✓ | |
| Funcionarios, roles y log de auditoría | ✓ | | | | | | | |
| Portal: sus contratos, facturas, historial, pago en línea | | | | | | | | ✓ |

Errores de seguridad en formato RFC 7807: **401** sin token o token inválido, **403** rol insuficiente,
**423** cuenta bloqueada.

---

## 3. Endpoints

Todos con prefijo `/api`. La documentación interactiva completa está en Swagger.

### Autenticación y administración

| Método | Ruta | Uso |
|---|---|---|
| POST | `/auth/login` · `/auth/ciudadano/login` | Login de funcionario · de ciudadano (portal) |
| POST | `/auth/ciudadano/activacion` | El ciudadano crea su contraseña del portal (documento + correo registrado) |
| GET · POST | `/auth/perfil` · `/auth/logout` | Perfil y permisos del token · cierre de sesión auditado |
| GET | `/admin/roles` | Roles y permisos |
| GET · POST · PUT | `/admin/funcionarios[/{id}]` | Listar, crear y actualizar funcionarios |
| PATCH · POST | `/admin/funcionarios/{id}/rol` · `/estado` · `/contrasena` | Cambiar rol, activar/desactivar, restablecer contraseña |
| GET | `/admin/auditoria?usuario=&modulo=&accion=&entidad=&desde=&hasta=&soloFallidos=&pagina=` | Log de auditoría paginado (RF-15) |

### F-01 · Facturación (`/facturacion`)

Endpoints de la entrega 1 más:

| Método | Ruta | Uso |
|---|---|---|
| GET | `/facturas?estadoCobro=PENDIENTE\|VENCIDA\|PAGADA\|ANULADA` | Nuevo filtro por estado de cobro (DE-01) |
| POST | `/facturas/{numero}/anulacion` | Anular con motivo (auditado) |
| GET | `/facturas/{numero}/pdf` | Factura en PDF con el formato de EPQ (SWR-27) |

Cada ejecución de generación masiva queda en el log de auditoría con inicio, fin y cantidad de facturas (SWR-10).

### F-02 · PQR (`/pqr`)

Endpoints de la entrega 1 más:

| Método | Ruta | Uso |
|---|---|---|
| POST | `/pqr` | Ahora acepta `numeroContrato` y `municipioId` (opcionales) para asignar al gestor del municipio (SyR-02b) |
| POST · GET | `/pqr/{radicado}/evidencias` | Adjuntar (multipart `archivo` + `descripcion`) y listar evidencias (RN-06) |
| GET | `/pqr/{radicado}/evidencias/{id}/archivo` | Descargar una evidencia |
| GET | `/pqr/supervision/alertas?jefeId=` | PQR pendientes del equipo con horas restantes y nivel de alerta (SWR-11) |
| GET | `/pqr/indicadores/cumplimiento?desde=&hasta=` | % respondidas en plazo, por tipo y municipio, e incumplimientos (SyR-02f) |
| GET | `/pqr/indicadores/cumplimiento/exportar?formato=pdf\|excel` | Exportar los incumplimientos |

### F-03 · Usuarios del servicio (`/usuarios-servicio`) y F-04 · Contratos

| Método | Ruta | Uso |
|---|---|---|
| POST | `/usuarios-servicio` | Registrar usuario + predio; valida unicidad (409 con el perfil existente) y RNEC; si la RNEC no responde queda **pendiente** con advertencia (SWR-12 … SWR-14) |
| GET | `/usuarios-servicio?identificacion=&nombre=&direccion=` | Búsqueda indexada (SWR-16) |
| GET · PUT | `/usuarios-servicio/{codigo}` | Perfil completo · actualizar datos (auditoría campo por campo, SWR-15) |
| PATCH | `/usuarios-servicio/{codigo}/desactivacion` · `/reactivacion` | Con motivo |
| POST | `/usuarios-servicio/{codigo}/validacion-rnec` | Reintentar la validación pendiente |
| GET · POST | `/usuarios-servicio/{codigo}/predios` | Predios del usuario |
| POST | `/contratos` | Registrar contrato: RN-01 sin duplicados (409), tarifa automática (SWR-18), queda PENDIENTE si falta el medidor (SWR-19) |
| GET | `/contratos?estado=&servicio=&usuario=&municipioId=&candidatoSuspension=` | Búsqueda |
| GET | `/contratos/{numero}` · `/contratos/{numero}/historial` | Detalle con acciones disponibles · historial de estados |
| PATCH | `/contratos/{numero}/estado` | ACTIVO, SUSPENDIDO o CANCELADO con motivo (SWR-20) |
| PUT | `/contratos/{numero}/medidor` | Instalar/cambiar medidor y, opcionalmente, activar |
| GET | `/tarifas?servicio=&fecha=` · `/tarifas/aplicable?servicio=&municipioId=&estrato=` | Tarifas vigentes y la más específica |
| GET | `/municipios` | Municipios atendidos (público) |

### F-05 · Pagos (`/pagos`)

| Método | Ruta | Uso |
|---|---|---|
| POST | `/pagos/presenciales` | Pago en caja o banco por el valor total; rechaza facturas pagadas o anuladas (SWR-23, SWR-24) |
| GET | `/pagos?factura=&contrato=&canal=&desde=&hasta=` | Consulta de pagos |
| GET | `/pagos/{codigo}/comprobante[/pdf]` | Comprobante electrónico (SWR-22) |
| GET | `/pagos/intentos?factura=` | Intentos de pago en línea (aprobados, rechazados, sin respuesta) |

### F-06 · Portal del ciudadano (`/portal`, rol CIUDADANO)

| Método | Ruta | Uso |
|---|---|---|
| GET · PUT | `/portal/perfil` · `/portal/perfil/contacto` | Perfil y datos de contacto |
| GET | `/portal/contratos` | Contratos del ciudadano |
| GET | `/portal/contratos/{numero}/factura-actual` | Factura del periodo (SWR-25) o fecha estimada de disponibilidad (SWR-28) |
| GET | `/portal/contratos/{numero}/historial-consumos?meses=24` | Serie para gráfica y tabla (SWR-26) |
| GET | `/portal/facturas[?contrato=]` · `/portal/facturas/{numero}[/pdf]` | Facturas y PDF (SWR-27) |
| POST | `/portal/facturas/{numero}/pago-en-linea` | Pago con la pasarela; la factura pasa a PAGADA solo con confirmación (SWR-21) |
| GET | `/portal/pagos` · `/portal/pagos/{codigo}/comprobante[/pdf]` | Pagos y comprobantes |

Tarjetas de la pasarela simulada: `4111111111111111` aprobada · `4000000000000002` fondos insuficientes (402) ·
`4000000000000069` tarjeta vencida · `4000000000000119` sin respuesta (reintentos y 504).

### F-07 · Cobro persuasivo (`/cartera`)

| Método | Ruta | Uso |
|---|---|---|
| GET | `/cartera/morosos?municipioId=&minPeriodos=&soloCandidatos=` | Reporte: usuario, deuda, antigüedad y rango de mora (SWR-29) |
| GET | `/cartera/morosos/exportar?formato=pdf\|excel` | Exportación |
| GET | `/cartera/candidatos-suspension` | 2+ periodos consecutivos en mora sin acuerdo vigente (SWR-32) |
| GET · POST | `/cartera/acuerdos/simulacion` · `/cartera/acuerdos` | Simular y registrar acuerdos con cuotas y fechas automáticas (SWR-30) |
| GET | `/cartera/acuerdos[/{numero}]` | Consulta |
| POST | `/cartera/acuerdos/{numero}/cuotas/{n}/pago` | Pagar una cuota (en orden y por su valor) |
| GET | `/cartera/procesos-cobro` | Procesos de cobro coercitivo (suspendido durante el acuerdo, reactivado al incumplirse) |
| POST | `/cartera/monitoreo` | Ejecuta a demanda el monitoreo diario (SWR-31, SWR-32) |

### F-08 · Dashboard (`/dashboard`, roles Gerente y Directivo)

| Método | Ruta | Uso |
|---|---|---|
| GET | `/dashboard` | Indicadores de gestión comercial, facturación, recaudo y cartera, y PQR, calculados al momento con tendencia de 12 meses (SWR-34, SWR-35) |
| GET | `/dashboard/categoria?nombre=` | Una sola categoría |

---

## 4. Arquitectura

Arquitectura por capas organizada **por módulo funcional** (package-by-feature):

```
co.edu.uniquindio.epq
├── comun/        calendario laboral, territorio (municipios), secuencias, exportación PDF/Excel,
│                 notificaciones, excepciones, conversores JPA, plantilla de tareas
├── seguridad/    roles, funcionarios, JWT, verificación de credenciales y bloqueo, meta-anotaciones de acceso
├── auditoria/    log de auditoría transversal (RF-15) y su consulta
├── facturacion/  F-01 (+ estado de cobro de la factura, anulación, PDF, decorador de auditoría)
├── pqr/          F-02 (+ ubicación/municipio, evidencias, supervisión)
├── comercial/    F-03 y F-04: usuarios, predios, contratos, medidores, tarifas, adaptador RNEC
├── pagos/        F-05: pagos, comprobantes, pasarela (puerto + adaptadores)
├── portal/       F-06: consultas y pago en línea del ciudadano autenticado
├── cartera/      F-07: reporte de mora, acuerdos, procesos de cobro, monitoreo diario
└── dashboard/    F-08: proveedores de indicadores
```

Cada módulo mantiene las capas `dominio`, `repositorio`, `servicio` (interfaces + `impl/`), `web` (controladores y
DTO) y, cuando aplica, `evento` y `tarea`. Las consultas de lectura masiva (reporte de cartera, dashboard)
usan SQL con `NamedParameterJdbcTemplate` en lugar de cargar entidades (CQRS ligero).

### Patrones de diseño aplicados

| Patrón | Dónde | Para qué |
|---|---|---|
| **State** | `EstadoPqrTipo`, `EstadoContrato`, `EstadoCobroFactura` | Cada estado conoce sus transiciones válidas (DE-01, DE-02, DE-03); las entidades rechazan cambios ilegales con 409 |
| **Strategy** | `PoliticaPlazoRespuesta`, `EstrategiaAsignacionGestor` (`AsignacionPorMunicipio` → respaldo `AsignacionPorMenorCarga`), `EstrategiaCalculoFactura`, `PoliticaCuotas`, `ExportadorReporte`, `ProveedorIndicadores` | Reglas intercambiables: plazos, asignación, liquidación, cuotas, formatos de exportación, categorías del dashboard |
| **Factory** | `FabricaExportadores` (PDF/Excel), `Pqr.radicar`, `Factura.emitir`, `Contrato.registrar`, `AcuerdoPago.registrar`, `Cliente.registrar` | Elegir la implementación por formato; crear entidades siempre en un estado válido |
| **Decorator** | `GeneracionFacturacionAuditada` (`@Primary`) | Añade el log de auditoría de SWR-10 a la generación masiva sin modificar el servicio original |
| **Adapter / Puerto** | `ErpFinancieroGateway`, `ValidadorIdentidadRnec`, `PasarelaPagosGateway`, `CanalNotificacion`, `DirectorioFuncionarios`, `RepositorioCuentasCiudadano` | Aislar sistemas externos (simulado o REST por configuración) y desacoplar módulos |
| **Chain / Pipeline** | `PasoMonitoreoCartera` (`PasoVencerFacturas` → `PasoIncumplimientoAcuerdos` → `PasoProcesosCobro` → `PasoCandidatosSuspension`) | Monitoreo diario de cartera en pasos ordenados, cada uno en su transacción; agregar un paso no toca los demás |
| **Template Method** | `TareaProgramadaTemplate`, `DocumentoPdfEpq` | Esqueleto de tareas programadas; encabezado y pie comunes de todos los PDF (factura, comprobante, reportes) |
| **Observer** | Eventos de PQR y de pago + `@TransactionalEventListener(AFTER_COMMIT)`, `NotificacionDiferidaEvento` | Auditoría y notificaciones desacopladas; solo se notifica lo que realmente se confirmó |
| **Composite** | `DashboardServiceImpl` sobre la lista de `ProveedorIndicadores` | El dashboard es la composición de categorías independientes |
| **Specification** | `PqrSpecifications`, `FacturaSpecifications`, `ContratoSpecifications`, `PagoSpecifications`, `AuditoriaSpecifications` | Filtros combinables |
| **Facade** | `GeneracionFacturacionServiceImpl`, `RegistroPagoServiceImpl` | Un punto de entrada para procesos con varios colaboradores |
| **DTO / Mapper** | `web/dto`, `*Mapper` | La API no expone entidades JPA ni datos sensibles (contraseñas, tarjetas) |

### Principios SOLID

- **S — Responsabilidad única:** `VerificadorCredenciales` solo verifica contraseñas y bloqueo; `ProveedorTokenJwt`
  solo emite tokens; `AplicadorPago` solo aplica un pago confirmado a una factura; `CalculadoraMora` y
  `SelectorTarifa` son reglas puras; cada paso del monitoreo de cartera hace una sola cosa.
- **O — Abierto/cerrado:** un nuevo formato de exportación, indicador del dashboard, paso de monitoreo, política de
  cuotas, estrategia de asignación o adaptador externo se agrega como una implementación nueva que Spring inyecta;
  el código existente no cambia. La auditoría de la facturación se añadió con un decorador, sin editar el servicio
  de la entrega 1.
- **L — Sustitución de Liskov:** los adaptadores simulados y REST (ERP, RNEC, pasarela) cumplen el mismo contrato;
  `Funcionario` y `Cliente` son intercambiables como `CuentaConCredenciales` para el verificador.
- **I — Segregación de interfaces:** servicios separados por rol y por comando/consulta (`GestionUsuariosService` /
  `ConsultaUsuariosService`, `RegistroPagoService` / `ConsultaPagoService`, `AcuerdoPagoService` /
  `ReporteCarteraService` / `MonitoreoCarteraService`…). Los DTO de la entrega 1 conservan sus constructores
  anteriores para no romper a sus clientes.
- **D — Inversión de dependencias:** todo depende de abstracciones inyectadas por constructor (`ContextoSeguridad`,
  `ProveedorToken`, `ServicioAuditoria`, `GeneradorConsecutivos`, `CalendarioLaboral`, `Clock`, puertos externos), lo
  que permite probar con relojes fijos y dobles de prueba.

### Decisiones relevantes

- **Factura: dos estados independientes.** `estado` conserva la sincronización con el ERP de la entrega 1
  (GENERADA, SINCRONIZADA…) y el nuevo `estado_cobro` modela el ciclo de DE-01 (PENDIENTE, VENCIDA, PAGADA,
  ANULADA). Una factura incluida en un acuerdo vuelve a PENDIENTE protegida por el acuerdo y regresa a VENCIDA si
  el acuerdo se incumple (RN-07).
- **Tarifa (SWR-03, SWR-18, RN-08):** entre las tarifas vigentes del servicio se elige la más específica para el
  municipio y el estrato del predio (municipio pesa 2, estrato 1; a igual peso, la más reciente).
- **Pagos (RN-05):** la factura pasa a PAGADA solo con confirmación; la fila se bloquea (`PESSIMISTIC_WRITE`) y
  `pago.factura_id` es único, así que dos pagos simultáneos no pueden aplicarse. La llamada a la pasarela tiene
  tiempo máximo y reintentos, y cada intento queda registrado sin guardar el número de tarjeta.
- **Notificaciones y auditoría:** las notificaciones se emiten después del commit; los registros de auditoría de
  operaciones fallidas (p. ej. login errado) se guardan en una transacción independiente para que no se pierdan.
- **Evidencias de PQR:** se guardan en la base de datos (`BYTEA`) con validación de tipo y tamaño, para no depender
  de un almacenamiento de archivos en el prototipo.
- **Identificadores de negocio** (`USR-000001`, `CT-ACU-0001`, `PRD-000001`, `AP-2026-0001`, `PAG-…`) desde secuencias
  de PostgreSQL, seguras con varias instancias.

### Cambios que afectan al frontend

- Las rutas de back-office exigen `Authorization: Bearer <token>` (las del portal ciudadano de PQR siguen públicas).
- `EstadoContrato.INACTIVO` se reemplazó por **`CANCELADO`** y se agregó **`PENDIENTE`** (DE-03).
- Respuestas ampliadas (campos nuevos al final): `estadoCobro` en facturas; `municipio`, `numeroContrato` y
  `respondidaEnPlazo` en el detalle de PQR; `municipio` y `esJefe` en gestores.
- Responder una PQR requiere adjuntar antes al menos una evidencia (422 si no la tiene).
- CORS expone `Content-Disposition` para leer el nombre de los PDF y Excel descargados.

---

## 5. Pruebas

```bash
./mvnw clean test
```

Se conservan todas las pruebas de la entrega 1 (las de capa web usan `@AutoConfigureMockMvc(addFilters = false)`
porque prueban el contrato HTTP; la seguridad se prueba con la colección de Postman). Pruebas nuevas:

| Clase | Qué verifica |
|---|---|
| `CalculadoraPlazoRespuestaTest` (ampliada) | 15 días hábiles ≠ 15 calendario; radicación en sábado; Semana Santa; festivos de fin de año |
| `ContratoTest` | Máquina de estados del contrato y SWR-19 |
| `FacturaCobroTest` | Vencimiento, doble pago (SWR-23), anulación, acuerdo de pago y RN-07 |
| `SelectorTarifaTest` | Tarifa más específica por municipio y estrato |
| `CalculadoraMoraTest`, `AcuerdoPagoTest`, `PlanCuotasMensualesIgualesTest` | RN-03, RN-07, SWR-30 (redondeo y días hábiles) |
| `ControlAccesoTest` | Bloqueo tras 3 intentos y desbloqueo |
| `PasarelaPagosSimuladaAdapterTest` | Luhn, aprobada, rechazada, sin respuesta y datos de tarjeta ocultos |
| `CalculadoraDisponibilidadFacturaTest` | SWR-28 |
| `AsignacionPorMunicipioTest` | SyR-02b y el respaldo por menor carga |
| `GestionPqrEvidenciasTest` | RN-06: no se responde sin evidencia |
| `FormatoExportacionTest` | Formatos de exportación |

La colección `postman/SIGCA-EPQ.postman_collection.json` (134 peticiones) recorre todas las funcionalidades con los
tokens de cada rol; ver el [README principal](../README.md#63-pruebas-de-la-api-con-postman).

---

## 6. Limitaciones del prototipo

- **Integraciones simuladas:** RNEC, pasarela de pagos, ERP y correo tienen adaptadores simulados; los adaptadores
  REST siguen el contrato documentado en cada clase pero no se han probado contra servicios reales.
- **Datos de tarjeta:** viajan por la API hacia la pasarela (no se almacenan ni se escriben en logs), pero un
  sistema real debería tokenizarlos en el navegador con el SDK de la pasarela (PCI DSS) y usar HTTPS.
- **Sesión:** JWT sin lista de revocación; el cierre de sesión se audita y el frontend descarta el token, que
  expira a las 8 horas. No hay segundo factor de autenticación.
- **Pagos parciales:** no se admiten; un pago debe cubrir el total de la factura (las deudas se negocian con
  acuerdos de pago).
- **Acuerdos incumplidos:** las cuotas ya pagadas quedan registradas, pero no se abonan a las facturas que vuelven
  a mora; la conciliación queda para el ERP.
- **Tarifa por uso del predio** (residencial/comercial) no se modela; solo servicio, municipio y estrato.
- **Una sola instancia:** el radicado de PQR (heredado de la entrega 1) y los candados del lote de facturación y
  del monitoreo de cartera se sincronizan en memoria; con varios nodos haría falta un bloqueo distribuido.
