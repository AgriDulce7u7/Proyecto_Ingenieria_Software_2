import { Link } from "react-router-dom";
import { Boton } from "../../../componentes/ui/Boton";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { formatearMoneda, partesPeriodo, presentarEstadoCobro } from "../../../presentacion/facturacion";
import { formatearFecha } from "../../../presentacion/fechas";
import { estaPorPagar } from "../../../presentacion/portal";

/**
 * Facturas del ciudadano como filas: periodo, servicio, total, vencimiento y estado. Las que están por
 * pagar llevan el botón «Pagar»; las demás, solo el enlace a su detalle.
 */
export function ListaFacturas({ facturas, mostrarServicio = true }) {
  return (
    <ul className="cuenta-facturas">
      {facturas.map((factura) => {
        const cobro = presentarEstadoCobro(factura.estadoCobro);
        const periodo = partesPeriodo(factura.periodo);
        const anulada = factura.estadoCobro === "ANULADA";
        return (
          <li key={factura.numeroFactura} className={anulada ? "cuenta-factura-anulada" : undefined}>
            <div className="cuenta-factura-periodo">
              <strong>{periodo.corto}</strong>
              <Link to={`/mi-cuenta/facturas/${factura.numeroFactura}`} className="cuenta-enlace cifra">
                {factura.numeroFactura}
              </Link>
              {mostrarServicio && (
                <small>
                  {factura.servicio} · <span className="cifra">{factura.numeroContrato}</span>
                </small>
              )}
            </div>
            <div className="cuenta-factura-valor">
              <strong className="cifra">{formatearMoneda(factura.total)}</strong>
              <small>
                {factura.estadoCobro === "PAGADA" ? "Pagada" : `Vence el ${formatearFecha(factura.fechaVencimiento)}`}
              </small>
            </div>
            <div className="cuenta-factura-acciones">
              <Etiqueta tono={cobro.tono}>{cobro.etiqueta}</Etiqueta>
              {estaPorPagar(factura) ? (
                <Boton a={`/mi-cuenta/facturas/${factura.numeroFactura}/pagar`} icono="tarjeta">
                  Pagar
                </Boton>
              ) : (
                <Boton a={`/mi-cuenta/facturas/${factura.numeroFactura}`} variante="secundario">
                  Ver
                </Boton>
              )}
            </div>
          </li>
        );
      })}
    </ul>
  );
}