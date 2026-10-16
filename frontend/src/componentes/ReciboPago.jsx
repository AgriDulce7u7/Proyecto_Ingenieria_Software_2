import { Link } from "react-router-dom";
import { formatearMoneda, partesPeriodo, presentarEstadoCobro } from "../presentacion/facturacion";
import { formatearFechaHora } from "../presentacion/fechas";
import { presentarCanal } from "../presentacion/pagos";
import { enmascararDocumento } from "../presentacion/texto";
import { Icono } from "./ui/Icono";
import "./recibo-pago.css";

/** "CC 1094950001" → "CC ••• ••• 0001" */
function documentoEnmascarado(documento = "") {
  const [tipo, ...numero] = documento.split(" ");
  return numero.length ? `${tipo} ${enmascararDocumento(numero.join(" "))}` : enmascararDocumento(tipo);
}

/**
 * Comprobante electrónico de pago (SWR-22) con el formato de un recibo: valor pagado arriba y los datos
 * del pago, la factura y el usuario debajo. Lo usan el back-office y el portal ciudadano.
 *
 * @param enlaceFactura ruta del detalle de la factura, o null si no se enlaza
 */
export function ReciboPago({ comprobante, enlaceFactura = null }) {
  const canal = presentarCanal(comprobante.canal);
  const estadoFactura = presentarEstadoCobro(comprobante.estadoFactura);
  const filas = [
    { termino: "Número de comprobante", valor: comprobante.numeroComprobante, cifra: true },
    { termino: "Código del pago", valor: comprobante.codigoPago, cifra: true },
    {
      termino: "Factura",
      valor: enlaceFactura ? (
        <Link to={enlaceFactura} className="recibo-enlace">
          {comprobante.numeroFactura}
        </Link>
      ) : (
        comprobante.numeroFactura
      ),
      cifra: true,
    },
    { termino: "Periodo facturado", valor: comprobante.periodo && partesPeriodo(comprobante.periodo).corto },
    { termino: "Contrato", valor: comprobante.numeroContrato, cifra: true },
    { termino: "Servicio", valor: comprobante.servicio },
    { termino: "Usuario", valor: comprobante.usuario },
    { termino: "Documento", valor: documentoEnmascarado(comprobante.documentoUsuario) },
    { termino: "Canal", valor: canal.etiqueta },
    { termino: "Código de transacción", valor: comprobante.codigoTransaccion, cifra: true },
    { termino: "Referencia", valor: comprobante.referencia, cifra: true },
    { termino: "Registrado por", valor: comprobante.registradoPor },
    { termino: "Estado de la factura", valor: estadoFactura.etiqueta },
  ];

  return (
    <article className="recibo" aria-labelledby="recibo-valor">
      <header className="recibo-cabecera">
        <span className="recibo-confirmado">
          <Icono nombre="check" tamano={16} />
          Pago confirmado
        </span>
        <span className="antetitulo">Valor pagado</span>
        <strong id="recibo-valor" className="recibo-valor cifra">
          {formatearMoneda(comprobante.valor)}
        </strong>
        <p>
          {formatearFechaHora(comprobante.fechaPago)} · {canal.detalle}
        </p>
      </header>
      <dl className="recibo-datos">
        {filas
          .filter((fila) => fila.valor)
          .map((fila) => (
            <div key={fila.termino}>
              <dt>{fila.termino}</dt>
              <dd className={fila.cifra ? "cifra" : undefined}>{fila.valor}</dd>
            </div>
          ))}
      </dl>
    </article>
  );
}