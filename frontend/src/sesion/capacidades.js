/**
 * Qué puede hacer cada rol en la interfaz, según la matriz de control de acceso del backend
 * (backend/README.md, sección 2). Sirve para mostrar u ocultar menú, rutas y botones; la
 * autorización real la hace siempre el backend, que responde 403 si algo no está permitido.
 *
 * Se usan los códigos de rol y no el arreglo `permisos` del perfil porque esos textos son
 * descriptivos e incompletos (por ejemplo, el administrador puede atender PQR, pero su lista
 * de permisos no lo menciona).
 */

const ADMIN = "ADMINISTRADOR";
const ASESOR = "ASESOR_COMERCIAL";
const GESTOR = "GESTOR_PQR";
const JEFE = "JEFE_PQR";
const CARTERA = "GESTOR_CARTERA";
const GERENTE = "GERENTE";
const DIRECTIVO = "DIRECTIVO";

export const CAPACIDADES = {
  /** Bandeja, detalle, evidencias y cierre de PQR */
  PQR_ATENDER: [ADMIN, GESTOR, JEFE],
  /** Reasignar, ejecutar el monitoreo y ver las alertas del equipo (SWR-11) */
  PQR_SUPERVISAR: [ADMIN, JEFE],
  /** Indicador de cumplimiento de plazos para la SSPD (SyR-02f) */
  PQR_INDICADORES: [ADMIN, JEFE, GERENTE, DIRECTIVO],
  /** Consultar usuarios del servicio y contratos */
  COMERCIAL_CONSULTAR: [ADMIN, ASESOR, GESTOR, JEFE, CARTERA, GERENTE, DIRECTIVO],
  /** Registrar y modificar usuarios y contratos (SWR-15, SWR-20) */
  COMERCIAL_GESTIONAR: [ADMIN, ASESOR],
  /** Consultar lotes, facturas y pagos */
  FACTURACION_CONSULTAR: [ADMIN, ASESOR, CARTERA, GERENTE, DIRECTIVO],
  /** Generar lotes, sincronizar con el ERP, anular facturas y registrar pagos presenciales */
  FACTURACION_OPERAR: [ADMIN, ASESOR],
  /** Reporte de cartera morosa (SWR-29) */
  CARTERA_REPORTE: [CARTERA, GERENTE],
  /** Acuerdos de pago, procesos de cobro y monitoreo de cartera */
  CARTERA_GESTIONAR: [ADMIN, CARTERA],
  /** Registrar el pago de una cuota de un acuerdo */
  CARTERA_PAGAR_CUOTA: [ADMIN, ASESOR, CARTERA],
  /** Dashboard gerencial (SWR-33) */
  DASHBOARD: [GERENTE, DIRECTIVO],
  /** Funcionarios, roles y log de auditoría (RF-14, RF-15) */
  ADMINISTRACION: [ADMIN],
};

/** ¿El rol tiene la capacidad? */
export const rolPuede = (rol, capacidad) => Boolean(rol) && (CAPACIDADES[capacidad] ?? []).includes(rol);