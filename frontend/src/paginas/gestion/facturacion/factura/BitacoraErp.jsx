import { Cargando } from "../../../../componentes/Cargando";
import { MensajeError } from "../../../../componentes/MensajeError";
import { Etiqueta } from "../../../../componentes/ui/Etiqueta";
import { useSincronizacionesFactura } from "../../../../hooks/useFacturacion";
import { formatearFechaHora } from "../../../../presentacion/fechas";

const exitosa = (sincronizacion) => sincronizacion.estado.toUpperCase() === "EXITOSO";

/**
 * Bitácora técnica de envíos al ERP (SWR-04). Cada registro es un envío completo,
 * que puede incluir varios intentos automáticos antes del resultado final.
 */
export function BitacoraErp({ numeroFactura }) {
  const bitacora = useSincronizacionesFactura(numeroFactura);
  const registros = bitacora.data ?? [];

  return (
    <section className="panel" aria-labelledby="bitacora-titulo">
      <div className="panel-titulo">
        <div>
          <h2 id="bitacora-titulo">Sincronización con ERP</h2>
          <p className="factura-panel-descripcion">Bitácora técnica de los envíos realizados.</p>
        </div>
        {registros.length > 0 && (
          <Etiqueta>
            {registros.length} {registros.length === 1 ? "envío" : "envíos"}
          </Etiqueta>
        )}
      </div>

      {bitacora.isPending && <Cargando texto="Cargando bitácora…" />}
      <MensajeError error={bitacora.error} />
      {bitacora.isSuccess && registros.length === 0 && (
        <p className="factura-panel-descripcion">La factura aún no se ha enviado al ERP.</p>
      )}

      <ol className="factura-bitacora">
        {registros.map((registro) => (
          <li key={registro.id} className={exitosa(registro) ? "factura-envio-exitoso" : "factura-envio-fallido"}>
            <i aria-hidden="true" />
            <div>
              <strong>
                {exitosa(registro) ? "Envío aceptado" : "Envío rechazado"}
                {registro.intentos > 1 && ` tras ${registro.intentos} intentos`}
              </strong>
              <p>{registro.respuestaErp}</p>
              <small>{formatearFechaHora(registro.fechaSincronizacion)}</small>
            </div>
          </li>
        ))}
      </ol>
    </section>
  );
}