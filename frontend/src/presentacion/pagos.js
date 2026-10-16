/** Presentación de F-05: canales de pago (SWR-24), resultados de la pasarela y reglas para pagar (SWR-23). */

/** Canal del pago (CanalPago en el backend). */
const CANALES = {
  PRESENCIAL: { etiqueta: "Caja", detalle: "Pago en efectivo o datáfono en una oficina de EPQ", tono: "info" },
  BANCO: { etiqueta: "Consignación", detalle: "Consignación o transferencia en un banco aliado", tono: "neutro" },
  EN_LINEA: { etiqueta: "En línea", detalle: "Pago con tarjeta desde el portal ciudadano", tono: "exito" },
};

export const CANALES_CODIGOS = Object.keys(CANALES);

/** Canales que registra un funcionario. Los pagos en línea solo llegan con la confirmación de la pasarela (RN-05). */
export const CANALES_PRESENCIALES = ["PRESENCIAL", "BANCO"];

export const presentarCanal = (codigo) => CANALES[codigo] ?? { etiqueta: codigo, detalle: "", tono: "neutro" };

/** Resultado de un intento de pago en línea (ResultadoIntentoPago en el backend). */
const RESULTADOS_INTENTO = {
  APROBADO: { etiqueta: "Aprobado", tono: "exito" },
  RECHAZADO: { etiqueta: "Rechazado", tono: "peligro" },
  SIN_RESPUESTA: { etiqueta: "Sin respuesta", tono: "advertencia" },
};

export const presentarResultadoIntento = (codigo) => RESULTADOS_INTENTO[codigo] ?? { etiqueta: codigo, tono: "neutro" };

/**
 * Por qué una factura no admite pagos (Factura.validarPagable en el backend, que sigue siendo quien decide).
 * Devuelve null si se puede pagar.
 */
export function motivoNoPagable(factura) {
  if (factura.estadoCobro === "PAGADA") return "La factura ya está pagada.";
  if (factura.estadoCobro === "ANULADA") return "La factura está anulada y no admite pagos.";
  if (factura.enAcuerdoDePago) return "La factura está en un acuerdo de pago vigente: se paga con las cuotas del acuerdo.";
  return null;
}

/** Total recaudado y cantidad de pagos por canal. */
export function resumirPagos(pagos) {
  const porCanal = Object.fromEntries(CANALES_CODIGOS.map((canal) => [canal, 0]));
  let total = 0;
  pagos.forEach((pago) => {
    total += Number(pago.valor);
    porCanal[pago.canal] = (porCanal[pago.canal] ?? 0) + 1;
  });
  return { total, cantidad: pagos.length, porCanal };
}

const FORMATO_RECAUDO = new Intl.NumberFormat("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 0 });

/** 1234567.5 → "$ 1.234.568": cifra corta para las métricas del recaudo. */
export const formatearRecaudo = (valor) => FORMATO_RECAUDO.format(Number(valor ?? 0));