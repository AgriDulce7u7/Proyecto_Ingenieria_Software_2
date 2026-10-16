/**
 * En nombre de qué gestor actúa el usuario sobre una PQR. Replica AutorizacionPqr del backend:
 * - el Gestor de PQR solo actúa como su propio registro de gestor (usuario.gestorId);
 * - el Jefe de PQR y el Administrador pueden actuar como cualquier gestor.
 * El backend sigue siendo quien decide: si algo no cuadra responde 403 o 422 y se muestra el error.
 */

const SUPERVISORES = ["JEFE_PQR", "ADMINISTRADOR"];

export const esSupervisorPqr = (usuario) => SUPERVISORES.includes(usuario?.rol);

/**
 * Gestor que toma la PQR. El gestor la toma a su nombre; el jefe, a su nombre si tiene registro
 * de gestor o, si no (el administrador), a nombre del gestor ya asignado.
 */
export function gestorQueToma(usuario, pqr) {
  if (!usuario) return null;
  if (usuario.rol === "GESTOR_PQR") return usuario.gestorId ?? null;
  return usuario.gestorId ?? pqr.gestor?.id ?? null;
}

/**
 * Gestor que responde. El backend exige que sea el gestor asignado: el gestor solo puede si es él;
 * el jefe y el administrador registran la respuesta a nombre del gestor asignado.
 */
export function gestorQueResponde(usuario, pqr) {
  if (!usuario || !pqr.gestor) return null;
  if (esSupervisorPqr(usuario)) return pqr.gestor.id;
  return usuario.gestorId === pqr.gestor.id ? usuario.gestorId : null;
}