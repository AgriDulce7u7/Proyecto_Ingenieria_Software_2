import { useState } from "react";
import { EncabezadoGestion } from "../../../../componentes/EncabezadoGestion";
import { Boton } from "../../../../componentes/ui/Boton";
import { EnlaceVolver } from "../../../../componentes/ui/EnlaceVolver";
import { Etiqueta } from "../../../../componentes/ui/Etiqueta";
import { presentarEstadoAcuerdo } from "../../../../presentacion/cartera";
import { formatearMoneda } from "../../../../presentacion/facturacion";
import { useSesion } from "../../../../sesion/useSesion";
import { FormularioPagoCuota } from "./FormularioPagoCuota";
import "../../pagos/pagos.css";
import "../cartera.css";

/** Resultado del pago: la cuota pagada y cómo queda el acuerdo. */
function ResultadoPago({ acuerdo, numeroCuota, onOtro }) {
  const puedeVerAcuerdo = useSesion().puede("CARTERA_GESTIONAR");
  const cuota = acuerdo.cuotas.find((elemento) => elemento.numeroCuota === numeroCuota);
  const estado = presentarEstadoAcuerdo(acuerdo.estado);

  return (
    <div className="cuota-resultado">
      <div className="aviso aviso-exito" role="status">
        <p>
          Se registró el pago de la cuota {numeroCuota} del acuerdo{" "}
          <strong className="cifra">{acuerdo.numeroAcuerdo}</strong>
          {cuota && (
            <>
              {" "}
              por <span className="cifra">{formatearMoneda(cuota.valor)}</span>
            </>
          )}
          .
          {acuerdo.estado === "CUMPLIDO" && " Era la última: las facturas del acuerdo quedaron pagadas."}
        </p>
      </div>
      <dl className="cuota-resumen">
        <div>
          <dt>Usuario</dt>
          <dd>{acuerdo.usuario}</dd>
        </div>
        <div>
          <dt>Contrato</dt>
          <dd className="cifra">{acuerdo.numeroContrato}</dd>
        </div>
        <div>
          <dt>Saldo del acuerdo</dt>
          <dd className="cifra">{formatearMoneda(acuerdo.saldoPendiente)}</dd>
        </div>
        <div>
          <dt>Estado</dt>
          <dd>
            <Etiqueta tono={estado.tono}>{estado.etiqueta}</Etiqueta>
          </dd>
        </div>
      </dl>
      <div className="cuota-resultado-acciones">
        {puedeVerAcuerdo && (
          <Boton a={`/gestion/cartera/acuerdos/${acuerdo.numeroAcuerdo}`} variante="secundario">
            Ver acuerdo
          </Boton>
        )}
        <Boton onClick={onOtro} icono="mas">
          Registrar otra cuota
        </Boton>
      </div>
    </div>
  );
}

/**
 * Pago de una cuota de acuerdo en ventanilla (CU-08). El asesor no consulta acuerdos, así que se identifica
 * la cuota con el número del acuerdo y el de la cuota que trae el usuario en su plan de pagos.
 */
export function PagarCuota() {
  const [resultado, setResultado] = useState(null);
  const [intento, setIntento] = useState(0);

  return (
    <>
      <div className="detalle-volver-cartera">
        <EnlaceVolver a="/gestion/pagos" historial>
          Volver
        </EnlaceVolver>
      </div>
      <EncabezadoGestion
        antetitulo="Recaudo"
        titulo="Pago de cuota de acuerdo"
        descripcion="Las facturas incluidas en un acuerdo de pago se pagan con sus cuotas, en orden y por su valor completo."
      />
      <section className="panel cuota-panel">
        {resultado ? (
          <ResultadoPago
            acuerdo={resultado.acuerdo}
            numeroCuota={resultado.numeroCuota}
            onOtro={() => {
              setResultado(null);
              setIntento((anterior) => anterior + 1);
            }}
          />
        ) : (
          <FormularioPagoCuota
            key={intento}
            onPagado={(acuerdo, numeroCuota) => setResultado({ acuerdo, numeroCuota })}
          />
        )}
      </section>
    </>
  );
}