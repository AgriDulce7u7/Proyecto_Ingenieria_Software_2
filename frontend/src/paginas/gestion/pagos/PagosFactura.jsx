import { Link } from "react-router-dom";
import { Cargando } from "../../../componentes/Cargando";
import { MensajeError } from "../../../componentes/MensajeError";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { useIntentosPago, usePagos } from "../../../hooks/usePagos";
import { formatearMoneda } from "../../../presentacion/facturacion";
import { formatearFechaHora } from "../../../presentacion/fechas";
import { presentarCanal, presentarResultadoIntento } from "../../../presentacion/pagos";
import "./pagos.css";

/** Pago aplicado a la factura, con enlace a su comprobante. */
function PagoAplicado({ pago }) {
  const canal = presentarCanal(pago.canal);
  return (
    <div className="pagos-factura-pago">
      <div>
        <strong>
          <span className="cifra">{formatearMoneda(pago.valor)}</span> · {canal.etiqueta}
        </strong>
        <small>
          {formatearFechaHora(pago.fechaPago)} · {pago.registradoPor}
        </small>
      </div>
      <Link to={`/gestion/pagos/${pago.codigoPago}`} className="pagos-enlace cifra">
        {pago.numeroComprobante}
      </Link>
    </div>
  );
}

/** Intentos de pago en línea: el resultado de la pasarela y su mensaje (CO-02). */
function IntentosEnLinea({ intentos }) {
  return (
    <div className="pagos-factura-intentos">
      <h3>Intentos en línea</h3>
      <ol>
        {intentos.map((intento) => {
          const resultado = presentarResultadoIntento(intento.resultado);
          return (
            <li key={intento.id}>
              <div>
                <p>{intento.mensaje}</p>
                <small>
                  {formatearFechaHora(intento.fecha)}
                  {intento.ultimosDigitos && ` · Tarjeta terminada en ${intento.ultimosDigitos}`}
                  {intento.intentos > 1 && ` · ${intento.intentos} intentos con la pasarela`}
                </small>
              </div>
              <Etiqueta tono={resultado.tono}>{resultado.etiqueta}</Etiqueta>
            </li>
          );
        })}
      </ol>
    </div>
  );
}

/**
 * Pago e intentos de pago en línea de una factura (CU-04, SWR-22, CO-02). Una factura tiene a lo sumo un
 * pago; los intentos rechazados o sin respuesta de la pasarela no la modifican (RN-05).
 */
export function PagosFactura({ numeroFactura }) {
  const pagos = usePagos({ factura: numeroFactura });
  const intentos = useIntentosPago(numeroFactura);
  const cargando = pagos.isPending || intentos.isPending;
  const pago = pagos.data?.[0];
  const listaIntentos = intentos.data ?? [];

  return (
    <section className="panel" aria-labelledby="pagos-factura-titulo">
      <div className="panel-titulo">
        <h2 id="pagos-factura-titulo">Pagos</h2>
        {listaIntentos.length > 0 && (
          <span className="panel-nota">
            {listaIntentos.length} {listaIntentos.length === 1 ? "intento en línea" : "intentos en línea"}
          </span>
        )}
      </div>
      {cargando && <Cargando texto="Consultando pagos…" />}
      <MensajeError error={pagos.error ?? intentos.error} />
      {!cargando && pago && <PagoAplicado pago={pago} />}
      {!cargando && !pago && !pagos.error && (
        <p className="pagos-factura-vacio">Aún no se ha registrado ningún pago para esta factura.</p>
      )}
      {listaIntentos.length > 0 && <IntentosEnLinea intentos={listaIntentos} />}
    </section>
  );
}