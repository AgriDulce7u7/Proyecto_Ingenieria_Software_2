import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { generarLote, listarContratos, listarLotes, obtenerProgramacion } from "../api/facturacion";

/** Claves de caché del módulo de facturación. */
export const clavesFacturacion = {
  todo: ["facturacion"],
  programacion: ["facturacion", "programacion"],
  lotes: ["facturacion", "lotes"],
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