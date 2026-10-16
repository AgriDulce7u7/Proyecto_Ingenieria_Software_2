import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  adjuntarEvidencia,
  consultarEstadoPqr,
  ejecutarMonitoreo,
  cerrarPqr,
  listarEvidencias,
  obtenerAlertasEquipo,
  obtenerCumplimiento,
  listarPqr,
  obtenerEstados,
  obtenerGestores,
  obtenerHistorialPqr,
  obtenerNotificacionesGestor,
  obtenerNotificacionesPqr,
  obtenerPqr,
  obtenerTiposSolicitud,
  reasignarPqr,
  registrarPqr,
  responderPqr,
  tomarPqr,
} from "../api/pqr";

/** Claves de caché de TanStack Query para el módulo PQR, centralizadas para invalidarlas sin errores. */
export const clavesPqr = {
  todo: ["pqr"],
  tiposSolicitud: ["pqr", "catalogos", "tipos-solicitud"],
  estados: ["pqr", "catalogos", "estados"],
  gestores: ["gestores"],
  notificacionesGestor: (gestorId) => ["gestores", gestorId, "notificaciones"],
  bandeja: (filtros) => ["pqr", "bandeja", filtros],
  consultaPublica: (radicado) => ["pqr", "consulta", radicado],
  detalle: (radicado) => ["pqr", "detalle", radicado],
  historial: (radicado) => ["pqr", "detalle", radicado, "historial"],
  notificaciones: (radicado) => ["pqr", "detalle", radicado, "notificaciones"],
  evidencias: (radicado) => ["pqr", "detalle", radicado, "evidencias"],
  alertasEquipo: ["pqr", "supervision", "alertas"],
  cumplimiento: (periodo) => ["pqr", "indicadores", "cumplimiento", periodo],
};

// Los catálogos no cambian durante la sesión: se piden una sola vez.
const CATALOGO = { staleTime: Infinity };

/** Tipos de solicitud (Petición, Queja, Reclamo). */
export function useTiposSolicitud() {
  return useQuery({ queryKey: clavesPqr.tiposSolicitud, queryFn: obtenerTiposSolicitud, ...CATALOGO });
}

/** Estados de una PQR, en el orden definido por el backend. */
export function useEstadosPqr() {
  return useQuery({ queryKey: clavesPqr.estados, queryFn: obtenerEstados, ...CATALOGO });
}

/** Gestores activos (filtro de la bandeja y reasignación). */
export function useGestores() {
  return useQuery({ queryKey: clavesPqr.gestores, queryFn: obtenerGestores, staleTime: 5 * 60 * 1000 });
}

/** Radicación de una PQR desde el portal ciudadano (RF-08). */
export function useRegistrarPqr() {
  return useMutation({ mutationFn: registrarPqr });
}

/** Consulta pública del estado de una PQR por su radicado (RF-10, CU-07, SWR-08). No consulta si no hay radicado. */
export function useConsultaPqr(radicado) {
  return useQuery({
    queryKey: clavesPqr.consultaPublica(radicado),
    queryFn: () => consultarEstadoPqr(radicado),
    enabled: Boolean(radicado),
  });
}

/**
 * Bandeja de PQR con los filtros del backend: { estado, tipo, gestorId } (RF-09).
 * Se refresca cada minuto para reflejar cambios de estado y vencimientos del monitoreo.
 */
export function useBandejaPqr(filtros = {}) {
  return useQuery({
    queryKey: clavesPqr.bandeja(filtros),
    queryFn: () => listarPqr(filtros),
    refetchInterval: 60 * 1000,
    placeholderData: (anteriores) => anteriores,
  });
}

/** Notificaciones de un gestor (asignaciones, alertas de 48 h y vencimientos). No consulta sin gestor. */
export function useNotificacionesGestor(gestorId) {
  return useQuery({
    queryKey: clavesPqr.notificacionesGestor(gestorId),
    queryFn: () => obtenerNotificacionesGestor(gestorId),
    enabled: Boolean(gestorId),
    refetchInterval: 60 * 1000,
  });
}

