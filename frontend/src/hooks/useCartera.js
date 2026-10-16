import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  ejecutarMonitoreoCartera,
  listarAcuerdos,
  listarProcesosCobro,
  obtenerAcuerdo,
  obtenerReporteCartera,
  pagarCuotaAcuerdo,
  registrarAcuerdo,
  simularAcuerdo,
} from "../api/cartera";

/** Claves de caché de F-07. */
export const clavesCartera = {
  todo: ["cartera"],
  reporte: (filtros) => ["cartera", "reporte", filtros],
  acuerdos: (filtros) => ["cartera", "acuerdos", filtros],
  acuerdo: (numero) => ["cartera", "acuerdo", numero],
  simulacion: (criterio) => ["cartera", "simulacion", criterio],
  procesos: (estado) => ["cartera", "procesos", estado ?? "todos"],
};

/**
 * Un acuerdo, una cuota o el monitoreo cambian facturas (vencida, en acuerdo, pagada), contratos
 * (candidato a suspensión) y pagos, así que se refrescan esos módulos junto con la cartera.
 */
function refrescarRelacionados(clienteConsultas) {
  return Promise.all(
    [clavesCartera.todo, ["facturacion"], ["contratos"], ["pagos"]].map((clave) =>
      clienteConsultas.invalidateQueries({ queryKey: clave }),
    ),
  );
}

/** Reporte de cartera morosa (RF-11, SWR-29) con { municipioId, minPeriodos, soloCandidatos }. */
export function useReporteCartera(filtros) {
  return useQuery({
    queryKey: clavesCartera.reporte(filtros),
    queryFn: () => obtenerReporteCartera(filtros),
    placeholderData: (anteriores) => anteriores,
  });
}

export function useAcuerdos(filtros = {}, { habilitada = true } = {}) {
  return useQuery({
    queryKey: clavesCartera.acuerdos(filtros),
    queryFn: () => listarAcuerdos(filtros),
    enabled: habilitada,
    placeholderData: (anteriores) => anteriores,
  });
}

export function useAcuerdo(numero) {
  return useQuery({ queryKey: clavesCartera.acuerdo(numero), queryFn: () => obtenerAcuerdo(numero) });
}

/** Plan de cuotas calculado por el backend antes de registrar (SWR-30). No reintenta: un 404 o 422 es definitivo. */
export function useSimulacionAcuerdo(criterio) {
  return useQuery({
    queryKey: clavesCartera.simulacion(criterio),
    queryFn: () => simularAcuerdo(criterio),
    enabled: Boolean(criterio.contrato),
    placeholderData: (anteriores) => anteriores,
    retry: false,
  });
}

/**
 * Registro del acuerdo (RF-12, SWR-30). El refresco no se espera: la pantalla navega al detalle de
 * inmediato y, si se esperara, el formulario desaparecería antes (el contrato ya tendría acuerdo vigente).
 */
export function useRegistrarAcuerdo() {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: registrarAcuerdo,
    onSuccess: (acuerdo) => {
      clienteConsultas.setQueryData(clavesCartera.acuerdo(acuerdo.numeroAcuerdo), acuerdo);
      refrescarRelacionados(clienteConsultas);
    },
  });
}

/** Pago de una cuota (en orden). Recibe { numeroAcuerdo, numeroCuota, canal, referencia, valor }. */
export function usePagarCuota() {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: ({ numeroAcuerdo, numeroCuota, ...datos }) => pagarCuotaAcuerdo(numeroAcuerdo, numeroCuota, datos),
    onSuccess: (acuerdo) => {
      clienteConsultas.setQueryData(clavesCartera.acuerdo(acuerdo.numeroAcuerdo), acuerdo);
      return refrescarRelacionados(clienteConsultas);
    },
  });
}

/** Procesos de cobro coercitivo (RN-07), opcionalmente por estado. */
export function useProcesosCobro(estado) {
  return useQuery({
    queryKey: clavesCartera.procesos(estado),
    queryFn: () => listarProcesosCobro(estado),
    placeholderData: (anteriores) => anteriores,
  });
}

/** Monitoreo de cartera manual: vence facturas, incumple acuerdos y marca candidatos (SWR-31, SWR-32). */
export function useMonitoreoCartera() {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: ejecutarMonitoreoCartera,
    onSuccess: () => refrescarRelacionados(clienteConsultas),
  });
}