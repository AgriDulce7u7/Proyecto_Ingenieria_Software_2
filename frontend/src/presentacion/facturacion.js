/** Presentación de datos de facturación (F-01): periodos, estados de lote, incidencias y cifras. */

const LOCALE = "es-CO";
const FORMATO_NUMERO = new Intl.NumberFormat(LOCALE);
const FORMATO_MES = new Intl.DateTimeFormat(LOCALE, { month: "long" });

/** 12486 → "12.486" */
export const formatearNumero = (valor) => FORMATO_NUMERO.format(valor ?? 0);

/** "2026-08" → { mes: "Agosto", anio: "2026", texto: "agosto de 2026" } */
export function partesPeriodo(periodo) {
  const [anio, mes] = periodo.split("-").map(Number);
  const nombreMes = FORMATO_MES.format(new Date(anio, mes - 1, 1));
  return {
    mes: nombreMes[0].toUpperCase() + nombreMes.slice(1),
    anio: String(anio),
    texto: `${nombreMes} de ${anio}`,
  };
}

/** Duración del procesamiento de un lote: "850 ms", "1,5 s" o "18 min 42 s". */
export function formatearDuracion(milisegundos) {
  if (milisegundos < 1000) return `${milisegundos} ms`;
  const segundos = milisegundos / 1000;
  if (segundos < 60) return `${segundos.toLocaleString(LOCALE, { maximumFractionDigits: 1 })} s`;
  const minutos = Math.floor(segundos / 60);
  return `${minutos} min ${Math.round(segundos % 60)} s`;
}

/** Estado del lote (EstadoLote en el backend, más SIN_EJECUTAR cuando aún no existe). */
const ESTADOS_LOTE = {
  SIN_EJECUTAR: { etiqueta: "Pendiente", tono: "advertencia" },
  EN_PROCESO: { etiqueta: "En proceso", tono: "info" },
  COMPLETADO: { etiqueta: "Completado", tono: "exito" },
  ERROR: { etiqueta: "Con error", tono: "peligro" },
};

export const presentarEstadoLote = (estado) => ESTADOS_LOTE[estado] ?? { etiqueta: estado, tono: "neutro" };

/** Motivo de una incidencia al liquidar un contrato (TipoResultadoFacturacion en el backend). */
const INCIDENCIAS = {
  SIN_MEDIDOR: "Sin medidor activo",
  SIN_LECTURA: "Sin lectura del periodo",
  SIN_TARIFA: "Sin tarifa vigente",
  ERROR: "Error al liquidar",
};

export const presentarIncidencia = (tipo) => INCIDENCIAS[tipo] ?? tipo;