/**
 * Opciones del menú del back-office. Cada opción indica la capacidad que necesita
 * (sesion/capacidades.js): el menú muestra solo las que el rol puede usar.
 */
export const MENU_GESTION = [
  { a: "/gestion/dashboard", texto: "Dashboard", icono: "tablero", capacidad: "DASHBOARD" },
  { a: "/gestion/pqr", texto: "Gestión de PQR", icono: "documento", capacidad: "PQR_ATENDER" },
  { a: "/gestion/alertas", texto: "Alertas", icono: "campana", capacidad: "PQR_ATENDER" },
  { a: "/gestion/supervision", texto: "Supervisión", icono: "personas", capacidad: "PQR_SUPERVISAR" },
  { a: "/gestion/cumplimiento", texto: "Cumplimiento", icono: "grafica", capacidad: "PQR_INDICADORES" },
  { a: "/gestion/usuarios", texto: "Usuarios", icono: "personas", capacidad: "COMERCIAL_CONSULTAR" },
  { a: "/gestion/contratos", texto: "Contratos", icono: "contrato", capacidad: "COMERCIAL_CONSULTAR" },
  { a: "/gestion/facturacion", texto: "Facturación", icono: "recibo", fin: true, capacidad: "FACTURACION_CONSULTAR" },
  { a: "/gestion/facturacion/lotes", texto: "Lotes de facturación", icono: "capas", capacidad: "FACTURACION_CONSULTAR" },
  { a: "/gestion/facturacion/facturas", texto: "Facturas", icono: "documento", capacidad: "FACTURACION_CONSULTAR" },
  { a: "/gestion/pagos", texto: "Pagos", icono: "tarjeta", capacidad: "FACTURACION_CONSULTAR" },
  { a: "/gestion/cartera", texto: "Cartera morosa", icono: "billetera", fin: true, capacidad: "CARTERA_REPORTE" },
  { a: "/gestion/cartera/acuerdos", texto: "Acuerdos de pago", icono: "acuerdo", capacidad: "CARTERA_GESTIONAR" },
  { a: "/gestion/cartera/cobro", texto: "Cobro y monitoreo", icono: "reloj", capacidad: "CARTERA_GESTIONAR" },
  { a: "/gestion/administracion/funcionarios", texto: "Funcionarios", icono: "escudo", capacidad: "ADMINISTRACION" },
  { a: "/gestion/administracion/roles", texto: "Roles y permisos", icono: "llave", capacidad: "ADMINISTRACION" },
  { a: "/gestion/administracion/auditoria", texto: "Auditoría", icono: "historial", capacidad: "ADMINISTRACION" },
];

/** Opciones del menú que puede usar el usuario, según su función `puede` de la sesión. */
export const menuPermitido = (puede) => MENU_GESTION.filter((opcion) => puede(opcion.capacidad));