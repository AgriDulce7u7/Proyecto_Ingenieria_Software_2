import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  asignarMedidor,
  cambiarEstadoContrato,
  listarContratos,
  obtenerContrato,
  obtenerTarifaAplicable,
  registrarContrato,
} from "../api/contratos";

/** Claves de caché de F-04. */
export const clavesContratos = {
  todo: ["contratos"],
  lista: (filtros) => ["contratos", "lista", filtros],
  detalle: (numero) => ["contratos", "detalle", numero],
  tarifa: (criterio) => ["contratos", "tarifa", criterio],
};

/**
 * Un contrato nuevo o un cambio de estado también cambia el perfil del usuario (lista sus contratos)
 * y los contratos que usa la facturación, así que se refrescan los tres módulos.
 */
function refrescarRelacionados(clienteConsultas) {
  return Promise.all([
    clienteConsultas.invalidateQueries({ queryKey: clavesContratos.todo }),
    clienteConsultas.invalidateQueries({ queryKey: ["usuarios"] }),
    clienteConsultas.invalidateQueries({ queryKey: ["facturacion", "contratos"] }),
  ]);
}

/** Contratos con los filtros del backend. */
export function useListaContratos(filtros = {}) {
  return useQuery({
    queryKey: clavesContratos.lista(filtros),
    queryFn: () => listarContratos(filtros),
    placeholderData: (anteriores) => anteriores,
  });
}

/** Detalle con tarifa, medidor activo, historial y acciones disponibles. */
export function useContrato(numero) {
  return useQuery({ queryKey: clavesContratos.detalle(numero), queryFn: () => obtenerContrato(numero) });
}

/** Tarifa aplicable al predio elegido. Solo consulta con servicio, municipio y estrato. */
export function useTarifaAplicable(criterio) {
  const completo = Boolean(criterio.servicio && criterio.municipioId && criterio.estrato);
  return useQuery({
    queryKey: clavesContratos.tarifa(criterio),
    queryFn: () => obtenerTarifaAplicable(criterio),
    enabled: completo,
    retry: false,
  });
}

/** Registro de un contrato (CU-02, RF-04). Responde el detalle, que se guarda para la pantalla siguiente. */
export function useRegistrarContrato() {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: registrarContrato,
    onSuccess: (detalle) => {
      clienteConsultas.setQueryData(clavesContratos.detalle(detalle.numeroContrato), detalle);
      return refrescarRelacionados(clienteConsultas);
    },
  });
}

/** Acción sobre un contrato que responde el detalle actualizado. */
function useAccionContrato(numero, accion) {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: (datos) => accion(numero, datos),
    onSuccess: (detalle) => {
      clienteConsultas.setQueryData(clavesContratos.detalle(numero), detalle);
      return refrescarRelacionados(clienteConsultas);
    },
  });
}

/** Cambio de estado con motivo (SWR-20). Recibe { estado, motivo }. */
export const useCambiarEstadoContrato = (numero) => useAccionContrato(numero, cambiarEstadoContrato);
/** Instalación o cambio del medidor (SWR-19). Recibe { numeroSerie, fechaInstalacion, activarContrato }. */
export const useAsignarMedidor = (numero) => useAccionContrato(numero, asignarMedidor);