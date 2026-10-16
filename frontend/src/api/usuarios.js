import { solicitar } from "./cliente";

/** Endpoints de F-03: usuarios del servicio y sus predios (CU-01, RF-01 a RF-03). */

const BASE = "/usuarios-servicio";

/** Búsqueda por { identificacion, nombre, direccion }. El backend exige al menos un criterio. */
export const buscarUsuarios = (criterios) => solicitar(BASE, { parametros: criterios });
export const registrarUsuario = (datos) => solicitar(BASE, { metodo: "POST", cuerpo: datos });
export const obtenerUsuario = (codigo) => solicitar(`${BASE}/${codigo}`);
export const actualizarUsuario = (codigo, datos) => solicitar(`${BASE}/${codigo}`, { metodo: "PUT", cuerpo: datos });
export const desactivarUsuario = (codigo, motivo) =>
  solicitar(`${BASE}/${codigo}/desactivacion`, { metodo: "PATCH", cuerpo: { motivo } });
export const reactivarUsuario = (codigo, motivo) =>
  solicitar(`${BASE}/${codigo}/reactivacion`, { metodo: "PATCH", cuerpo: { motivo } });
export const revalidarIdentidad = (codigo) => solicitar(`${BASE}/${codigo}/validacion-rnec`, { metodo: "POST" });
export const registrarPredio = (codigo, datos) =>
  solicitar(`${BASE}/${codigo}/predios`, { metodo: "POST", cuerpo: datos });