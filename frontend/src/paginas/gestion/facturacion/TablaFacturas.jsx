import { Link, useNavigate } from "react-router-dom";
import { Boton } from "../../../componentes/ui/Boton";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import {
  formatearConsumo,
  formatearMoneda,
  presentarEstadoCobro,
  presentarEstadoFactura,
  puedeSincronizarse,
} from "../../../presentacion/facturacion";
import { formatearFechaCorta } from "../../../presentacion/fechas";
import "./facturas.css";

/**
 * Listado de facturas con su estado de cobro (DE-01) y frente al ERP. Si se recibe `onReintentar`
 * (el usuario puede operar la facturación), las que no han llegado al ERP ofrecen "Reintentar" (SWR-04);
 * si no, todas llevan al detalle.
 * En pantallas pequeñas cada fila se muestra como una tarjeta (ver facturas.css).
 */
export function TablaFacturas({ facturas, onReintentar, reintentando }) {
  const navegar = useNavigate();

  const abrirDesdeFila = (numero) => (evento) => {
    if (!evento.target.closest("a, button")) navegar(`/gestion/facturacion/facturas/${numero}`);
  };

  return (
    <div className="facturas-tabla-contenedor">
      <table className="facturas-tabla">
        <thead>
          <tr>
            <th scope="col">Factura / contrato</th>
            <th scope="col">Cliente / servicio</th>
            <th scope="col">Consumo</th>
            <th scope="col">Valor total</th>
            <th scope="col">Vencimiento</th>
            <th scope="col">Cobro</th>
            <th scope="col">ERP</th>
            <th scope="col">
              <span className="solo-lectores">Acciones</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {facturas.map((factura) => {
            const estado = presentarEstadoFactura(factura.estado);
            const cobro = presentarEstadoCobro(factura.estadoCobro);
            const anulada = factura.estadoCobro === "ANULADA";
            const enCurso = reintentando === factura.numeroFactura;
            return (
              <tr
                key={factura.numeroFactura}
                className={anulada ? "facturas-fila-anulada" : undefined}
                onClick={abrirDesdeFila(factura.numeroFactura)}
              >
                <td>
                  <span className="facturas-celda-doble">
                    <Link to={`/gestion/facturacion/facturas/${factura.numeroFactura}`} className="facturas-numero cifra">
                      {factura.numeroFactura}
                    </Link>
                    <small className="cifra">{factura.numeroContrato}</small>
                  </span>
                </td>
                <td>
                  <span className="facturas-celda-doble">
                    <span>{factura.cliente}</span>
                    <small>{factura.servicio}</small>
                  </span>
                </td>
                <td data-etiqueta="Consumo" className="cifra">
                  {formatearConsumo(factura.consumo)}
                </td>
                <td data-etiqueta="Valor total" className="facturas-moneda cifra">
                  <span className="facturas-valor">{formatearMoneda(factura.total)}</span>
                </td>
                <td data-etiqueta="Vencimiento">{formatearFechaCorta(factura.fechaVencimiento)}</td>
                <td data-etiqueta="Cobro">
                  <Etiqueta tono={cobro.tono}>{cobro.etiqueta}</Etiqueta>
                </td>
                <td data-etiqueta="ERP">
                  <Etiqueta tono={estado.tono}>{estado.etiqueta}</Etiqueta>
                </td>
                <td>
                  {onReintentar && puedeSincronizarse(factura.estado) ? (
                    <Boton
                      variante="discreto"
                      icono="sincronizar"
                      onClick={() => onReintentar(factura.numeroFactura)}
                      disabled={Boolean(reintentando)}
                      aria-label={`Reintentar la sincronización de ${factura.numeroFactura}`}
                    >
                      {enCurso ? "Enviando…" : "Reintentar"}
                    </Boton>
                  ) : (
                    <Link
                      to={`/gestion/facturacion/facturas/${factura.numeroFactura}`}
                      className="facturas-ver"
                      aria-label={`Ver detalle de ${factura.numeroFactura}`}
                    >
                      <Icono nombre="chevron" tamano={18} />
                    </Link>
                  )}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}