import { solicitar } from "./cliente";

/** Endpoints de administración: funcionarios y roles (CU-10, RF-14) y log de auditoría (CU-11, RF-15). */

const BASE = "/admin";

export const listarRoles = () => solicitar(`${BASE}/roles`);

/** Filtros: { rol, activo }. */
export const listarFuncionarios = (filtros) => solicitar(`${BASE}/funcionarios`, { parametros: filtros });
export const obtenerFuncionario = (id) => solicitar(`${BASE}/funcionarios/${id}`);
export const crearFuncionario = (datos) => solicitar(`${BASE}/funcionarios`, { metodo: "POST", cuerpo: datos });
/** Recibe { nombreCompleto, correo, municipioId }. */
export const actualizarFuncionario = (id, datos) =>
  solicitar(`${BASE}/funcionarios/${id}`, { metodo: "PUT", cuerpo: datos });
/** Recibe { rol, gestorId, motivo }. */
export const cambiarRolFuncionario = (id, datos) =>
  solicitar(`${BASE}/funcionarios/${id}/rol`, { metodo: "PATCH", cuerpo: datos });
/** Recibe { activo, motivo }. */
export const cambiarEstadoFuncionario = (id, datos) =>
  solicitar(`${BASE}/funcionarios/${id}/estado`, { metodo: "PATCH", cuerpo: datos });
/** Restablece la contraseña y desbloquea la cuenta. Responde 204 sin cuerpo. */
export const restablecerContrasenaFuncionario = (id, nuevaContrasena) =>
  solicitar(`${BASE}/funcionarios/${id}/contrasena`, { metodo: "POST", cuerpo: { nuevaContrasena } });

/**
 * Log de auditoría paginado (la página empieza en 0). Filtros: { usuario, modulo, accion, entidad,
 * entidadId, desde, hasta, soloFallidos, pagina, tamano }.
 */
export const consultarAuditoria = (filtros) => solicitar(`${BASE}/auditoria`, { parametros: filtros });
/** Módulos y acciones auditables: { modulos: [...], acciones: [...] }. */
export const obtenerCatalogoAuditoria = () => solicitar(`${BASE}/auditoria/catalogo`);