/**
 * Ejecuta a demanda el monitoreo de plazos (SWR-07, RN-04). Puede marcar PQR como vencidas
 * y enviar alertas, así que al terminar se refrescan la bandeja y las notificaciones.
 */
export function useEjecutarMonitoreo() {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: ejecutarMonitoreo,
    onSuccess: () =>
      Promise.all([
        clienteConsultas.invalidateQueries({ queryKey: clavesPqr.todo }),
        clienteConsultas.invalidateQueries({ queryKey: clavesPqr.gestores }),
      ]),
  });
}

/** Detalle de una PQR para el back-office, con las acciones que permite su estado (RF-09, CU-06). */
export function usePqrDetalle(radicado) {
  return useQuery({ queryKey: clavesPqr.detalle(radicado), queryFn: () => obtenerPqr(radicado) });
}

/** Trazabilidad de la PQR: cada cambio de estado, asignación y comentario (SWR-09). */
export function useHistorialPqr(radicado) {
  return useQuery({ queryKey: clavesPqr.historial(radicado), queryFn: () => obtenerHistorialPqr(radicado) });
}

/** Notificaciones enviadas por la PQR (al ciudadano y al gestor). */
export function useNotificacionesPqr(radicado) {
  return useQuery({ queryKey: clavesPqr.notificaciones(radicado), queryFn: () => obtenerNotificacionesPqr(radicado) });
}

/** Evidencias documentales adjuntas a la PQR (RN-06, RF-09). */
export function useEvidenciasPqr(radicado) {
  return useQuery({ queryKey: clavesPqr.evidencias(radicado), queryFn: () => listarEvidencias(radicado) });
}

/** Adjunta una evidencia ({ archivo, descripcion }) y refresca la lista y el historial de la PQR. */
export function useAdjuntarEvidencia(radicado) {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: (datos) => adjuntarEvidencia(radicado, datos),
    onSuccess: () => clienteConsultas.invalidateQueries({ queryKey: clavesPqr.detalle(radicado) }),
  });
}

/**
 * PQR pendientes del equipo con su nivel frente al plazo (StR-02c). El backend filtra por el equipo
 * del jefe que inició sesión; el administrador ve toda la oficina. Se refresca cada minuto.
 */
export function useAlertasEquipo() {
  return useQuery({
    queryKey: clavesPqr.alertasEquipo,
    queryFn: obtenerAlertasEquipo,
    refetchInterval: 60 * 1000,
  });
}

/**
 * Indicador de cumplimiento del plazo normativo (SyR-02f, RN-04) para el periodo { desde, hasta }.
 * Sin fechas el backend toma desde el primer día de hace dos meses hasta hoy.
 */
export function useCumplimientoPqr(periodo) {
  return useQuery({
    queryKey: clavesPqr.cumplimiento(periodo),
    queryFn: () => obtenerCumplimiento(periodo),
    placeholderData: (anteriores) => anteriores,
  });
}

/**
 * Acción sobre una PQR (tomar, reasignar, responder o cerrar). El backend responde con el detalle
 * actualizado, que se guarda de inmediato; luego se refrescan la bandeja, el historial y las alertas.
 */
function useAccionPqr(radicado, accion) {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: (datos) => accion(radicado, datos),
    onSuccess: (detalle) => {
      clienteConsultas.setQueryData(clavesPqr.detalle(radicado), detalle);
      return Promise.all([
        clienteConsultas.invalidateQueries({ queryKey: clavesPqr.todo }),
        clienteConsultas.invalidateQueries({ queryKey: clavesPqr.gestores }),
      ]);
    },
  });
}

export const useTomarPqr = (radicado) => useAccionPqr(radicado, tomarPqr);
export const useReasignarPqr = (radicado) => useAccionPqr(radicado, reasignarPqr);
export const useResponderPqr = (radicado) => useAccionPqr(radicado, responderPqr);
export const useCerrarPqr = (radicado) => useAccionPqr(radicado, cerrarPqr);