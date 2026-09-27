import { Link, useNavigate } from "react-router-dom";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import {
  formatearDuracion,
  formatearMoneda,
  formatearNumero,
  partesPeriodo,
  presentarEstadoLote,
} from "../../../presentacion/facturacion";
import { formatearFechaHora } from "../../../presentacion/fechas";

/** Estado de la sincronización con el ERP de las facturas de un lote (SWR-04). */
function SincronizacionErp({ lote }) {
  const total = lote.totalFacturasGeneradas;
  let detalle = { texto: "Sin errores", clase: "" };
  if (lote.facturasConErrorSincronizacion > 0) {
    detalle = { texto: `${lote.facturasConErrorSincronizacion} con error`, clase: "lotes-erp-error" };
  } else if (lote.facturasPendientesSincronizacion > 0) {
    detalle = { texto: `${lote.facturasPendientesSincronizacion} pendientes`, clase: "lotes-erp-pendiente" };
  }

  return (
    <span className="lotes-celda-doble">
      <strong className="cifra">
        {formatearNumero(lote.facturasSincronizadas)} / {formatearNumero(total)}
      </strong>
      <small className={detalle.clase}>{detalle.texto}</small>
    </span>
  );
}

/** Historial de lotes. En pantallas pequeñas cada fila se muestra como una tarjeta (ver lotes.css). */
export function TablaLotes({ lotes }) {
  const navegar = useNavigate();

  const abrirDesdeFila = (periodo) => (evento) => {
    if (!evento.target.closest("a")) navegar(`/gestion/facturacion/lotes/${periodo}`);
  };

  return (
    <div className="lotes-tabla-contenedor">
      <table className="lotes-tabla">
        <thead>
          <tr>
            <th scope="col">Periodo</th>
            <th scope="col">Estado</th>
            <th scope="col">Ejecución</th>
            <th scope="col">Duración</th>
            <th scope="col">Facturas</th>
            <th scope="col">Total facturado</th>
            <th scope="col">Sincronización ERP</th>
            <th scope="col">
              <span className="solo-lectores">Acciones</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {lotes.map((lote) => {
            const estado = presentarEstadoLote(lote.estado);
            return (
              <tr key={lote.periodo} onClick={abrirDesdeFila(lote.periodo)}>
                <td>
                  <Link to={`/gestion/facturacion/lotes/${lote.periodo}`} className="lotes-periodo">
                    {partesPeriodo(lote.periodo).corto}
                  </Link>
                </td>
                <td>
                  <Etiqueta tono={estado.tono}>{estado.etiqueta}</Etiqueta>
                </td>
                <td data-etiqueta="Ejecución">{formatearFechaHora(lote.fechaInicioProceso)}</td>
                <td data-etiqueta="Duración">
                  {lote.duracionSegundos == null ? "En curso" : formatearDuracion(lote.duracionSegundos * 1000)}
                </td>
                <td data-etiqueta="Facturas" className="cifra">
                  {formatearNumero(lote.totalFacturasGeneradas)}
                </td>
                <td data-etiqueta="Total facturado" className="lotes-moneda cifra">
                  {formatearMoneda(lote.valorTotalFacturado)}
                </td>
                <td data-etiqueta="Sincronización ERP">
                  <SincronizacionErp lote={lote} />
                </td>
                <td>
                  <Link
                    to={`/gestion/facturacion/lotes/${lote.periodo}`}
                    className="lotes-ver"
                    aria-label={`Ver detalle del lote de ${partesPeriodo(lote.periodo).texto}`}
                  >
                    <Icono nombre="chevron" tamano={18} />
                  </Link>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}