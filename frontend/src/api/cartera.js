import { descargar, solicitar } from "./cliente";

/** Endpoints de F-07, cobro persuasivo (CU-08, RF-11, RF-12, RN-03, RN-07). */

const BASE = "/cartera";

/** Reporte de cartera morosa. Filtros: { municipioId, minPeriodos, soloCandidatos }. */
export const obtenerReporteCartera = (filtros) => solicitar(`${BASE}/morosos`, { parametros: filtros });

/** Exporta el reporte con los mismos filtros. formato: "pdf" | "excel". */
export const exportarReporteCartera = (formato, filtros) =>
  descargar(`${BASE}/morosos/exportar`, {
    parametros: { formato, ...filtros },
    nombre: `cartera-morosa.${formato === "excel" ? "xlsx" : "pdf"}`,
  });

// Acuerdos de pago
export const simularAcuerdo = ({ contrato, numeroCuotas, fechaPrimeraCuota }) =>
  solicitar(`${BASE}/acuerdos/simulacion`, { parametros: { contrato, numeroCuotas, fechaPrimeraCuota } });
export const registrarAcuerdo = (datos) => solicitar(`${BASE}/acuerdos`, { metodo: "POST", cuerpo: datos });
/** Filtros: { estado, contrato }. */
export const listarAcuerdos = (filtros) => solicitar(`${BASE}/acuerdos`, { parametros: filtros });
export const obtenerAcuerdo = (numero) => solicitar(`${BASE}/acuerdos/${numero}`);
/** Pago de una cuota, en orden. Recibe { canal, referencia, valor }. */
export const pagarCuotaAcuerdo = (numero, cuota, datos) =>
  solicitar(`${BASE}/acuerdos/${numero}/cuotas/${cuota}/pago`, { metodo: "POST", cuerpo: datos });

// Cobro coercitivo y monitoreo
export const listarProcesosCobro = (estado) => solicitar(`${BASE}/procesos-cobro`, { parametros: { estado } });
export const ejecutarMonitoreoCartera = () => solicitar(`${BASE}/monitoreo`, { metodo: "POST" });