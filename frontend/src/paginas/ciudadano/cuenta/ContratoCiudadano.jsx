import { Link, useParams } from "react-router-dom";
import { Cargando } from "../../../componentes/Cargando";
import { MensajeError } from "../../../componentes/MensajeError";
import { Boton } from "../../../componentes/ui/Boton";
import { EnlaceVolver } from "../../../componentes/ui/EnlaceVolver";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import { useContratosPortal, useFacturaActual, useFacturasPortal } from "../../../hooks/usePortal";
import { useTituloPagina } from "../../../hooks/useTituloPagina";
import { formatearConsumo, formatearMoneda, partesPeriodo, presentarEstadoCobro } from "../../../presentacion/facturacion";
import { formatearFecha } from "../../../presentacion/fechas";
import { motivoNoPagable } from "../../../presentacion/pagos";
import { presentarEstadoContratoPortal } from "../../../presentacion/portal";
import { HistorialConsumos } from "./HistorialConsumos";
import { ListaFacturas } from "./ListaFacturas";

/** Factura del periodo actual ya generada: total, vencimiento y acceso directo al pago (SWR-25). */
function FacturaDisponible({ factura }) {
  const cobro = presentarEstadoCobro(factura.estadoCobro);
  const pagable = !motivoNoPagable(factura);
  return (
    <div className="cuenta-factura-actual">
      <div className="cuenta-factura-actual-datos">
        <span className="antetitulo">Factura de {partesPeriodo(factura.periodo).texto}</span>
        <strong className="cuenta-factura-actual-total cifra">{formatearMoneda(factura.total)}</strong>
        <p>
          <Etiqueta tono={cobro.tono}>{cobro.etiqueta}</Etiqueta>{" "}
          {factura.estadoCobro === "PAGADA"
            ? `Pagada el ${formatearFecha(factura.fechaPago)}`
            : `Vence el ${formatearFecha(factura.fechaVencimiento)}`}{" "}
          · Consumo {formatearConsumo(factura.consumo)}
        </p>
        {factura.enAcuerdoDePago && (
          <p className="cuenta-nota">Esta factura está en un acuerdo de pago: se paga con las cuotas del acuerdo.</p>
        )}
      </div>
      <div className="cuenta-factura-actual-acciones">
        <Boton a={`/mi-cuenta/facturas/${factura.numeroFactura}`} variante="secundario" icono="documento">
          Ver factura
        </Boton>
        {pagable && (
          <Boton a={`/mi-cuenta/facturas/${factura.numeroFactura}/pagar`} icono="tarjeta">
            Pagar en línea
          </Boton>
        )}
      </div>
    </div>
  );
}

/** Factura del periodo actual, o la fecha estimada en que estará lista y la última emitida (SWR-28, CU-05 4a). */
function FacturaActual({ numeroContrato }) {
  const actual = useFacturaActual(numeroContrato);
  const datos = actual.data;

  return (
    <section className="cuenta-panel" aria-labelledby="factura-actual-titulo">
      <div className="cuenta-panel-titulo">
        <h2 id="factura-actual-titulo">Factura del mes</h2>
      </div>
      {actual.isPending && <Cargando texto="Consultando la factura…" />}
      <MensajeError error={actual.error} />
      {datos?.disponible && <FacturaDisponible factura={datos.factura} />}
      {datos && !datos.disponible && (
        <>
          <div className="cuenta-factura-pendiente" role="status">
            <Icono nombre="reloj" tamano={22} />
            <div>
              <strong>La factura de {partesPeriodo(datos.periodoActual).texto} aún no está lista</strong>
              <p>
                Estará disponible aproximadamente el <strong>{formatearFecha(datos.fechaEstimadaDisponibilidad)}</strong>.
              </p>
            </div>
          </div>
          {datos.ultimaFacturaEmitida && (
            <p className="cuenta-nota">
              Última factura emitida:{" "}
              <Link to={`/mi-cuenta/facturas/${datos.ultimaFacturaEmitida.numeroFactura}`} className="cuenta-enlace">
                {partesPeriodo(datos.ultimaFacturaEmitida.periodo).corto}
              </Link>{" "}
              por <span className="cifra">{formatearMoneda(datos.ultimaFacturaEmitida.total)}</span> (
              {presentarEstadoCobro(datos.ultimaFacturaEmitida.estadoCobro).etiqueta.toLowerCase()}).
            </p>
          )}
        </>
      )}
    </section>
  );
}

/** Facturas del contrato, de la más reciente a la más antigua. */
function FacturasDelContrato({ numeroContrato }) {
  const facturas = useFacturasPortal(numeroContrato);
  return (
    <section className="cuenta-panel" aria-labelledby="facturas-contrato-titulo">
      <div className="cuenta-panel-titulo">
        <h2 id="facturas-contrato-titulo">Facturas</h2>
      </div>
      {facturas.isPending && <Cargando texto="Cargando las facturas…" />}
      <MensajeError error={facturas.error} />
      {facturas.data?.length === 0 && <p className="cuenta-vacio">Este servicio aún no tiene facturas.</p>}
      {facturas.data?.length > 0 && <ListaFacturas facturas={facturas.data} mostrarServicio={false} />}
    </section>
  );
}

/** Un servicio del ciudadano: factura del mes, historial de consumo y facturas (CU-05, SWR-25, SWR-26, SWR-28). */
export function ContratoCiudadano() {
  const { numero } = useParams();
  const contratos = useContratosPortal();
  const contrato = contratos.data?.find((elemento) => elemento.numeroContrato === numero);
  useTituloPagina(contrato ? `${contrato.servicio} ${numero}` : `Contrato ${numero}`);

  if (contratos.isPending) return <Cargando texto="Cargando el servicio…" />;
  if (contratos.error) return <MensajeError error={contratos.error} />;
  if (!contrato) {
    return (
      <div className="aviso aviso-error" role="alert">
        <p>
          No encontramos el contrato <strong className="cifra">{numero}</strong> entre sus servicios vigentes.{" "}
          <Link to="/mi-cuenta">Volver al resumen</Link>.
        </p>
      </div>
    );
  }

  const estado = presentarEstadoContratoPortal(contrato.estado);
  return (
    <>
      <div className="cuenta-volver">
        <EnlaceVolver a="/mi-cuenta" historial>
          Mis servicios
        </EnlaceVolver>
      </div>
      <div className="cuenta-servicio">
        <span className="cuenta-contrato-icono" aria-hidden="true">
          <Icono nombre="gota" tamano={22} />
        </span>
        <div>
          <h2>
            {contrato.servicio} <span className="cifra">{contrato.numeroContrato}</span>
          </h2>
          <p>
            {contrato.direccionServicio}
            {contrato.municipio && `, ${contrato.municipio}`}
          </p>
        </div>
        <Etiqueta tono={estado.tono}>{estado.etiqueta}</Etiqueta>
      </div>

      <div className="cuenta-columnas">
        <FacturaActual numeroContrato={numero} />
        <HistorialConsumos numeroContrato={numero} />
        <FacturasDelContrato numeroContrato={numero} />
      </div>
    </>
  );
}