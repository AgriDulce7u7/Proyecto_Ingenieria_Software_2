import { useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import { descargarComprobantePdf } from "../../../api/pagos";
import { BotonDescarga } from "../../../componentes/BotonDescarga";
import { Cargando } from "../../../componentes/Cargando";
import { MensajeError } from "../../../componentes/MensajeError";
import { ReciboPago } from "../../../componentes/ReciboPago";
import { Boton } from "../../../componentes/ui/Boton";
import { EnlaceVolver } from "../../../componentes/ui/EnlaceVolver";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { useComprobante } from "../../../hooks/usePagos";
import { useTituloPagina } from "../../../hooks/useTituloPagina";
import { formatearFechaHora } from "../../../presentacion/fechas";
import { presentarCanal } from "../../../presentacion/pagos";
import "../pqr/detalle/detalle.css";
import "./pagos.css";

function ContenidoComprobante({ comprobante, registrado }) {
  const [errorPdf, setErrorPdf] = useState(null);
  const canal = presentarCanal(comprobante.canal);

  return (
    <>
      <div className="detalle-encabezado">
        <div>
          <span className="antetitulo">Comprobante de pago</span>
          <h1 className="cifra">{comprobante.numeroComprobante}</h1>
          <div className="detalle-etiquetas">
            <Etiqueta tono={canal.tono}>{canal.etiqueta}</Etiqueta>
            <span className="cifra">{comprobante.codigoPago}</span>
            <span>{formatearFechaHora(comprobante.fechaPago)}</span>
          </div>
        </div>
        <div className="detalle-acciones">
          <Boton a={`/gestion/facturacion/facturas/${comprobante.numeroFactura}`} variante="secundario" icono="documento">
            Ver factura
          </Boton>
          <BotonDescarga
            descargar={() => descargarComprobantePdf(comprobante.codigoPago)}
            onError={setErrorPdf}
            variante="principal"
          >
            Descargar PDF
          </BotonDescarga>
        </div>
      </div>

      {registrado && (
        <div className="aviso aviso-exito detalle-resultado" role="status">
          <p>
            Se registró el pago y la factura <strong className="cifra">{comprobante.numeroFactura}</strong> quedó
            pagada. Entregue el comprobante al usuario.
          </p>
        </div>
      )}
      {errorPdf && (
        <div className="detalle-resultado">
          <MensajeError error={errorPdf} />
        </div>
      )}

      <div className="comprobante-contenido">
        <ReciboPago
          comprobante={comprobante}
          enlaceFactura={`/gestion/facturacion/facturas/${comprobante.numeroFactura}`}
        />
      </div>
    </>
  );
}

/** Comprobante electrónico de un pago (SWR-22) con su descarga en PDF. */
export function Comprobante() {
  const { codigo } = useParams();
  const { state } = useLocation();
  useTituloPagina(`Pago ${codigo}`);
  const comprobante = useComprobante(codigo);

  return (
    <>
      <div className="detalle-volver">
        <EnlaceVolver a="/gestion/pagos" historial>
          Volver
        </EnlaceVolver>
      </div>

      {comprobante.isPending && <Cargando texto="Cargando el comprobante…" />}
      {comprobante.error?.noEncontrado && (
        <div className="aviso aviso-error" role="alert">
          <p>
            No existe el pago <strong className="cifra">{codigo}</strong>.
          </p>
        </div>
      )}
      {comprobante.error && !comprobante.error.noEncontrado && <MensajeError error={comprobante.error} />}
      {comprobante.data && (
        <ContenidoComprobante key={codigo} comprobante={comprobante.data} registrado={Boolean(state?.registrado)} />
      )}
    </>
  );
}