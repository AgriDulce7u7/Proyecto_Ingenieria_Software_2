import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  actualizarContactoPortal,
  listarContratosPortal,
  listarFacturasPortal,
  listarPagosPortal,
  obtenerComprobantePortal,
  obtenerFacturaActual,
  obtenerFacturaPortal,
  obtenerHistorialConsumos,
  obtenerPerfilPortal,
  pagarFacturaEnLinea,
} from "../api/portal";

/**
 * Claves de caché del portal ciudadano. Al cambiar de sesión, SesionProvider vacía la caché,
 * así que nunca se mezclan datos de dos ciudadanos en el mismo navegador.
 */
export const clavesPortal = {
  todo: ["portal"],
  perfil: ["portal", "perfil"],
  contratos: ["portal", "contratos"],
  facturaActual: (contrato) => ["portal", "contratos", contrato, "factura-actual"],
  historial: (contrato) => ["portal", "contratos", contrato, "historial"],
  facturas: (contrato) => ["portal", "facturas", contrato ?? "todas"],
  factura: (numero) => ["portal", "factura", numero],
  pagos: ["portal", "pagos"],
  comprobante: (codigo) => ["portal", "comprobante", codigo],
};

export function usePerfilPortal() {
  return useQuery({ queryKey: clavesPortal.perfil, queryFn: obtenerPerfilPortal });
}

/** Contratos vigentes del ciudadano con su saldo pendiente (CU-05 paso 2). */
export function useContratosPortal() {
  return useQuery({ queryKey: clavesPortal.contratos, queryFn: listarContratosPortal });
}

/** Factura del periodo actual o la fecha estimada en que estará disponible (SWR-25, SWR-28). */
export function useFacturaActual(numeroContrato) {
  return useQuery({
    queryKey: clavesPortal.facturaActual(numeroContrato),
    queryFn: () => obtenerFacturaActual(numeroContrato),
  });
}

/** Consumos de los últimos 24 meses para la gráfica y la tabla (SWR-26). */
export function useHistorialConsumos(numeroContrato) {
  return useQuery({
    queryKey: clavesPortal.historial(numeroContrato),
    queryFn: () => obtenerHistorialConsumos(numeroContrato),
  });
}

export function useFacturasPortal(contrato) {
  return useQuery({
    queryKey: clavesPortal.facturas(contrato),
    queryFn: () => listarFacturasPortal(contrato),
    placeholderData: (anteriores) => anteriores,
  });
}

export function useFacturaPortal(numero) {
  return useQuery({ queryKey: clavesPortal.factura(numero), queryFn: () => obtenerFacturaPortal(numero) });
}

export function usePagosPortal() {
  return useQuery({ queryKey: clavesPortal.pagos, queryFn: listarPagosPortal });
}

export function useComprobantePortal(codigo) {
  return useQuery({ queryKey: clavesPortal.comprobante(codigo), queryFn: () => obtenerComprobantePortal(codigo) });
}

/** Actualización de correo, teléfono y dirección de notificación. Responde el perfil actualizado. */
export function useActualizarContacto() {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: actualizarContactoPortal,
    onSuccess: (perfil) => clienteConsultas.setQueryData(clavesPortal.perfil, perfil),
  });
}

/**
 * Pago en línea de una factura (CU-04, DS-03). Responde el comprobante, que se guarda para la pantalla
 * siguiente. Haya o no pago, se refresca todo el portal: un intento rechazado no cambia la factura,
 * pero uno aprobado cambia el saldo, la factura y la lista de pagos. El refresco no se espera: la
 * pantalla navega al comprobante de inmediato y, si se esperara, el formulario desaparecería antes
 * porque la factura ya estaría pagada.
 */
export function usePagarEnLinea(numeroFactura) {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: (tarjeta) => pagarFacturaEnLinea(numeroFactura, tarjeta),
    onSuccess: (comprobante) => {
      clienteConsultas.setQueryData(clavesPortal.comprobante(comprobante.codigoPago), comprobante);
    },
    onSettled: () => {
      clienteConsultas.invalidateQueries({ queryKey: clavesPortal.todo });
    },
  });
}