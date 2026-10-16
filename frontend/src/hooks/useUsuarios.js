import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  actualizarUsuario,
  buscarUsuarios,
  desactivarUsuario,
  obtenerUsuario,
  reactivarUsuario,
  registrarPredio,
  registrarUsuario,
  revalidarIdentidad,
} from "../api/usuarios";

/** Claves de caché de F-03. */
export const clavesUsuarios = {
  todo: ["usuarios"],
  busqueda: (criterios) => ["usuarios", "busqueda", criterios],
  detalle: (codigo) => ["usuarios", "detalle", codigo],
};

/**
 * Búsqueda de usuarios (RF-03, SWR-16). Solo consulta cuando hay un criterio: el backend responde 400
 * sin criterios y limita el resultado a 50 coincidencias.
 */
export function useBusquedaUsuarios(criterios) {
  const hayCriterio = Object.values(criterios).some(Boolean);
  return useQuery({
    queryKey: clavesUsuarios.busqueda(criterios),
    queryFn: () => buscarUsuarios(criterios),
    enabled: hayCriterio,
    placeholderData: (anteriores) => anteriores,
  });
}

/** Registro de un usuario con su predio (CU-01, RF-01). */
export function useRegistrarUsuario() {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: registrarUsuario,
    onSuccess: () => clienteConsultas.invalidateQueries({ queryKey: clavesUsuarios.todo }),
  });
}

/** Perfil del usuario con sus predios y contratos. */
export function useUsuario(codigo) {
  return useQuery({ queryKey: clavesUsuarios.detalle(codigo), queryFn: () => obtenerUsuario(codigo) });
}

/**
 * Acción sobre el perfil que devuelve el perfil actualizado: se guarda de inmediato y se refrescan
 * las búsquedas para que el listado muestre el cambio.
 */
function useAccionUsuario(codigo, accion) {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: (datos) => accion(codigo, datos),
    onSuccess: (perfil) => {
      clienteConsultas.setQueryData(clavesUsuarios.detalle(codigo), perfil);
      return clienteConsultas.invalidateQueries({ queryKey: ["usuarios", "busqueda"] });
    },
  });
}

/** Actualización de datos de contacto (RF-02, SWR-15). Recibe el cuerpo de ActualizarUsuarioRequest. */
export const useActualizarUsuario = (codigo) => useAccionUsuario(codigo, actualizarUsuario);
/** Desactivación lógica; recibe el motivo. */
export const useDesactivarUsuario = (codigo) => useAccionUsuario(codigo, desactivarUsuario);
/** Reactivación; recibe el motivo. */
export const useReactivarUsuario = (codigo) => useAccionUsuario(codigo, reactivarUsuario);
/** Reintento de la validación contra la RNEC (IS-03, SWR-14). */
export const useRevalidarIdentidad = (codigo) => useAccionUsuario(codigo, () => revalidarIdentidad(codigo));

/** Registro de otro predio del usuario. Responde el predio, así que se vuelve a pedir el perfil. */
export function useRegistrarPredio(codigo) {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: (datos) => registrarPredio(codigo, datos),
    onSuccess: () => clienteConsultas.invalidateQueries({ queryKey: clavesUsuarios.detalle(codigo) }),
  });
}