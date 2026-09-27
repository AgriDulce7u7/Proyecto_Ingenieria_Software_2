import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  buscarFacturas,
  generarLote,
  listarContratos,
  listarLotes,
  obtenerProgramacion,
  sincronizarFactura,
} from "../api/facturacion";

/** Claves de caché del módulo de facturación. */
export const clavesFacturacion = {
  todo: ["facturacion"],
  programacion: ["facturacion", "programacion"],
  lotes: ["facturacion", "lotes"],
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