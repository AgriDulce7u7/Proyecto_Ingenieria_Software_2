import { useQuery } from "@tanstack/react-query";
import { obtenerMunicipios } from "../api/territorio";

export const clavesTerritorio = {
  municipios: ["municipios"],
};

/** Municipios del Quindío. No cambian durante la sesión: se piden una sola vez. */
export function useMunicipios() {
  return useQuery({ queryKey: clavesTerritorio.municipios, queryFn: obtenerMunicipios, staleTime: Infinity });
}