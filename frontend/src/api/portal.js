import { descargar, solicitar } from "./cliente";

/**
 * Endpoints de F-06, portal de autogestión ciudadana (IU-01, CU-05). Requieren la sesión del ciudadano:
 * el backend solo devuelve sus propios datos y responde 404 si se pide algo ajeno.
 */

const BASE = "/portal";

export const obtenerPerfilPortal = () => solicitar(`${BASE}/perfil`);
export const actualizarContactoPortal = (datos) =>
  solicitar(`${BASE}/perfil/contacto`, { metodo: "PUT", cuerpo: datos });

export const listarContratosPortal = () => solicitar(`${BASE}/contratos`);
export const obtenerFacturaActual = (numeroContrato) => solicitar(`${BASE}/contratos/${numeroContrato}/factura-actual`);
export const obtenerHistorialConsumos = (numeroContrato, meses = 24) =>
  solicitar(`${BASE}/contratos/${numeroContrato}/historial-consumos`, { parametros: { meses } });

/** Facturas del ciudadano, opcionalmente de un contrato. */
export const listarFacturasPortal = (contrato) => solicitar(`${BASE}/facturas`, { parametros: { contrato } });
export const obtenerFacturaPortal = (numero) => solicitar(`${BASE}/facturas/${numero}`);
export const descargarFacturaPortal = (numero) =>
  descargar(`${BASE}/facturas/${numero}/pdf`, { nombre: `factura-${numero}.pdf` });

/** Pago con tarjeta a través de la pasarela (DS-03). 402 si la rechaza, 504 si no responde. */
export const pagarFacturaEnLinea = (numero, tarjeta) =>
  solicitar(`${BASE}/facturas/${numero}/pago-en-linea`, { metodo: "POST", cuerpo: tarjeta });

export const listarPagosPortal = () => solicitar(`${BASE}/pagos`);
export const obtenerComprobantePortal = (codigoPago) => solicitar(`${BASE}/pagos/${codigoPago}/comprobante`);
export const descargarComprobantePortal = (codigoPago) =>
  descargar(`${BASE}/pagos/${codigoPago}/comprobante/pdf`, { nombre: `comprobante-${codigoPago}.pdf` });