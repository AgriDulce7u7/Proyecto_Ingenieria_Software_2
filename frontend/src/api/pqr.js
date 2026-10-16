import { descargar, solicitar } from "./cliente";

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

// Evidencias documentales (RN-06: la respuesta exige al menos una)
export const listarEvidencias = (radicado) => solicitar(`/pqr/${radicado}/evidencias`);
export function adjuntarEvidencia(radicado, { archivo, descripcion }) {
  const formulario = new FormData();
  formulario.append("archivo", archivo);
  return solicitar(`/pqr/${radicado}/evidencias`, {
    metodo: "POST",
    cuerpo: formulario,
    parametros: { descripcion },
  });
}
export const descargarEvidencia = (radicado, evidencia) =>
  descargar(`/pqr/${radicado}/evidencias/${evidencia.id}/archivo`, { nombre: evidencia.nombreArchivo });

// Supervisión e indicadores (StR-02c, SyR-02f)
export const obtenerAlertasEquipo = () => solicitar("/pqr/supervision/alertas");
export const obtenerCumplimiento = ({ desde, hasta } = {}) =>
  solicitar("/pqr/indicadores/cumplimiento", { parametros: { desde, hasta } });
export const exportarCumplimiento = ({ formato, desde, hasta }) =>
  descargar("/pqr/indicadores/cumplimiento/exportar", {
    parametros: { formato, desde, hasta },
    nombre: `cumplimiento-pqr.${formato === "excel" ? "xlsx" : "pdf"}`,
  });

export const ejecutarMonitoreo = () => solicitar("/pqr/monitoreo", { metodo: "POST" });