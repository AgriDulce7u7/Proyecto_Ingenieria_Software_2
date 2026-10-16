import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { listarIntentosPago, listarPagos, obtenerComprobante, registrarPagoPresencial } from "../api/pagos";

/** Claves de caché de F-05. */
export const clavesPagos = {
  todo: ["pagos"],
  lista: (filtros) => ["pagos", "lista", filtros],
  comprobante: (codigo) => ["pagos", "comprobante", codigo],
  intentos: (factura) => ["pagos", "intentos", factura],
};

/** Pagos con los filtros del backend: { factura, contrato, canal, desde, hasta }. */
export function usePagos(filtros = {}, { habilitada = true } = {}) {
  return useQuery({
    queryKey: clavesPagos.lista(filtros),
    queryFn: () => listarPagos(filtros),
    enabled: habilitada,
    placeholderData: (anteriores) => anteriores,
  });
}

/** Comprobante electrónico de un pago (SWR-22). */
export function useComprobante(codigoPago) {
  return useQuery({ queryKey: clavesPagos.comprobante(codigoPago), queryFn: () => obtenerComprobante(codigoPago) });
}

/** Intentos de pago en línea de una factura, del más reciente al más antiguo. */
export function useIntentosPago(numeroFactura) {
  return useQuery({ queryKey: clavesPagos.intentos(numeroFactura), queryFn: () => listarIntentosPago(numeroFactura) });
}

/**
 * Registro de un pago presencial o bancario (CU-04, SWR-22 a SWR-24). Responde el comprobante, que se
 * guarda para la pantalla siguiente. La factura pasa a pagada, así que también se refresca la facturación
 * y los contratos (su saldo y la marca de candidato a suspensión dependen de las facturas pendientes).
 */
export function useRegistrarPagoPresencial() {
  const clienteConsultas = useQueryClient();
  return useMutation({
    mutationFn: registrarPagoPresencial,
    onSuccess: (comprobante) => {
      clienteConsultas.setQueryData(clavesPagos.comprobante(comprobante.codigoPago), comprobante);
      return Promise.all([
        clienteConsultas.invalidateQueries({ queryKey: clavesPagos.todo }),
        clienteConsultas.invalidateQueries({ queryKey: ["facturacion"] }),
        clienteConsultas.invalidateQueries({ queryKey: ["contratos"] }),
      ]);
    },
  });
}