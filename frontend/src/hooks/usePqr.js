import { useMutation, useQuery } from "@tanstack/react-query";
import { obtenerTiposSolicitud, registrarPqr } from "../api/pqr";

/** Claves de caché de TanStack Query para el módulo PQR, centralizadas para invalidarlas sin errores. */
export const clavesPqr = {
  todo: ["pqr"],
  tiposSolicitud: ["pqr", "catalogos", "tipos-solicitud"],
};

/** Tipos de solicitud (Petición, Queja, Reclamo). Es un catálogo fijo: no se vuelve a pedir en la sesión. */
export function useTiposSolicitud() {
  return useQuery({
    queryKey: clavesPqr.tiposSolicitud,
    queryFn: obtenerTiposSolicitud,
    staleTime: Infinity,
  });
}

/** Radicación de una PQR desde el portal ciudadano (RF-08). */
export function useRegistrarPqr() {
  return useMutation({ mutationFn: registrarPqr });
}