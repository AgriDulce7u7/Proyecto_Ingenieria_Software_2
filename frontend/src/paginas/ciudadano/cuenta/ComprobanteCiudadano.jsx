import { useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import { descargarComprobantePortal } from "../../../api/portal";
import { BotonDescarga } from "../../../componentes/BotonDescarga";
import { Cargando } from "../../../componentes/Cargando";
import { MensajeError } from "../../../componentes/MensajeError";
import { ReciboPago } from "../../../componentes/ReciboPago";
import { Boton } from "../../../componentes/ui/Boton";
import { EnlaceVolver } from "../../../componentes/ui/EnlaceVolver";
import { useComprobantePortal } from "../../../hooks/usePortal";
import { useTituloPagina } from "../../../hooks/useTituloPagina";

/**
 * Comprobante de un pago del ciudadano (SWR-22). Tras un pago en línea aprobado se llega aquí de
 * inmediato, con el aviso de confirmación.
 */
export function ComprobanteCiudadano() {
  const { codigo } = useParams();
  const { state } = useLocation();
  useTituloPagina("Comprobante de pago");
  const comprobante = useComprobantePortal(codigo);
  const [errorPdf, setErrorPdf] = useState(null);
  const datos = comprobante.data;

  return (
    <>
      <div className="cuenta-volver">
        <EnlaceVolver a="/mi-cuenta/pagos" historial>
          Mis pagos
        </EnlaceVolver>
      </div>

      {comprobante.isPending && <Cargando texto="Cargando el comprobante…" />}
      {comprobante.error?.noEncontrado && (
        <div className="aviso aviso-error" role="alert">
          <p>
            No encontramos el pago <strong className="cifra">{codigo}</strong> entre sus pagos.
          </p>
        </div>
      )}
      {comprobante.error && !comprobante.error.noEncontrado && <MensajeError error={comprobante.error} />}

      {datos && (
        <div className="cuenta-comprobante">
          {state?.pagado && (
            <div className="aviso aviso-exito" role="status">
              <p>
                <strong>¡Pago aprobado!</strong> La factura <span className="cifra">{datos.numeroFactura}</span> quedó
                pagada. Guarde este comprobante.
              </p>
            </div>
          )}
          {errorPdf && <MensajeError error={errorPdf} />}
          <ReciboPago comprobante={datos} enlaceFactura={`/mi-cuenta/facturas/${datos.numeroFactura}`} />
          <div className="cuenta-comprobante-acciones">
            <Boton a="/mi-cuenta" variante="secundario">
              Volver a mi cuenta
            </Boton>
            <BotonDescarga
              descargar={() => descargarComprobantePortal(datos.codigoPago)}
              onError={setErrorPdf}
              variante="principal"
            >
              Descargar comprobante
            </BotonDescarga>
          </div>
        </div>
      )}
    </>
  );
}