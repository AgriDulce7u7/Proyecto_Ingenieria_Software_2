import { Link, useNavigate } from "react-router-dom";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import { formatearMoneda } from "../../../presentacion/facturacion";
import { formatearFechaHora } from "../../../presentacion/fechas";
import { presentarCanal } from "../../../presentacion/pagos";

/** Listado de pagos con los estilos de la tabla de la bandeja (tarjetas en móvil). La fila abre el comprobante. */
export function TablaPagos({ pagos }) {
  const navegar = useNavigate();
  const abrirDesdeFila = (codigo) => (evento) => {
    if (!evento.target.closest("a")) navegar(`/gestion/pagos/${codigo}`);
  };

  return (
    <div className="bandeja-tabla-contenedor">
      <table className="bandeja-tabla">
        <thead>
          <tr>
            <th scope="col">Pago</th>
            <th scope="col">Usuario y factura</th>
            <th scope="col">Valor</th>
            <th scope="col">Canal</th>
            <th scope="col">Fecha</th>
            <th scope="col">Registrado por</th>
            <th scope="col">
              <span className="solo-lectores">Acciones</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {pagos.map((pago) => {
            const canal = presentarCanal(pago.canal);
            return (
              <tr key={pago.codigoPago} onClick={abrirDesdeFila(pago.codigoPago)}>
                <td>
                  <span className="bandeja-celda-doble">
                    <Link to={`/gestion/pagos/${pago.codigoPago}`} className="bandeja-radicado cifra">
                      {pago.codigoPago}
                    </Link>
                    <small className="cifra">{pago.numeroComprobante}</small>
                  </span>
                </td>
                <td>
                  <span className="bandeja-celda-doble">
                    <strong>{pago.usuario}</strong>
                    <small>
                      <Link to={`/gestion/facturacion/facturas/${pago.numeroFactura}`} className="pagos-enlace cifra">
                        {pago.numeroFactura}
                      </Link>{" "}
                      · <span className="cifra">{pago.numeroContrato}</span>
                    </small>
                  </span>
                </td>
                <td>
                  <strong className="pagos-valor cifra">{formatearMoneda(pago.valor)}</strong>
                </td>
                <td>
                  <Etiqueta tono={canal.tono}>{canal.etiqueta}</Etiqueta>
                </td>
                <td>{formatearFechaHora(pago.fechaPago)}</td>
                <td>{pago.registradoPor}</td>
                <td>
                  <Link
                    to={`/gestion/pagos/${pago.codigoPago}`}
                    className="bandeja-ver"
                    aria-label={`Ver comprobante de ${pago.codigoPago}`}
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