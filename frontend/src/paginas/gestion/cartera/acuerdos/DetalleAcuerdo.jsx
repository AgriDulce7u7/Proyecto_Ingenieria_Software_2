import { useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import { Cargando } from "../../../../componentes/Cargando";
import { MensajeError } from "../../../../componentes/MensajeError";
import { Boton } from "../../../../componentes/ui/Boton";
import { Dialogo } from "../../../../componentes/ui/Dialogo";
import { EnlaceVolver } from "../../../../componentes/ui/EnlaceVolver";
import { Etiqueta } from "../../../../componentes/ui/Etiqueta";
import { useAcuerdo } from "../../../../hooks/useCartera";
import { useTituloPagina } from "../../../../hooks/useTituloPagina";
import { porcentajePagado, presentarEstadoAcuerdo, siguienteCuota } from "../../../../presentacion/cartera";
import { formatearMoneda } from "../../../../presentacion/facturacion";
import { formatearFecha, formatearFechaHora } from "../../../../presentacion/fechas";
import { useSesion } from "../../../../sesion/useSesion";
import { FormularioPagoCuota } from "./FormularioPagoCuota";
import { TablaCuotas } from "./TablaCuotas";
import "../../pqr/detalle/detalle.css";
import "../../pagos/pagos.css";
import "../cartera.css";

/** Saldo del acuerdo con la proporción pagada, en el bloque oscuro del sistema visual. */
function SaldoAcuerdo({ acuerdo }) {
  const porcentaje = porcentajePagado(acuerdo);
  return (
    <section className="acuerdo-saldo" aria-labelledby="saldo-acuerdo-titulo">
      <span id="saldo-acuerdo-titulo" className="antetitulo">
        Saldo pendiente
      </span>
      <strong className="acuerdo-saldo-valor cifra">{formatearMoneda(acuerdo.saldoPendiente)}</strong>
      <div className="acuerdo-saldo-barra" role="img" aria-label={`${Math.round(porcentaje)} % pagado`}>
        <span style={{ width: `${porcentaje}%` }} />
      </div>
      <p>
        Pagado <span className="cifra">{formatearMoneda(acuerdo.valorPagado)}</span> de{" "}
        <span className="cifra">{formatearMoneda(acuerdo.totalDeuda)}</span>
      </p>
    </section>
  );
}

/** Aviso según el estado: incumplido (cobro coercitivo reactivado) o cumplido (facturas pagadas). */
function AvisoEstado({ acuerdo }) {
  if (acuerdo.estado === "INCUMPLIDO") {
    return (
      <div className="aviso aviso-error detalle-resultado" role="status">
        <p>
          <strong>Acuerdo incumplido</strong> el {formatearFechaHora(acuerdo.fechaCambioEstado)}: una cuota venció sin
          pago. Las facturas volvieron a mora y se reactivó el cobro coercitivo (RN-07).
        </p>
      </div>
    );
  }
  if (acuerdo.estado === "CUMPLIDO") {
    return (
      <div className="aviso aviso-exito detalle-resultado" role="status">
        <p>
          <strong>Acuerdo cumplido</strong> el {formatearFechaHora(acuerdo.fechaCambioEstado)}. Las facturas incluidas
          quedaron pagadas y el proceso de cobro se cerró.
        </p>
      </div>
    );
  }
  return null;
}

function ContenidoAcuerdo({ acuerdo, creado }) {
  const puedePagar = useSesion().puede("CARTERA_PAGAR_CUOTA");
  const [pagando, setPagando] = useState(false);
  const [resultado, setResultado] = useState(null);
  const estado = presentarEstadoAcuerdo(acuerdo.estado);
  const proxima = acuerdo.estado === "VIGENTE" ? siguienteCuota(acuerdo) : null;

  const alPagar = (actualizado, numeroCuota) => {
    setPagando(false);
    setResultado(
      actualizado.estado === "CUMPLIDO"
        ? `Se registró el pago de la cuota ${numeroCuota}, la última del acuerdo.`
        : `Se registró el pago de la cuota ${numeroCuota}.`,
    );
  };

  return (
    <>
      <div className="detalle-encabezado">
        <div>
          <span className="antetitulo">Acuerdo de pago</span>
          <h1 className="cifra">{acuerdo.numeroAcuerdo}</h1>
          <div className="detalle-etiquetas">
            <Etiqueta tono={estado.tono}>{estado.etiqueta}</Etiqueta>
            <span>
              {acuerdo.numeroCuotas} cuotas · desde el {formatearFecha(acuerdo.fechaAcuerdo)}
            </span>
          </div>
        </div>
        {puedePagar && proxima && (
          <div className="detalle-acciones">
            <Boton icono="tarjeta" onClick={() => setPagando(true)}>
              Registrar pago de la cuota {proxima.numeroCuota}
            </Boton>
          </div>
        )}
      </div>

      {creado && !resultado && (
        <div className="aviso aviso-exito detalle-resultado" role="status">
          <p>
            Se registró el acuerdo. El cobro coercitivo quedó suspendido y se envió el plan de cuotas al correo del
            usuario.
          </p>
        </div>
      )}
      {resultado && (
        <div className="aviso aviso-exito detalle-resultado" role="status">
          <p>{resultado}</p>
        </div>
      )}
      <AvisoEstado acuerdo={acuerdo} />

      <div className="detalle-rejilla acuerdo-rejilla">
        <div className="detalle-principal">
          <section className="panel" aria-labelledby="cuotas-titulo">
            <div className="panel-titulo">
              <h2 id="cuotas-titulo">Plan de cuotas</h2>
              {proxima && (
                <span className="panel-nota">
                  Próxima: cuota {proxima.numeroCuota}, {formatearFecha(proxima.fechaVencimiento)}
                </span>
              )}
            </div>
            <TablaCuotas cuotas={acuerdo.cuotas} siguiente={proxima?.numeroCuota} />
          </section>

          <section className="panel" aria-labelledby="facturas-acuerdo-titulo">
            <div className="panel-titulo">
              <h2 id="facturas-acuerdo-titulo">Facturas incluidas</h2>
              <span className="panel-nota">{acuerdo.facturasIncluidas.length}</span>
            </div>
            <ul className="acuerdo-facturas">
              {acuerdo.facturasIncluidas.map((numero) => (
                <li key={numero}>
                  <Link to={`/gestion/facturacion/facturas/${numero}`} className="pagos-enlace cifra">
                    {numero}
                  </Link>
                </li>
              ))}
            </ul>
          </section>
        </div>

        <aside className="detalle-lateral">
          <SaldoAcuerdo acuerdo={acuerdo} />
          <section className="panel" aria-labelledby="datos-acuerdo-titulo">
            <h2 id="datos-acuerdo-titulo">Datos del acuerdo</h2>
            <dl className="detalle-datos">
              <div>
                <dt>Usuario</dt>
                <dd>{acuerdo.usuario}</dd>
              </div>
              <div>
                <dt>Documento</dt>
                <dd className="cifra">{acuerdo.documentoUsuario}</dd>
              </div>
              <div>
                <dt>Contrato</dt>
                <dd>
                  <Link to={`/gestion/contratos/${acuerdo.numeroContrato}`} className="pagos-enlace cifra">
                    {acuerdo.numeroContrato}
                  </Link>{" "}
                  · {acuerdo.servicio}
                </dd>
              </div>
              <div>
                <dt>Registrado</dt>
                <dd>
                  {formatearFechaHora(acuerdo.fechaAcuerdo)} por {acuerdo.registradoPor}
                </dd>
              </div>
              {acuerdo.observacion && (
                <div>
                  <dt>Observación</dt>
                  <dd className="acuerdo-observacion">{acuerdo.observacion}</dd>
                </div>
              )}
            </dl>
          </section>
        </aside>
      </div>

      <Dialogo
        abierto={pagando}
        onCerrar={() => setPagando(false)}
        antetitulo={acuerdo.numeroAcuerdo}
        titulo="Registrar pago de cuota"
      >
        {pagando && proxima && (
          <FormularioPagoCuota
            numeroAcuerdo={acuerdo.numeroAcuerdo}
            cuota={proxima}
            onPagado={alPagar}
            onCancelar={() => setPagando(false)}
          />
        )}
      </Dialogo>
    </>
  );
}

/** Detalle de un acuerdo de pago (CU-08, RF-12, RN-07): plan de cuotas, saldo, facturas y pago de cuotas. */
export function DetalleAcuerdo() {
  const { numero } = useParams();
  const { state } = useLocation();
  useTituloPagina(`Acuerdo ${numero}`);
  const acuerdo = useAcuerdo(numero);

  return (
    <>
      <div className="detalle-volver">
        <EnlaceVolver a="/gestion/cartera/acuerdos" historial>
          Volver
        </EnlaceVolver>
      </div>
      {acuerdo.isPending && <Cargando texto="Cargando el acuerdo…" />}
      {acuerdo.error?.noEncontrado && (
        <div className="aviso aviso-error" role="alert">
          <p>
            No existe el acuerdo <strong className="cifra">{numero}</strong>.
          </p>
        </div>
      )}
      {acuerdo.error && !acuerdo.error.noEncontrado && <MensajeError error={acuerdo.error} />}
      {acuerdo.data && <ContenidoAcuerdo key={numero} acuerdo={acuerdo.data} creado={Boolean(state?.creado)} />}
    </>
  );
}