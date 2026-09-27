import { solicitar } from "./cliente";

/** Endpoints de F-02: registro y seguimiento de PQR. */

// Catálogos (formularios y filtros)
export const obtenerTiposSolicitud = () => solicitar("/pqr/catalogos/tipos-solicitud");
export const obtenerCanales = () => solicitar("/pqr/catalogos/canales");
export const obtenerEstados = () => solicitar("/pqr/catalogos/estados");
export const obtenerGestores = () => solicitar("/gestores");
export const obtenerNotificacionesGestor = (gestorId) => solicitar(`/gestores/${gestorId}/notificaciones`);

// Portal ciudadano
export const registrarPqr = (datos) => solicitar("/pqr", { metodo: "POST", cuerpo: datos });
export const consultarEstadoPqr = (radicado) =>
  solicitar(`/pqr/consulta/${encodeURIComponent(radicado.trim())}`);

// Back-office
export const listarPqr = (filtros) => solicitar("/pqr", { parametros: filtros });
export const obtenerPqr = (radicado) => solicitar(`/pqr/${radicado}`);
export const obtenerHistorialPqr = (radicado) => solicitar(`/pqr/${radicado}/historial`);
export const obtenerNotificacionesPqr = (radicado) => solicitar(`/pqr/${radicado}/notificaciones`);

export const tomarPqr = (radicado, { gestorId }) =>
  solicitar(`/pqr/${radicado}/tomar`, { metodo: "PATCH", cuerpo: { gestorId } });
export const reasignarPqr = (radicado, { gestorId, usuario, motivo }) =>
  solicitar(`/pqr/${radicado}/reasignar`, { metodo: "PATCH", cuerpo: { gestorId, usuario, motivo } });
export const responderPqr = (radicado, { gestorId, respuesta }) =>
  solicitar(`/pqr/${radicado}/responder`, { metodo: "PATCH", cuerpo: { gestorId, respuesta } });
export const cerrarPqr = (radicado, { usuario, observacion }) =>
  solicitar(`/pqr/${radicado}/cerrar`, { metodo: "PATCH", cuerpo: { usuario, observacion } });

export const ejecutarMonitoreo = () => solicitar("/pqr/monitoreo", { metodo: "POST" });
