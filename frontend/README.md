# SIGCA-EPQ · Frontend

Aplicación web del prototipo **SIGCA-EPQ** (Empresas Públicas del Quindío), Ingeniería de Software 2.
Consume la API REST del backend (`../backend`) y cubre de punta a punta las ocho funcionalidades del Plan de
Requisitos, con inicio de sesión y pantallas según el rol:

| Funcionalidad | Qué se puede hacer desde la interfaz |
|---|---|
| **F-01** Generar facturas mensuales | Programación, generación del lote, historial de lotes, facturas, sincronización con el ERP, anulación y PDF |
| **F-02** Registrar y dar seguimiento a PQR | Radicar y consultar desde el portal; bandeja, trámite, evidencias, alertas, supervisión del equipo e indicador de cumplimiento |
| **F-03** Registrar usuario del servicio | Búsqueda, registro con predio y validación RNEC, perfil, edición y desactivación |
| **F-04** Gestionar contrato de servicio | Listado, registro con tarifa aplicable, medidor, cambios de estado e historial |
| **F-05** Registrar pago de factura | Pagos en caja y consignación, pagos en línea, comprobante electrónico y PDF |
| **F-06** Consultar factura e historial | Portal «Mi cuenta»: activación, servicios, factura actual, historial de 24 meses, pago en línea y datos de contacto |
| **F-07** Gestionar cobro persuasivo | Cartera morosa con exportación, acuerdos de pago, pago de cuotas, cobro coercitivo y monitoreo |
| **F-08** Consultar dashboard gerencial | Indicadores de las cuatro áreas con tendencia de 12 meses |
| Transversal | Login de funcionarios y de ciudadanos (JWT), menú por rol, funcionarios, roles y auditoría |

**Stack:** Vite 8 · React 19 (JavaScript) · React Router 7 · TanStack Query 5 · CSS propio con variables · ESLint.
No usa librerías de componentes ni de gráficas: las gráficas son SVG y CSS propios.

---

## Contenido

