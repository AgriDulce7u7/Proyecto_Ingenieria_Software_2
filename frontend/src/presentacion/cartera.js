/** Presentación de F-07: rangos de mora, estados de acuerdos, cuotas y procesos de cobro. */

/** Rangos de antigüedad del reporte (CalculadoraMora.rangoMora en el backend), del más leve al más grave. */
export const RANGOS_MORA = [
  { codigo: "0-30 días", tono: "info" },
  { codigo: "31-60 días", tono: "advertencia" },
  { codigo: "61-90 días", tono: "peligro" },
  { codigo: "Más de 90 días", tono: "peligro" },
];

export const tonoRangoMora = (rango) => RANGOS_MORA.find((r) => r.codigo === rango)?.tono ?? "neutro";

/** Estado del acuerdo de pago (EstadoAcuerdo en el backend). */
const ESTADOS_ACUERDO = {
  VIGENTE: { etiqueta: "Vigente", tono: "info", detalle: "Al día con las cuotas" },
  INCUMPLIDO: { etiqueta: "Incumplido", tono: "peligro", detalle: "Una cuota venció sin pago" },
  CUMPLIDO: { etiqueta: "Cumplido", tono: "exito", detalle: "Todas las cuotas pagadas" },
};

export const ESTADOS_ACUERDO_CODIGOS = Object.keys(ESTADOS_ACUERDO);

export const presentarEstadoAcuerdo = (estado) =>
  ESTADOS_ACUERDO[estado] ?? { etiqueta: estado, tono: "neutro", detalle: "" };

/** Estado de una cuota (EstadoCuota en el backend). */
const ESTADOS_CUOTA = {
  PENDIENTE: { etiqueta: "Pendiente", tono: "advertencia" },
  PAGADA: { etiqueta: "Pagada", tono: "exito" },
  VENCIDA: { etiqueta: "Vencida", tono: "peligro" },
};

export const presentarEstadoCuota = (estado) => ESTADOS_CUOTA[estado] ?? { etiqueta: estado, tono: "neutro" };

/** Estado del proceso de cobro coercitivo (EstadoProcesoCobro, RN-07). */
const ESTADOS_PROCESO = {
  ACTIVO: { etiqueta: "Cobro activo", tono: "peligro", detalle: "Sin acuerdo de pago" },
  SUSPENDIDO: { etiqueta: "Suspendido", tono: "info", detalle: "Por acuerdo de pago vigente" },
  CERRADO: { etiqueta: "Cerrado", tono: "exito", detalle: "Deuda saldada" },
};

export const ESTADOS_PROCESO_CODIGOS = Object.keys(ESTADOS_PROCESO);

export const presentarEstadoProceso = (estado) =>
  ESTADOS_PROCESO[estado] ?? { etiqueta: estado ?? "Sin proceso", tono: "neutro", detalle: "" };

/** Límites del plan de cuotas (CarteraProperties en el backend: minCuotas 2, maxCuotas 12). */
export const CUOTAS_MINIMAS = 2;
export const CUOTAS_MAXIMAS = 12;

/** Primera cuota pendiente del acuerdo: las cuotas se pagan en orden. */
export const siguienteCuota = (acuerdo) => acuerdo.cuotas.find((cuota) => cuota.estado !== "PAGADA") ?? null;

/** Porcentaje pagado del acuerdo (0 a 100). */
export const porcentajePagado = (acuerdo) =>
  Number(acuerdo.totalDeuda) > 0 ? Math.min(100, (Number(acuerdo.valorPagado) / Number(acuerdo.totalDeuda)) * 100) : 0;

/** Cantidad de acuerdos en cada estado: { VIGENTE: 3, INCUMPLIDO: 1, CUMPLIDO: 0 }. */
export const contarPorEstado = (acuerdos) =>
  Object.fromEntries(
    ESTADOS_ACUERDO_CODIGOS.map((codigo) => [codigo, acuerdos.filter((acuerdo) => acuerdo.estado === codigo).length]),
  );