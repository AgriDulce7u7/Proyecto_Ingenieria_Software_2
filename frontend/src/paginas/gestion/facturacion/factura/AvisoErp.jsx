import { Icono } from "../../../../componentes/ui/Icono";
import { useSincronizacionesFactura } from "../../../../hooks/useFacturacion";

/** Mensaje de cada estado de la factura frente al ERP. */
const AVISOS = {
  GENERADA: {
    clase: "pendiente",
    icono: "reloj",
    titulo: "Pendiente de envío",
    texto: () => "La factura es válida en SIGCA-EPQ, pero aún no se ha registrado en el ERP.",
  },
  ERROR_SINCRONIZACION: {
    clase: "error",
    icono: "alerta",
    titulo: "Sincronización pendiente",
    texto: (ultimo) =>
      `La factura es válida en SIGCA-EPQ, pero el ERP rechazó el último envío${ultimo ? `: ${ultimo}` : "."}`,
  },
  SINCRONIZADA: {
    clase: "exito",
    icono: "check",
    titulo: "Registrada en el ERP",
    texto: (ultimo) => ultimo ?? "El ERP aceptó la factura.",
  },
  PAGADA: {
    clase: "exito",
    icono: "check",
    titulo: "Factura pagada",
    texto: () => "El pago quedó registrado.",
  },
};

/** Estado de la factura frente al ERP, con la última respuesta del ERP cuando existe. */
export function AvisoErp({ numeroFactura, estado }) {
  const bitacora = useSincronizacionesFactura(numeroFactura);
  const aviso = AVISOS[estado];
  if (!aviso) return null;

  const ultimaRespuesta = bitacora.data?.[0]?.respuestaErp;

  return (
    <div className={`factura-aviso factura-aviso-${aviso.clase}`} role="status">
      <Icono nombre={aviso.icono} tamano={18} />
      <div>
        <strong>{aviso.titulo}</strong>
        <p>{aviso.texto(ultimaRespuesta)}</p>
      </div>
    </div>
  );
}