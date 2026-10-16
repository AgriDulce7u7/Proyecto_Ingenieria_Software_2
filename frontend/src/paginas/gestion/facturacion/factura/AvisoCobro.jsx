import { Icono } from "../../../../componentes/ui/Icono";
import { formatearFecha, formatearFechaHora } from "../../../../presentacion/fechas";

/**
 * Situación de cobro de la factura (DE-01): pagada, vencida, anulada (con su motivo) o incluida
 * en un acuerdo de pago. Una factura por pagar no necesita aviso: la liquidación ya muestra el vencimiento.
 */
export function AvisoCobro({ factura }) {
  let aviso = null;
  if (factura.estadoCobro === "ANULADA") {
    aviso = { clase: "neutro", icono: "cerrar", titulo: "Factura anulada", texto: factura.motivoAnulacion };
  } else if (factura.estadoCobro === "PAGADA") {
    aviso = {
      clase: "exito",
      icono: "check",
      titulo: "Factura pagada",
      texto: factura.fechaPago ? `Pago registrado el ${formatearFechaHora(factura.fechaPago)}.` : "El pago quedó registrado.",
    };
  } else if (factura.enAcuerdoDePago) {
    aviso = {
      clase: "info",
      icono: "documento",
      titulo: "En acuerdo de pago",
      texto: "La deuda se paga por cuotas del acuerdo. Mientras esté vigente, la factura no se puede anular.",
    };
  } else if (factura.estadoCobro === "VENCIDA") {
    aviso = {
      clase: "error",
      icono: "alerta",
      titulo: "Factura vencida",
      texto: `No se pagó antes del ${formatearFecha(factura.fechaVencimiento)}. Cuenta para la cartera del usuario.`,
    };
  }
  if (!aviso) return null;

  return (
    <div className={`factura-aviso factura-aviso-${aviso.clase}`} role="status">
      <Icono nombre={aviso.icono} tamano={18} />
      <div>
        <strong>{aviso.titulo}</strong>
        {aviso.texto && <p>{aviso.texto}</p>}
      </div>
    </div>
  );
}