import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  buscarFacturas,
  generarLote,
  listarContratos,
  listarFacturasDelLote,
  listarLotes,
  obtenerLote,
  obtenerProgramacion,
  sincronizarFactura,
  sincronizarLote,
} from "../api/facturacion";

/** Claves de caché del módulo de facturación. */
export const clavesFacturacion = {
  todo: ["facturacion"],
  programacion: ["facturacion", "programacion"],
  lotes: ["facturacion", "lotes"],
  lote: (periodo) => ["facturacion", "lotes", periodo],
  facturasLote: (periodo) => ["facturacion", "lotes", periodo, "facturas"],
  facturas: (filtros) => ["facturacion", "facturas", filtros],
  contratos: (estado) => ["facturacion", "contratos", estado ?? "todos"],
};

/** Programación de la facturación automática (SWR-01): próxima ejecución y periodo pendiente. */
export function useProgramacion() {
  return useQuery({ queryKey: clavesFacturacion.programacion, queryFn: obtenerProgramacion });
}

/** Historial de lotes de facturación, del más reciente al más antiguo. */
export function useLotes() {
  return useQuery({ queryKey: clavesFacturacion.lotes, queryFn: listarLotes });
}

/** Contratos de servicio, opcionalmente filtrados por estado (ACTIVO, SUSPENDIDO, INACTIVO). */
export function useContratos(estado) {
  return useQuery({ queryKey: clavesFacturacion.contratos(estado), queryFn: () => listarContratos(estado) });
}

/**
 * Generación manual del lote mensual (RF-05, CU-03). Sin periodo factura el mes anterior.
 * Al terminar se refresca todo el módulo: programación, lotes y facturas.
 */
export function useGenerarLote() {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: generarLote,
    onSuccess: () => clienteConsultas.invalidateQueries({ queryKey: clavesFacturacion.todo }),
  });
}

/** Búsqueda de facturas con los filtros del backend: { periodo, contrato, estado, documento }. */
export function useFacturas(filtros, { habilitada = true } = {}) {
  return useQuery({
    queryKey: clavesFacturacion.facturas(filtros),
    queryFn: () => buscarFacturas(filtros),
    enabled: habilitada,
    placeholderData: (anteriores) => anteriores,
  });
}

/**
 * Reenvía una factura al ERP (SWR-04). El resultado puede ser un rechazo del ERP (exitosa: false),
 * que no es un error de la petición. Al terminar se refrescan facturas y lotes.
 */
export function useSincronizarFactura() {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: sincronizarFactura,
    onSettled: () => clienteConsultas.invalidateQueries({ queryKey: clavesFacturacion.todo }),
  });
}

/** Resumen de un lote: tiempos, totales y estado de sincronización con el ERP (SWR-02, SWR-04). */
export function useLote(periodo) {
  return useQuery({ queryKey: clavesFacturacion.lote(periodo), queryFn: () => obtenerLote(periodo) });
}

/** Facturas generadas en un lote. */
export function useFacturasLote(periodo) {
  return useQuery({
    queryKey: clavesFacturacion.facturasLote(periodo),
    queryFn: () => listarFacturasDelLote(periodo),
  });
}

/** Reintenta el envío al ERP de las facturas pendientes o con error del lote (SWR-04). */
export function useSincronizarLote(periodo) {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: () => sincronizarLote(periodo),
    onSettled: () => clienteConsultas.invalidateQueries({ queryKey: clavesFacturacion.todo }),
  });
}