1. [Puesta en marcha](#1-puesta-en-marcha)
2. [Usuarios de prueba y acceso](#2-usuarios-de-prueba-y-acceso)
3. [Pantallas y trazabilidad](#3-pantallas-y-trazabilidad)
4. [Estructura del proyecto](#4-estructura-del-proyecto)
5. [Arquitectura y decisiones](#5-arquitectura-y-decisiones)
6. [Sesión y permisos](#6-sesión-y-permisos)
7. [Sistema visual](#7-sistema-visual)
8. [Accesibilidad y responsive](#8-accesibilidad-y-responsive)
9. [Convenciones](#9-convenciones)
10. [Verificación](#10-verificación)
11. [Limitaciones del prototipo](#11-limitaciones-del-prototipo)
12. [Solución de problemas](#12-solución-de-problemas)

---

## 1. Puesta en marcha

### Requisitos

- **Node.js** 20.19 o superior (requisito de Vite) y **npm**.
- La **base de datos** y el **backend** en ejecución (ver el README principal del repositorio).

### Instalación

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

La aplicación queda en `http://localhost:5173`.

### Variables de entorno

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `VITE_API_URL` | `http://localhost:8080/api` | URL base de la API del backend |

El archivo `.env` no se versiona; `.env.example` es la plantilla. Después de cambiarlo hay que reiniciar
`npm run dev`. El backend ya permite peticiones desde `http://localhost:5173` (CORS en `application.yml`).

### Scripts

| Comando | Uso |
|---|---|
| `npm run dev` | Servidor de desarrollo con recarga automática |
| `npm run build` | Compilación de producción en `dist/` (cada pantalla queda en su propio archivo) |
| `npm run preview` | Sirve localmente la compilación de `dist/` |
| `npm run lint` | Revisión con ESLint; el proyecto se mantiene con `npx eslint src --max-warnings 0` |

---

## 2. Usuarios de prueba y acceso

Los crea el script `database/init/04_seed_entrega2.sql`. El detalle de qué puede hacer cada rol está en
[`backend/README.md` → Control de acceso](../backend/README.md#2-control-de-acceso-rf-14-rnf-05).

| Entrada | Usuario | Contraseña | Pantalla inicial |
|---|---|---|---|
| **Acceso funcionarios** (`/acceso`) | `admin` | `Epq2026*` | Gestión de PQR (ve todo el back-office y la administración) |
| | `asesor` | `Epq2026*` | Usuarios |
| | `lgiraldo` · `aocampo` · `mcastano` (gestores de PQR) | `Epq2026*` | Gestión de PQR |
| | `phenao` (Jefe de PQR) | `Epq2026*` | Gestión de PQR (más Supervisión y Cumplimiento) |
| | `cartera` | `Epq2026*` | Usuarios (más Cartera, Acuerdos y Cobro) |
| | `gerente` · `directivo` | `Epq2026*` | Dashboard |
| **Ingresar a Mi cuenta** (`/ingresar`) | Cédula `1094950001` (y los demás usuarios del seed) | `Ciudadano2026*` | Mi cuenta |

- El portal público (radicar y consultar PQR) no pide sesión.
- Al entrar a `/gestion` sin sesión se va al login y, después de ingresar, se vuelve a la página pedida.
- **Activar mi cuenta** (`/activar-cuenta`): un usuario del servicio que EPQ registró y que aún no tiene
  contraseña del portal la crea con su cédula y el correo registrado. En el seed sirve `1098765432` con
  `smorales@correo.com`.
- Tres contraseñas erradas seguidas bloquean la cuenta 15 minutos. El administrador la desbloquea desde el
  detalle del funcionario con **Desbloquear** (restablece la contraseña).

### Datos útiles del seed para probar

| Caso | Dato |
|---|---|
| Contrato candidato a suspensión, sin acuerdo | `CT-ACU-0004` (Hostal Cafetero, NIT 900123456) |
| Acuerdo de pago vigente | El de `CT-ACU-0002` (CC 41912345) |
| Acuerdo que se incumple al ejecutar el monitoreo | El de `CT-GAS-0001` (primera cuota vencida) |
| Ciudadano con facturas por pagar | CC `7548123` o NIT `900123456` |
| Tarjetas de la pasarela simulada | `4111111111111111` aprobada · `4000000000000002` fondos insuficientes · `4000000000000069` vencida · `4000000000000119` sin respuesta |

---

## 3. Pantallas y trazabilidad

Cada ruta indica, como comentario en `src/rutas.jsx`, los requisitos del student book (RF, CU, RN, DE) y del
Plan de Requisitos (SWR) que respalda. La columna **Roles** es la capacidad que exige la ruta
(`src/sesion/capacidades.js`); el backend vuelve a verificar el permiso en cada petición.

### Portal ciudadano (público)

| Pantalla | Ruta | Requisitos |
|---|---|---|
| Inicio | `/` | — |
| Radicar PQR | `/radicar` | RF-08, CU-06, SWR-05, SWR-06 |
| Constancia de radicación | `/radicar/constancia` | RF-08, SWR-05, SWR-06, RN-04 |
| Consulta pública del estado | `/consultar` | RF-10, CU-07, SWR-08 |
| Acceso funcionarios | `/acceso` | RF-14, RNF-05 |
| Ingresar a Mi cuenta | `/ingresar` | RF-07, CU-05 |
| Activar mi cuenta | `/activar-cuenta` | CU-01 |

### Portal ciudadano · Mi cuenta (rol Ciudadano, F-06 y F-05)

| Pantalla | Ruta | Requisitos |
|---|---|---|
| Resumen: saldo y servicios | `/mi-cuenta` | RF-07, CU-05 |
| Servicio: factura actual e historial de 24 meses | `/mi-cuenta/contratos/:numero` | RF-07, CU-05, SWR-25, SWR-26, SWR-28 |
| Facturas | `/mi-cuenta/facturas` | RF-07, SWR-25, SWR-27 |
| Detalle de factura y PDF | `/mi-cuenta/facturas/:numero` | RF-07, SWR-25, SWR-27 |
| Pago en línea | `/mi-cuenta/facturas/:numero/pagar` | RF-06, CU-04, RN-05, SWR-21, SWR-22, SWR-23 |
| Pagos realizados | `/mi-cuenta/pagos` | RF-06, SWR-22, SWR-24 |
| Comprobante | `/mi-cuenta/pagos/:codigo` | SWR-22 |
| Mis datos de contacto | `/mi-cuenta/datos` | RF-07 |

### Back-office

| Área | Pantalla | Ruta | Roles | Requisitos |
|---|---|---|---|---|
| F-08 | Dashboard gerencial | `/gestion/dashboard` | Gerente, Directivo | RF-13, CU-09, SWR-33, SWR-34, SWR-35 |
| F-02 | Bandeja de PQR | `/gestion/pqr` | Admin, Gestor y Jefe de PQR | RF-09, CU-06 |
| | Detalle: tomar, reasignar, evidencias, responder y cerrar | `/gestion/pqr/:radicado` | Admin, Gestor y Jefe de PQR | RF-09, CU-06, DE-02, RN-06, SWR-09 |
| | Alertas y monitoreo de plazos | `/gestion/alertas` | Admin, Gestor y Jefe de PQR | RF-09, SWR-07, RN-04 |
| | Supervisión del equipo | `/gestion/supervision` | Admin, Jefe de PQR | StR-02c, SWR-07, SWR-11, RN-04 |
| | Cumplimiento de plazos (SSPD) | `/gestion/cumplimiento` | Admin, Jefe de PQR, Gerente, Directivo | SyR-02f, RN-04 |
| F-03 | Usuarios del servicio | `/gestion/usuarios` | Todos los funcionarios | RF-03, SWR-16 |
| | Registrar usuario | `/gestion/usuarios/nuevo` | Admin, Asesor | RF-01, CU-01, SWR-12, SWR-13, SWR-14 |
| | Perfil del usuario | `/gestion/usuarios/:codigo` | Todos los funcionarios | RF-02, RF-03, SWR-14, SWR-15 |
| F-04 | Contratos | `/gestion/contratos` | Todos los funcionarios | RF-04, CU-02, SWR-20 |
| | Registrar contrato | `/gestion/contratos/nuevo` | Admin, Asesor | RF-04, CU-02, RN-01, RN-08, SWR-17, SWR-18, SWR-19 |
| | Detalle del contrato | `/gestion/contratos/:numero` | Todos los funcionarios | RF-04, CU-02, SWR-18, SWR-19, SWR-20 |
| F-01 | Programación y generación del lote | `/gestion/facturacion` | Admin, Asesor, Cartera, Gerente, Directivo | RF-05, CU-03, SWR-01, SWR-02 |
| | Historial de lotes | `/gestion/facturacion/lotes` | Igual que la anterior | RF-05, CU-03, SWR-02, SWR-04 |
| | Detalle del lote | `/gestion/facturacion/lotes/:periodo` | Igual que la anterior | RF-05, CU-03, SWR-02, SWR-04 |
| | Facturas | `/gestion/facturacion/facturas` | Igual que la anterior | RF-05, SWR-03, SWR-04 |
| | Detalle de factura, anulación y PDF | `/gestion/facturacion/facturas/:numero` | Igual que la anterior | RF-05, SWR-03, SWR-04, SWR-27 |
| F-05 | Pagos | `/gestion/pagos` | Admin, Asesor, Cartera, Gerente, Directivo | RF-06, CU-04, SWR-24 |
| | Registrar pago en caja o consignación | `/gestion/pagos/nuevo` | Admin, Asesor | RF-06, CU-04, RN-05, SWR-22, SWR-23, SWR-24 |
| | Pago de cuota de acuerdo | `/gestion/pagos/cuota` | Admin, Asesor, Cartera | RF-12, CU-08 |
| | Comprobante | `/gestion/pagos/:codigo` | Admin, Asesor, Cartera, Gerente, Directivo | RF-06, SWR-22 |
| F-07 | Cartera morosa y exportación | `/gestion/cartera` | Cartera, Gerente | RF-11, CU-08, SWR-29, SWR-32, RN-03 |
| | Acuerdos de pago | `/gestion/cartera/acuerdos` | Admin, Cartera | RF-12, CU-08, RN-07 |
| | Nuevo acuerdo con simulación del plan | `/gestion/cartera/acuerdos/nuevo` | Admin, Cartera | RF-12, CU-08, SWR-30, RN-07 |
| | Detalle del acuerdo y pago de cuotas | `/gestion/cartera/acuerdos/:numero` | Admin, Cartera | RF-12, CU-08, RN-07 |
| | Cobro coercitivo y monitoreo | `/gestion/cartera/cobro` | Admin, Cartera | RN-07, SWR-31, SWR-32 |
| Administración | Funcionarios | `/gestion/administracion/funcionarios` | Admin | RF-14, CU-10, RNF-05 |
| | Nuevo funcionario | `/gestion/administracion/funcionarios/nuevo` | Admin | RF-14, CU-10 |
| | Detalle: datos, rol, estado, contraseña y cambios | `/gestion/administracion/funcionarios/:id` | Admin | RF-14, RF-15, CU-10 |
| | Roles y permisos | `/gestion/administracion/roles` | Admin | RF-14, RNF-05 |
| | Auditoría | `/gestion/administracion/auditoria` | Admin | RF-15, CU-11 |

**Dos restricciones del backend que se notan en la interfaz:** el reporte de cartera morosa es solo para el
Gestor de Cartera y el Gerente (el administrador gestiona acuerdos pero no ve el reporte), y el asesor puede
pagar cuotas pero no consultar acuerdos, por eso su pantalla de pago de cuota pide el número del acuerdo y de
la cuota.

---

## 4. Estructura del proyecto

```
frontend/
├── index.html                 Documento base: idioma, fuente Public Sans y punto de entrada
├── public/favicon.svg
└── src/
    ├── main.jsx               Arranque: QueryClient, sesión y enrutador
    ├── rutas.jsx              Mapa de pantallas con sus requisitos y carga diferida
    ├── api/                   Comunicación HTTP: una función por endpoint
    │   ├── cliente.js         Cliente único: token JWT, errores RFC 7807 → ErrorApi, descargas
    │   ├── autenticacion.js   Login de funcionarios y ciudadanos, activación y cierre de sesión
    │   ├── pqr.js · facturacion.js · usuarios.js · contratos.js · territorio.js
    │   ├── pagos.js · portal.js · cartera.js · dashboard.js · administracion.js
    ├── hooks/                 Consultas y mutaciones de TanStack Query, una por módulo
    │   ├── usePqr.js · useFacturacion.js · useUsuarios.js · useContratos.js · useTerritorio.js
    │   ├── usePagos.js · usePortal.js · useCartera.js · useDashboard.js · useAdministracion.js
    │   ├── useFiltrosUrl.js   Filtros y paginación guardados en la URL
    │   └── useTituloPagina.js Título de la pestaña por pantalla
    ├── presentacion/          Funciones puras de presentación (sin React)
    │   ├── fechas.js · texto.js · paginacion.js · archivos.js · datosPersonales.js
    │   ├── estadosPqr.js · notificaciones.js · supervisionPqr.js
    │   ├── facturacion.js · usuarios.js · contratos.js · pagos.js · portal.js
    │   └── cartera.js · dashboard.js · administracion.js
    ├── sesion/                Sesión JWT y permisos
    │   ├── almacenSesion.js   Sesión en localStorage, vencimiento y aviso de 401
    │   ├── SesionProvider.jsx Contexto: usuario, iniciar y cerrar sesión, puede(capacidad)
    │   ├── capacidades.js     Qué roles tienen cada capacidad (menú, rutas y botones)
    │   └── actuacionPqr.js    En nombre de qué gestor actúa el usuario sobre una PQR
    ├── componentes/           Estructura y componentes reutilizables
    │   ├── ui/                Boton, Campo, Dialogo, Etiqueta, EnlaceVolver, Icono, Logo
    │   ├── PortalLayout.jsx   Encabezado y pie del portal ciudadano
    │   ├── BackofficeLayout.jsx  Menú lateral por rol y barra del back-office
    │   ├── menuGestion.js     Opciones del menú con la capacidad que exige cada una
    │   ├── RutaProtegida.jsx · RutaCiudadano.jsx   Guardas de sesión y de rol
    │   ├── CargaPagina.jsx    Aviso mientras se descarga una pantalla
    │   └── ...                Encabezados, formularios por secciones, paginación, errores, recibo de pago
    ├── estilos/
    │   ├── tokens.css         Tokens de diseño: color, tipografía, espaciado y forma
    │   └── base.css           Reinicio de estilos y utilidades globales
    └── paginas/
        ├── acceso/            Login de funcionarios
        ├── ciudadano/         Inicio, radicación, constancia y consulta pública
        │   └── cuenta/        «Mi cuenta»: ingreso, activación, servicios, facturas, pagos y datos
        └── gestion/
            ├── dashboard/     F-08
            ├── pqr/ · alertas/ · supervision/ · cumplimiento/      F-02
            ├── usuarios/      F-03 (perfil/ con sus diálogos)
            ├── contratos/     F-04 (detalle/)
            ├── facturacion/   F-01 (factura/)
            ├── pagos/         F-05
            ├── cartera/       F-07 (acuerdos/)
            └── administracion/  Funcionarios, roles y auditoría
```

Cada pantalla vive en su carpeta junto con sus componentes y su hoja de estilos.

---

## 5. Arquitectura y decisiones

### Capas

| Capa | Responsabilidad | Regla |
|---|---|---|
| `api/` | Hablar HTTP con el backend | No conoce React |
| `hooks/` | Obtener y modificar datos del servidor | Las páginas nunca llaman a `fetch` directamente |
| `presentacion/` | Transformar datos para mostrarlos | Funciones puras, fáciles de probar |
| `sesion/` | Sesión, token y permisos | Un solo lugar decide qué puede hacer cada rol |
| `componentes/` | Piezas visuales reutilizables | No conocen la API |
| `paginas/` | Componer una pantalla | Orquestan hooks y componentes |

### Decisiones principales

- **Estado del servidor con TanStack Query.** Cada módulo tiene su fábrica de claves (`clavesPqr`,
  `clavesFacturacion`, `clavesCartera`, `clavesAdministracion`…). Después de cada acción se invalidan las
  consultas afectadas, incluidas las de otros módulos cuando corresponde (un pago refresca facturas y
  contratos; un acuerdo refresca cartera, facturas, contratos y pagos). Los errores 4xx no se reintentan.
- **El refresco después de guardar no se espera** cuando la pantalla navega al resultado (registrar un pago,
  un acuerdo o pagar en línea). Si se esperara, el formulario se desmontaría antes de navegar, porque la factura
  ya estaría pagada o el contrato ya tendría acuerdo.
- **Errores estándar.** `api/cliente.js` convierte las respuestas RFC 7807 del backend en `ErrorApi`
  (`estado`, `titulo`, `detalle`, `errores` por campo). Los formularios muestran cada error del 400 debajo de su
  campo y los 409/422 en el pie o en el campo al que se refieren.
- **Sin reglas de negocio duplicadas.** Los botones del detalle de PQR salen de `accionesDisponibles` (DE-02);
  el plan de cuotas lo simula el backend (SWR-30); los indicadores del dashboard llegan calculados (SWR-34).
  La interfaz solo replica validaciones de formato para avisar antes de enviar.
- **La URL como estado.** Filtros, búsqueda, página y el indicador del dashboard viven en la URL: se conservan
  al volver desde un detalle, al recargar y al compartir el enlace.
- **Fechas sin desfase.** Las fechas del backend llegan sin zona horaria; `presentacion/fechas.js` las arma en
  hora local para que `2026-09-22` no se muestre como el 21 en Colombia (UTC-5).
- **Carga diferida de pantallas.** `rutas.jsx` carga cada pantalla con `React.lazy` y los layouts muestran
  `CargaPagina` mientras llega. El paquete principal pasó de más de 500 kB a unos 270 kB, y un ciudadano no
  descarga el back-office.
- **Gráficas propias.** La tendencia del dashboard y el historial de consumos son SVG y CSS con una sola serie
  y un solo color, ayuda emergente con puntero y teclado, y vista en tabla. Evita agregar dependencias.

### Patrones utilizados

| Patrón | Dónde |
|---|---|
| Adapter | `api/cliente.js` traduce las respuestas del backend a `ErrorApi` |
| Custom hooks | `hooks/`: datos del servidor por módulo, filtros en la URL, título de página |
| Provider / Context | `sesion/SesionProvider.jsx`: sesión y permisos en toda la aplicación |
| Observer | `almacenSesion.js` avisa a los suscriptores cuando el token vence o el backend responde 401 |
| Guard | `RutaProtegida` y `RutaCiudadano` protegen rutas por sesión y capacidad |
| Composición de componentes | Las páginas componen componentes de `componentes/` |
| Query key factory | `clavesPqr`, `clavesCartera`, `clavesAdministracion`… |
| Interfaz dirigida por el servidor | Acciones del detalle de PQR a partir de `accionesDisponibles` |
| Lazy loading | `rutas.jsx` con `React.lazy` y `Suspense` |

---

## 6. Sesión y permisos

- **Login:** `POST /auth/login` (funcionarios) y `POST /auth/ciudadano/login` (ciudadanos) devuelven un JWT con
  vencimiento de 8 horas. `almacenSesion.js` lo guarda en `localStorage` (clave `sigca-epq:sesion`) y el cliente
  HTTP lo envía en `Authorization: Bearer`. Nunca se guarda la contraseña.
- **Cierre de sesión:** manual (avisa al backend para la auditoría), automático al vencer el token o al recibir
  un 401. En los dos últimos casos el login muestra el motivo. Al cambiar de sesión se vacía la caché, para que
  nadie vea datos cargados por el usuario anterior.
- **Capacidades:** `sesion/capacidades.js` traduce los roles del backend a capacidades de la interfaz
  (`PQR_ATENDER`, `CARTERA_GESTIONAR`, `DASHBOARD`, `ADMINISTRACION`…). Con ellas se arma el menú, se protegen
  las rutas y se muestran los botones. Es solo presentación: la autorización real la hace el backend, que
  responde 403 si algo no está permitido.
- **Inicio por rol:** `/gestion` lleva a la primera opción del menú que permite el rol (el gerente cae en el
  dashboard, el asesor en Usuarios).
- **Cambios de rol:** el rol viaja dentro del token, así que un cambio de rol aplica cuando el funcionario
  vuelve a iniciar sesión.

---

## 7. Sistema visual

El diseño se construyó en React y se implementó con CSS propio, sin librerías de componentes. Todas las
pantallas siguen la misma línea visual.

- **Tokens** en `src/estilos/tokens.css`: paleta azul petróleo del servicio de agua, colores semánticos
  para estados (ámbar para plazos, rojo para vencidos o errores, verde para resueltos, pagados o sincronizados),
  escala tipográfica, espaciado en múltiplos de 4 px y radios.
- **Tipografía:** Public Sans, con dígitos de ancho fijo (clase `.cifra`) para radicados, números de factura,
  contratos y montos.
- **Piezas comunes:** encabezado de pantalla con acción, franja de métricas, panel de filtros con tabla que
  pasa a tarjetas, detalle con columna lateral, formularios por secciones numeradas y bloque oscuro para el
  dato principal (saldo pendiente, total a pagar).
- **Puntos de corte:** `37.5rem` (600 px, tableta), `56.25rem` (900 px, escritorio) y `75rem` (1200 px) para
  las filas de filtros más anchas.
- **Gráficas:** una sola serie por gráfica en el color agua; el verde y el rojo se reservan para la variación,
  que también se escribe con palabras («Mejora», «Empeora»).

---

## 8. Accesibilidad y responsive

Orientado a WCAG 2.1 AA (RNF-03) y a navegadores de escritorio y móviles (RNF-08):

- Estilos **mobile-first**: las tablas se convierten en tarjetas en pantallas pequeñas y el menú del
  back-office pasa a un panel lateral que se abre con un botón y se cierra con `Esc`, al navegar o tocando fuera.
- Todos los campos tienen etiqueta; los errores se asocian a su campo con `aria-invalid` y
  `aria-describedby`, y al enviar un formulario con errores el foco va al primer campo inválido.
- Ventanas modales con el elemento nativo `<dialog>`: el foco queda dentro, se cierran con `Esc` y el foco
  vuelve al botón que las abrió.
- Las gráficas se recorren con el teclado y tienen su vista en tabla; ningún dato depende solo del color.
- Enlace «Saltar al contenido principal», foco visible en todos los controles y título de pestaña por pantalla.
- Textos en español con terminología para usuarios no técnicos (RNF-11).

---

## 9. Convenciones

- **Nombres en español**, alineados con el dominio y con el backend (`radicado`, `lote`, `acuerdo`, `cuota`...).
- **Archivos:** componentes en `PascalCase.jsx` (`BandejaPqr.jsx`); utilidades, hooks y estilos en
  `camelCase` o `kebab-case` (`useCartera.js`, `detalle-lote.css`). Windows no distingue mayúsculas en los
  nombres de archivo, pero Linux y el despliegue sí, así que hay que respetar el nombre exacto.
- **Estilos:** cada pantalla importa su propia hoja; lo compartido está en `base.css`, `componentes/ui/ui.css`,
  `componentes/backoffice.css` y en las hojas de la bandeja (`pqr/bandeja.css`) y del detalle
  (`pqr/detalle/detalle.css`). Cuando una hoja ajusta la tabla o los filtros de la bandeja, usa las dos clases
  juntas (`.bandeja-tabla.cartera-tabla`) para no depender del orden en que se cargan los archivos.
- **Componentes y hooks:** un archivo de componentes solo exporta componentes (regla `react-refresh` de
  ESLint); las funciones auxiliares van en `presentacion/`.
- **Commits:** Conventional Commits con alcance, por ejemplo `feat(frontend): ...` o `fix(frontend): ...`.

---

## 10. Verificación

Antes de cada commit:

```bash
npm run build
npx eslint src --max-warnings 0
```

Cada pantalla se revisó en escritorio (1366 px) y en celular (390 px) contra el backend real con los datos del
seed y contra respuestas simuladas para los casos difíciles de reproducir (pasarela sin respuesta, cuota fuera
de orden, cuenta bloqueada, único administrador). El recorrido de extremo a extremo está en la sección 6.4 del
README principal.

---

## 11. Limitaciones del prototipo

- **Sin pruebas automatizadas del frontend:** están planeadas con Vitest y Testing Library. La API se valida con
  las pruebas del backend y la colección de Postman.
- **Datos de tarjeta:** el pago en línea envía los datos de la tarjeta a la API (que no los guarda). Un sistema
  real los tokenizaría en el navegador con el script de la pasarela (PCI DSS).
- **Sesión en `localStorage`:** sencillo para el prototipo; en producción se preferiría una cookie `HttpOnly`.
- **Sin modo oscuro ni internacionalización:** la interfaz está solo en español de Colombia.

---

## 12. Solución de problemas

| Síntoma | Causa | Solución |
|---|---|---|
| Aviso «Sin conexión» en todas las pantallas | El backend no está en ejecución o `VITE_API_URL` no apunta a él | Levantar el backend y revisar `.env`; después de cambiar `.env`, reiniciar `npm run dev` |
| Vuelve al login con «Su sesión venció» | El token cumplió 8 horas o el backend lo rechazó (por ejemplo, se reinició con otro `JWT_SECRET`) | Iniciar sesión de nuevo |
| El login dice que la cuenta está bloqueada | Tres contraseñas erradas seguidas | Esperar 15 minutos o desbloquearla como `admin` en Funcionarios |
| «No tiene acceso a esta sección» | La capacidad de la ruta no incluye el rol | Entrar con un rol que la tenga (sección 3) |
| `Failed to fetch dynamically imported module` al abrir una pantalla | Se publicó una versión nueva y la pestaña abierta pide un archivo de la anterior | Recargar la página |
| Página en blanco sin aviso de Vite | Error de JavaScript al ejecutar un componente | Abrir la consola del navegador (`F12` → Console) y revisar el primer error en rojo |
| `does not provide an export named ...` | Un archivo quedó con otra versión o con otro nombre | Verificar el nombre exacto (mayúsculas incluidas) y que el archivo esté actualizado |
| `Failed to resolve import` | El archivo no está en la ruta que indica el `import` | Revisar la carpeta con `ls` |