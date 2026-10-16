import { Link } from "react-router-dom";
import { Cargando } from "../../../componentes/Cargando";
import { MensajeError } from "../../../componentes/MensajeError";
import { Boton } from "../../../componentes/ui/Boton";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import { useContratosPortal } from "../../../hooks/usePortal";
import { useTituloPagina } from "../../../hooks/useTituloPagina";
import { formatearMoneda, partesPeriodo } from "../../../presentacion/facturacion";
import { presentarEstadoContratoPortal } from "../../../presentacion/portal";

/** Saldo pendiente de todos los contratos: lo primero que el ciudadano quiere saber. */
function SaldoTotal({ contratos }) {
  const saldo = contratos.reduce((suma, contrato) => suma + Number(contrato.saldoPendiente), 0);
  const vencidas = contratos.reduce((suma, contrato) => suma + contrato.facturasVencidas, 0);

  return (
    <section className="cuenta-saldo" aria-labelledby="saldo-titulo">
      <div>
        <span id="saldo-titulo" className="antetitulo">
          Saldo pendiente
        </span>
        <strong className="cuenta-saldo-valor cifra">{formatearMoneda(saldo)}</strong>
        <p>
          {saldo === 0
            ? "Está al día con todos sus servicios."
            : vencidas > 0
              ? `${vencidas} ${vencidas === 1 ? "factura vencida" : "facturas vencidas"}. Pague para evitar la suspensión del servicio.`
              : "Tiene facturas por pagar dentro del plazo."}
        </p>
      </div>
      {saldo > 0 && (
        <Boton a="/mi-cuenta/facturas?cobro=pendientes" icono="tarjeta" className="cuenta-saldo-boton">
          Pagar facturas
        </Boton>
      )}
    </section>
  );
}

/** Tarjeta de un contrato con su estado, saldo y último periodo facturado. */
function TarjetaContrato({ contrato }) {
  const estado = presentarEstadoContratoPortal(contrato.estado);
  const saldo = Number(contrato.saldoPendiente);

  return (
    <li className="cuenta-contrato">
      <div className="cuenta-contrato-cabecera">
        <span className="cuenta-contrato-icono" aria-hidden="true">
          <Icono nombre="gota" tamano={20} />
        </span>
        <div>
          <h3>{contrato.servicio}</h3>
          <small className="cifra">{contrato.numeroContrato}</small>
        </div>
        <Etiqueta tono={estado.tono}>{estado.etiqueta}</Etiqueta>
      </div>
      <p className="cuenta-contrato-direccion">
        {contrato.direccionServicio}
        {contrato.municipio && `, ${contrato.municipio}`}
      </p>
      <dl className="cuenta-contrato-datos">
        <div>
          <dt>Saldo pendiente</dt>
          <dd className={`cifra${contrato.facturasVencidas > 0 ? " cuenta-dato-vencido" : ""}`}>
            {saldo > 0 ? formatearMoneda(saldo) : "Al día"}
          </dd>
        </div>
        <div>
          <dt>Último periodo</dt>
          <dd>{contrato.ultimoPeriodoFacturado ? partesPeriodo(contrato.ultimoPeriodoFacturado).corto : "Sin facturas"}</dd>
        </div>
      </dl>
      {contrato.facturasVencidas > 0 && (
        <p className="cuenta-contrato-alerta">
          <Icono nombre="alerta" tamano={15} />
          {contrato.facturasVencidas} {contrato.facturasVencidas === 1 ? "factura vencida" : "facturas vencidas"}
        </p>
      )}
      <Link to={`/mi-cuenta/contratos/${contrato.numeroContrato}`} className="cuenta-contrato-enlace">
        Ver factura y consumo
        <Icono nombre="chevron" tamano={16} />
      </Link>
    </li>
  );
}

/** Resumen de la cuenta (CU-05 paso 2): saldo total y los contratos vigentes del ciudadano. */
export function Resumen() {
  useTituloPagina("Mi cuenta");
  const contratos = useContratosPortal();

  return (
    <>
      {contratos.isPending && <Cargando texto="Cargando sus servicios…" />}
      <MensajeError error={contratos.error} />
      {contratos.data && (
        <>
          <SaldoTotal contratos={contratos.data} />
          <section className="cuenta-seccion" aria-labelledby="servicios-titulo">
            <h2 id="servicios-titulo">Mis servicios</h2>
            {contratos.data.length === 0 ? (
              <p className="cuenta-vacio">
                No tiene contratos de servicio vigentes. Si cree que es un error, acuda a una oficina de atención.
              </p>
            ) : (
              <ul className="cuenta-contratos">
                {contratos.data.map((contrato) => (
                  <TarjetaContrato key={contrato.numeroContrato} contrato={contrato} />
                ))}
              </ul>
            )}
          </section>
        </>
      )}
    </>
  );
}