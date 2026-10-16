import { Link } from "react-router-dom";
import { Cargando } from "../../../componentes/Cargando";
import { MensajeError } from "../../../componentes/MensajeError";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import { usePagosPortal } from "../../../hooks/usePortal";
import { useTituloPagina } from "../../../hooks/useTituloPagina";
import { formatearMoneda } from "../../../presentacion/facturacion";
import { formatearFechaHora } from "../../../presentacion/fechas";
import { presentarCanal } from "../../../presentacion/pagos";

/** Pagos del ciudadano por cualquier canal, del más reciente al más antiguo, con su comprobante (SWR-22). */
export function PagosCiudadano() {
  useTituloPagina("Mis pagos");
  const pagos = usePagosPortal();

  return (
    <section className="cuenta-seccion" aria-labelledby="pagos-titulo">
      <div className="cuenta-seccion-titulo">
        <h2 id="pagos-titulo">Mis pagos</h2>
      </div>
      {pagos.isPending && <Cargando texto="Cargando sus pagos…" />}
      <MensajeError error={pagos.error} />
      {pagos.data?.length === 0 && <p className="cuenta-vacio">Aún no tiene pagos registrados.</p>}
      {pagos.data?.length > 0 && (
        <ul className="cuenta-pagos">
          {pagos.data.map((pago) => {
            const canal = presentarCanal(pago.canal);
            return (
              <li key={pago.codigoPago}>
                <Link to={`/mi-cuenta/pagos/${pago.codigoPago}`} className="cuenta-pago">
                  <span className="cuenta-pago-icono" aria-hidden="true">
                    <Icono nombre="check" tamano={18} />
                  </span>
                  <span className="cuenta-pago-datos">
                    <strong className="cifra">{formatearMoneda(pago.valor)}</strong>
                    <small>
                      Factura <span className="cifra">{pago.numeroFactura}</span> · {formatearFechaHora(pago.fechaPago)}
                    </small>
                  </span>
                  <Etiqueta tono={canal.tono}>{canal.etiqueta}</Etiqueta>
                  <span className="cuenta-pago-comprobante">
                    <span className="cifra">{pago.numeroComprobante}</span>
                    <Icono nombre="chevron" tamano={16} />
                  </span>
                </Link>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}