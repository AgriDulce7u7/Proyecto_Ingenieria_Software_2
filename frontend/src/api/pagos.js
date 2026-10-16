import { descargar, solicitar } from "./cliente";

/** Endpoints de F-05 en el back-office: pagos presenciales, consulta, comprobantes e intentos en línea (CU-04). */

/** Filtros: { factura, contrato, canal, desde, hasta }. */
export const listarPagos = (filtros) => solicitar("/pagos", { parametros: filtros });

/** Pago en caja (PRESENCIAL) o por consignación (BANCO). Responde el comprobante (SWR-22). */
export const registrarPagoPresencial = (datos) => solicitar("/pagos/presenciales", { metodo: "POST", cuerpo: datos });

export const obtenerComprobante = (codigoPago) => solicitar(`/pagos/${codigoPago}/comprobante`);

export const descargarComprobantePdf = (codigoPago) =>
  descargar(`/pagos/${codigoPago}/comprobante/pdf`, { nombre: `comprobante-${codigoPago}.pdf` });

/** Intentos de pago en línea de una factura: aprobados, rechazados y sin respuesta de la pasarela. */
export const listarIntentosPago = (numeroFactura) =>
  solicitar("/pagos/intentos", { parametros: { factura: numeroFactura } });