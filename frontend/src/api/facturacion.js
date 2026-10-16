import { descargar, solicitar } from "./cliente";

/** Endpoints de F-01: generación de facturas mensuales. */

const BASE = "/facturacion";

export const obtenerProgramacion = () => solicitar(`${BASE}/programacion`);

// Lotes
export const generarLote = (periodo) =>
  solicitar(`${BASE}/lotes`, { metodo: "POST", cuerpo: periodo ? { periodo } : undefined });
export const listarLotes = () => solicitar(`${BASE}/lotes`);
export const obtenerLote = (periodo) => solicitar(`${BASE}/lotes/${periodo}`);
export const listarFacturasDelLote = (periodo) => solicitar(`${BASE}/lotes/${periodo}/facturas`);
export const sincronizarLote = (periodo) => solicitar(`${BASE}/lotes/${periodo}/sincronizacion`, { metodo: "POST" });

// Facturas
export const buscarFacturas = (filtros) => solicitar(`${BASE}/facturas`, { parametros: filtros });
export const obtenerFactura = (numero) => solicitar(`${BASE}/facturas/${numero}`);
export const obtenerSincronizacionesFactura = (numero) => solicitar(`${BASE}/facturas/${numero}/sincronizaciones`);
export const sincronizarFactura = (numero) =>
  solicitar(`${BASE}/facturas/${numero}/sincronizacion`, { metodo: "POST" });
export const anularFactura = (numero, motivo) =>
  solicitar(`${BASE}/facturas/${numero}/anulacion`, { metodo: "POST", cuerpo: { motivo } });
export const descargarPdfFactura = (numero) =>
  descargar(`${BASE}/facturas/${numero}/pdf`, { nombre: `factura-${numero}.pdf` });

// Contratos
export const listarContratos = (estado) => solicitar(`${BASE}/contratos`, { parametros: { estado } });
