# SIGCA-EPQ · Frontend

Aplicación web del prototipo **SIGCA-EPQ** (Empresas Públicas del Quindío), Ingeniería de Software 2.
Consume la API REST del backend (`../backend`) y cubre las dos funcionalidades del prototipo de punta a punta:

- **F-01 Generar facturas mensuales:** programación, generación del lote, historial de lotes, facturas y sincronización con el ERP.
- **F-02 Registrar y dar seguimiento a PQR:** portal ciudadano (radicar y consultar) y back-office del gestor (bandeja, trámite y alertas).

**Stack:** Vite 8 · React 19 (JavaScript) · React Router · TanStack Query · CSS propio con variables · ESLint.

---

## Contenido

1. [Puesta en marcha](#1-puesta-en-marcha)
2. [Pantallas y trazabilidad](#2-pantallas-y-trazabilidad)
3. [Estructura del proyecto](#3-estructura-del-proyecto)
4. [Arquitectura y decisiones](#4-arquitectura-y-decisiones)
5. [Sistema visual](#5-sistema-visual)
6. [Accesibilidad y responsive](#6-accesibilidad-y-responsive)
7. [Convenciones](#7-convenciones)
8. [Limitaciones del prototipo](#8-limitaciones-del-prototipo)
9. [Solución de problemas](#9-solución-de-problemas)

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

El archivo `.env` no se versiona; `.env.example` es la plantilla. El backend ya permite peticiones desde
`http://localhost:5173` (configuración CORS en `application.yml`).

### Scripts

| Comando | Uso |
|---|---|
| `npm run dev` | Servidor de desarrollo con recarga automática |
| `npm run build` | Compilación de producción en `dist/` |
| `npm run preview` | Sirve localmente la compilación de `dist/` |
| `npm run lint` | Revisión con ESLint (el proyecto se mantiene con 0 advertencias) |

---

## 2. Pantallas y trazabilidad

Cada ruta respalda requisitos del student book (RF, CU, RN, DE) y del Plan de Requisitos (SWR).
La misma referencia está como comentario en `src/rutas.jsx`.

| Área | Pantalla | Ruta | Requisitos |
|---|---|---|---|
| Portal ciudadano | Inicio | `/` | — |
| | Radicar PQR | `/radicar` | RF-08, CU-06, SWR-05, SWR-06 |
| | Constancia de radicación | `/radicar/constancia` | RF-08, SWR-05, SWR-06, RN-04 |
| | Consulta pública del estado | `/consultar` | RF-10, CU-07, SWR-08 |
| Back-office PQR | Bandeja con métricas y filtros | `/gestion/pqr` | RF-09, CU-06 |
| | Detalle: tomar, reasignar, responder y cerrar | `/gestion/pqr/:radicado` | RF-09, CU-06, DE-02, RN-06, SWR-09 |
| | Alertas del gestor y monitoreo de plazos | `/gestion/alertas` | RF-09, SWR-07, RN-04 |
| Back-office facturación | Programación y generación del lote | `/gestion/facturacion` | RF-05, CU-03, SWR-01, SWR-02 |
| | Historial de lotes | `/gestion/facturacion/lotes` | RF-05, CU-03, SWR-02, SWR-04 |
| | Detalle del lote | `/gestion/facturacion/lotes/:periodo` | RF-05, CU-03, SWR-02, SWR-04 |
| | Facturas | `/gestion/facturacion/facturas` | RF-05, SWR-03, SWR-04 |
| | Detalle de factura | `/gestion/facturacion/facturas/:numero` | RF-05, SWR-03, SWR-04 |

**Acceso al back-office:** el prototipo no tiene inicio de sesión. Se entra por **Acceso funcionarios** y se
elige el gestor en **Gestor actual** (barra superior). Ese gestor es el que ejecuta las acciones sobre las PQR
y el que queda registrado en el historial (SWR-09).

---

## 3. Estructura del proyecto

```
frontend/
├── index.html                 Documento base: idioma, fuente Public Sans y punto de entrada
├── public/favicon.svg
└── src/
    ├── main.jsx               Arranque: QueryClient, sesión y enrutador
    ├── rutas.jsx              Mapa de pantallas con sus requisitos
    ├── api/                   Comunicación HTTP
    │   ├── cliente.js         Cliente único; convierte los errores RFC 7807 en ErrorApi
    │   ├── pqr.js             Una función por endpoint de F-02
    │   └── facturacion.js     Una función por endpoint de F-01
    ├── hooks/                 Consultas y mutaciones (TanStack Query) y hooks reutilizables
    │   ├── usePqr.js          Claves de caché y hooks del módulo PQR
    │   ├── useFacturacion.js  Claves de caché y hooks del módulo de facturación
    │   ├── useFiltrosUrl.js   Filtros y paginación guardados en la URL
    │   └── useTituloPagina.js Título de la pestaña por pantalla
    ├── presentacion/          Funciones puras de presentación (sin React)
    │   ├── fechas.js          Fechas del backend en español de Colombia
    │   ├── estadosPqr.js      Colores y línea de tiempo de los estados de PQR
    │   ├── notificaciones.js  Presentación de cada tipo de notificación
    │   ├── facturacion.js     Moneda, periodos, duraciones y estados de lotes y facturas
    │   ├── texto.js           Búsqueda sin tildes y enmascarado de documentos
    │   └── paginacion.js      Recorte de listas por página
    ├── sesion/                Gestor actual (contexto de React, guardado en localStorage)
    ├── componentes/           Estructura y componentes reutilizables
    │   ├── ui/                Boton, Campo, Dialogo, Etiqueta, EnlaceVolver, Icono, Logo
    │   ├── PortalLayout.jsx   Encabezado y pie del portal ciudadano
    │   ├── BackofficeLayout.jsx  Menú lateral y barra del back-office
    │   └── ...                Paginación, mensajes de error, carga, 404
    ├── estilos/
    │   ├── tokens.css         Tokens de diseño: color, tipografía, espaciado y forma
    │   └── base.css           Reinicio de estilos y utilidades globales
    └── paginas/
        ├── ciudadano/         Inicio, radicación, constancia y consulta pública
        └── gestion/
            ├── pqr/           Bandeja y detalle de PQR (detalle/)
            ├── alertas/       Alertas del gestor y monitoreo
            └── facturacion/   Programación, lotes, facturas y detalle de factura (factura/)
```

Cada pantalla vive en su carpeta junto con sus componentes y su hoja de estilos.

---

## 4. Arquitectura y decisiones

### Capas

| Capa | Responsabilidad | Regla |
|---|---|---|
| `api/` | Hablar HTTP con el backend | No conoce React |
| `hooks/` | Obtener y modificar datos del servidor | Las páginas nunca llaman a `fetch` directamente |
| `presentacion/` | Transformar datos para mostrarlos | Funciones puras, fáciles de probar |
| `componentes/` | Piezas visuales reutilizables | No conocen la API |
| `paginas/` | Componer una pantalla | Orquestan hooks y componentes |

### Decisiones principales

- **Estado del servidor con TanStack Query.** Las claves de caché están centralizadas (`clavesPqr`,
  `clavesFacturacion`). Después de cada acción se invalidan las consultas afectadas, así la bandeja, las
  alertas, los lotes y los detalles se actualizan solos. Los errores 4xx no se reintentan; los de servidor
  o de conexión sí.
- **Errores estándar.** `api/cliente.js` convierte las respuestas RFC 7807 del backend en `ErrorApi`
  (`estado`, `titulo`, `detalle`, `errores` por campo). Los formularios muestran cada error del 400 debajo de
  su campo, y la falta de conexión se muestra como un aviso en lugar de una página en blanco.
- **Sin reglas de negocio duplicadas.** Los botones del detalle de PQR salen de `accionesDisponibles`, que
  calcula el backend según el estado (DE-02). La métrica "próximas a vencer" usa `alertaVencimientoEnviada`
  del backend (SWR-07), no un cálculo propio de 48 horas.
- **Validación en dos capas.** El formulario de radicación replica las reglas de `RegistrarPqrRequest` para
  avisar antes de enviar (`validacionPqr.js`); el backend sigue siendo la validación definitiva.
- **La URL como estado.** Filtros, búsqueda y página de los listados viven en la URL: se conservan al volver
  desde un detalle, al recargar y al compartir el enlace. El radicado de la consulta pública también.
- **Fechas sin desfase.** Las fechas del backend llegan sin zona horaria. `presentacion/fechas.js` construye
  las fechas en hora local para que una fecha como `2026-09-22` no se muestre como el 21 en Colombia (UTC-5).
- **Preparado para el login.** La sesión está aislada en `sesion/` y el cliente HTTP es el único punto donde
  se agregaría el token, así que incorporar autenticación no obliga a reestructurar las pantallas.

### Patrones utilizados

| Patrón | Dónde |
|---|---|
| Adapter | `api/cliente.js` traduce las respuestas del backend a `ErrorApi` |
| Custom hooks | `hooks/`: datos del servidor, filtros en la URL, título de página |
| Provider / Context | `sesion/`: gestor actual disponible en toda la aplicación |
| Composición de componentes | Las páginas componen componentes de `componentes/` |
| Query key factory | `clavesPqr`, `clavesFacturacion` |
| Interfaz dirigida por el servidor | Acciones del detalle de PQR a partir de `accionesDisponibles` |

---

## 5. Sistema visual

El diseño se construyó en React y se implementó con CSS propio, sin librerías de componentes.

- **Tokens** en `src/estilos/tokens.css`: paleta azul petróleo del servicio de agua, colores semánticos
  para estados (ámbar para plazos, rojo para vencidos o errores, verde para resueltos o sincronizados),
  escala tipográfica, espaciado en múltiplos de 4 px y radios.
- **Tipografía:** Public Sans, con dígitos de ancho fijo (clase `.cifra`) para radicados, números de
  factura y montos.
- **Puntos de corte:** `37.5rem` (600 px, tableta) y `56.25rem` (900 px, escritorio).
---

## 6. Accesibilidad y responsive

Orientado a WCAG 2.1 AA (RNF-03) y a navegadores de escritorio y móviles (RNF-08):

- Estilos **mobile-first**: las tablas se convierten en tarjetas en pantallas pequeñas y el menú del
  back-office pasa a un panel lateral que se abre con un botón y se cierra con `Esc` o tocando fuera.
- Todos los campos tienen etiqueta; los errores se asocian a su campo con `aria-invalid` y
  `aria-describedby`, y al enviar un formulario con errores el foco va al primer campo inválido.
- Ventanas modales con el elemento nativo `<dialog>`: el foco queda dentro, se cierran con `Esc` y el foco
  vuelve al botón que las abrió.
- Enlace "Saltar al contenido principal" (visible al presionar `Tab`), foco visible en todos los controles y
  título de pestaña por pantalla.
- Textos en español con terminología para usuarios no técnicos (RNF-11).

---

## 7. Convenciones

- **Nombres en español**, alineados con el dominio y con el backend (`radicado`, `lote`, `gestor`...).
- **Archivos:** componentes en `PascalCase.jsx` (`BandejaPqr.jsx`); utilidades, hooks y estilos en
  `camelCase` o `kebab-case` (`useFacturacion.js`, `detalle-lote.css`). Windows no distingue mayúsculas en
  los nombres de archivo, pero Linux y el despliegue sí, así que hay que respetar el nombre exacto.
- **Estilos:** cada componente o pantalla importa su propia hoja; lo compartido está en `base.css`,
  `componentes/ui/ui.css` y `componentes/backoffice.css`.
- **Calidad:** `npm run lint` sin advertencias antes de cada commit.
- **Commits:** Conventional Commits con alcance, por ejemplo `feat(frontend): ...` o `fix(frontend): ...`.

---

## 8. Limitaciones del prototipo

- **Sin autenticación ni roles (RNF-05):** el gestor se elige en un selector. Es una limitación compartida
  con el backend.
- **Sin archivos adjuntos (RN-06):** la respuesta a una PQR exige texto, pero no evidencia adjunta.
- **Pagos (F-05) fuera de alcance:** el estado "Pagada" existe en el modelo, pero no hay flujo de pago.
- **Sin pruebas automatizadas del frontend:** están planeadas con Vitest y Testing Library. La API se valida
  con las pruebas del backend y la colección de Postman.

---

## 9. Solución de problemas

| Síntoma | Causa | Solución |
|---|---|---|
| Aviso "Sin conexión" en todas las pantallas | El backend no está en ejecución o `VITE_API_URL` no apunta a él | Levantar el backend y revisar `.env`; después de cambiar `.env`, reiniciar `npm run dev` |
| Página en blanco sin aviso de Vite | Error de JavaScript al ejecutar un componente | Abrir la consola del navegador (`F12` → Console) y revisar el primer error en rojo |
| `does not provide an export named ...` | Un archivo quedó con otra versión o con otro nombre | Verificar el nombre exacto (mayúsculas incluidas) y que el archivo esté actualizado |
| `Failed to resolve import` | El archivo no está en la ruta que indica el `import` | Revisar la carpeta con `ls` |
| El selector "Gestor actual" dice "No disponible" | El backend no responde en `/api/gestores` | Revisar que el backend esté arriba y la configuración CORS |
