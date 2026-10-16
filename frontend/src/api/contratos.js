import { solicitar } from "./cliente";

/** Endpoints de F-04: contratos de servicio, medidores y tarifas (CU-02, RF-04). */

/** Filtros: { estado, servicio, usuario, municipioId, candidatoSuspension }. */
export const listarContratos = (filtros) => solicitar("/contratos", { parametros: filtros });
export const obtenerContrato = (numero) => solicitar(`/contratos/${numero}`);
export const registrarContrato = (datos) => solicitar("/contratos", { metodo: "POST", cuerpo: datos });
export const cambiarEstadoContrato = (numero, { estado, motivo }) =>
  solicitar(`/contratos/${numero}/estado`, { metodo: "PATCH", cuerpo: { estado, motivo } });
export const asignarMedidor = (numero, datos) =>
  solicitar(`/contratos/${numero}/medidor`, { metodo: "PUT", cuerpo: datos });

/** Tarifa que se aplicaría (SWR-18, RN-08) según servicio, municipio y estrato del predio. */
export const obtenerTarifaAplicable = ({ servicio, municipioId, estrato }) =>
  solicitar("/tarifas/aplicable", { parametros: { servicio, municipioId, estrato } });