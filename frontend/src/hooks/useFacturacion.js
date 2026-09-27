import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { generarLote, listarContratos, obtenerProgramacion } from "../api/facturacion";

/** Claves de caché del módulo de facturación. */
export const clavesFacturacion = {
  todo: ["facturacion"],
  programacion: ["facturacion", "programacion"],
  contratos: (estado) => ["facturacion", "contratos", estado ?? "todos"],
};

/** Programación de la facturación automática (SWR-01): próxima ejecución y periodo pendiente. */
export function useProgramacion() {
  return useQuery({ queryKey: clavesFacturacion.programacion, queryFn: obtenerProgramacion });
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