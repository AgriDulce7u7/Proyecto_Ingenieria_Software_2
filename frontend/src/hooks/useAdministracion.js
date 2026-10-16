import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  actualizarFuncionario,
  cambiarEstadoFuncionario,
  cambiarRolFuncionario,
  consultarAuditoria,
  crearFuncionario,
  listarFuncionarios,
  listarRoles,
  obtenerCatalogoAuditoria,
  obtenerFuncionario,
  restablecerContrasenaFuncionario,
} from "../api/administracion";

/** Claves de caché de administración. La auditoría va bajo "admin" para refrescarse con cada cambio. */
export const clavesAdministracion = {
  todo: ["admin"],
  roles: ["admin", "roles"],
  funcionarios: (filtros) => ["admin", "funcionarios", filtros],
  funcionario: (id) => ["admin", "funcionario", String(id)],
  auditoria: (filtros) => ["admin", "auditoria", filtros],
  catalogo: ["admin", "auditoria-catalogo"],
};

/** Roles del sistema con sus permisos. Están definidos en el backend y no cambian durante la sesión. */
export function useRoles() {
  return useQuery({ queryKey: clavesAdministracion.roles, queryFn: listarRoles, staleTime: Infinity });
}

/** Cuentas de funcionarios con los filtros del backend: { rol, activo }. */
export function useFuncionarios(filtros = {}) {
  return useQuery({
    queryKey: clavesAdministracion.funcionarios(filtros),
    queryFn: () => listarFuncionarios(filtros),
    placeholderData: (anteriores) => anteriores,
  });
}

export function useFuncionario(id) {
  return useQuery({ queryKey: clavesAdministracion.funcionario(id), queryFn: () => obtenerFuncionario(id) });
}

/**
 * Después de cada cambio se guarda la cuenta actualizada y se refrescan los listados y la auditoría,
 * que registra la operación. El refresco no se espera: las pantallas navegan o cierran el diálogo de inmediato.
 */
function alGuardar(clienteConsultas) {
  return (funcionario) => {
    if (funcionario) clienteConsultas.setQueryData(clavesAdministracion.funcionario(funcionario.id), funcionario);
    clienteConsultas.invalidateQueries({ queryKey: clavesAdministracion.todo });
  };
}

/** Creación de la cuenta de un funcionario (CU-10). */
export function useCrearFuncionario() {
  const clienteConsultas = useQueryClient();
  return useMutation({ mutationFn: crearFuncionario, onSuccess: alGuardar(clienteConsultas) });
}

/** Nombre, correo y municipio. El usuario de acceso no se modifica. */
export function useActualizarFuncionario(id) {
  const clienteConsultas = useQueryClient();
  return useMutation({ mutationFn: (datos) => actualizarFuncionario(id, datos), onSuccess: alGuardar(clienteConsultas) });
}

/** Cambio de rol con motivo, que queda en la auditoría (RF-15). */
export function useCambiarRol(id) {
  const clienteConsultas = useQueryClient();
  return useMutation({ mutationFn: (datos) => cambiarRolFuncionario(id, datos), onSuccess: alGuardar(clienteConsultas) });
}

/** Activación o desactivación de la cuenta con motivo. */
export function useCambiarEstadoFuncionario(id) {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: (datos) => cambiarEstadoFuncionario(id, datos),
    onSuccess: alGuardar(clienteConsultas),
  });
}

/**
 * Restablecimiento de la contraseña, que también quita el bloqueo por intentos fallidos. El backend no
 * responde la cuenta, así que se vuelve a pedir para actualizar el estado de bloqueo.
 */
export function useRestablecerContrasena(id) {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: (nuevaContrasena) => restablecerContrasenaFuncionario(id, nuevaContrasena),
    onSuccess: () => {
      clienteConsultas.invalidateQueries({ queryKey: clavesAdministracion.todo });
    },
  });
}

/** Página del log de auditoría (RF-15, CU-11). */
export function useAuditoria(filtros, { habilitada = true } = {}) {
  return useQuery({
    queryKey: clavesAdministracion.auditoria(filtros),
    queryFn: () => consultarAuditoria(filtros),
    enabled: habilitada,
    placeholderData: (anteriores) => anteriores,
  });
}

export function useCatalogoAuditoria() {
  return useQuery({ queryKey: clavesAdministracion.catalogo, queryFn: obtenerCatalogoAuditoria, staleTime: Infinity });
}