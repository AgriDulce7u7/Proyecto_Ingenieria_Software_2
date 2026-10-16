import { useQuery } from "@tanstack/react-query";
import { obtenerDashboard } from "../api/dashboard";

export const clavesDashboard = {
  todo: ["dashboard"],
};

/** Cada cuánto se vuelven a pedir los indicadores mientras la pantalla está abierta. */
const INTERVALO_ACTUALIZACION = 5 * 60 * 1000;

/**
 * Indicadores del dashboard gerencial (SWR-33 a SWR-35). El backend los calcula en cada consulta sobre
 * los datos operativos (SWR-34), así que se piden de nuevo cada 5 minutos y al volver a la pestaña.
 */
export function useDashboard() {
  return useQuery({
    queryKey: clavesDashboard.todo,
    queryFn: obtenerDashboard,
    refetchInterval: INTERVALO_ACTUALIZACION,
    refetchOnWindowFocus: true,
  });
}