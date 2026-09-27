import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  consultarEstadoPqr,
  ejecutarMonitoreo,
  listarPqr,
  obtenerEstados,
  obtenerGestores,
  obtenerNotificacionesGestor,
  obtenerTiposSolicitud,
  registrarPqr,
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

/** Gestores activos (selector del gestor actual y filtros). */
export function useGestores() {
  return useQuery({ queryKey: clavesPqr.gestores, queryFn: obtenerGestores, staleTime: 5 * 60 * 1000 });
}

/** Radicación de una PQR desde el portal ciudadano (RF-08). */
export function useRegistrarPqr() {
  return useMutation({ mutationFn: registrarPqr });
}

/** Consulta pública del estado de una PQR por su radicado (SWR-08). No consulta si no hay radicado. */
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

/** Notificaciones del gestor actual (asignaciones, alertas de 48 h y vencimientos). */
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