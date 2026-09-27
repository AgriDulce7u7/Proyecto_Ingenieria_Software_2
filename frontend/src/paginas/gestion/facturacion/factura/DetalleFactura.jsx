import { useParams } from "react-router-dom";
import { Cargando } from "../../../../componentes/Cargando";
import { MensajeError } from "../../../../componentes/MensajeError";
import { Boton } from "../../../../componentes/ui/Boton";
import { EnlaceVolver } from "../../../../componentes/ui/EnlaceVolver";
import { Etiqueta } from "../../../../componentes/ui/Etiqueta";
import { Icono } from "../../../../componentes/ui/Icono";
import { useFactura, useSincronizarFactura } from "../../../../hooks/useFacturacion";
import { useTituloPagina } from "../../../../hooks/useTituloPagina";
import {
  formatearConsumo,
  formatearMoneda,
  partesPeriodo,
  presentarEstadoFactura,
  puedeSincronizarse,
} from "../../../../presentacion/facturacion";
import { formatearFecha } from "../../../../presentacion/fechas";
import { enmascararDocumento } from "../../../../presentacion/texto";
import { AvisoErp } from "./AvisoErp";
import { BitacoraErp } from "./BitacoraErp";
import "./detalle-factura.css";

/** Rejilla de datos (término y valor) con bordes, como en una factura impresa. */
function Datos({ filas }) {
  return (
    <dl className="factura-datos">
      {filas
        .filter((fila) => fila.valor)
        .map((fila) => (
          <div key={fila.termino}>
            <dt>{fila.termino}</dt>
            <dd className={fila.cifra ? "cifra" : undefined}>{fila.valor}</dd>
          </div>
        ))}
    </dl>
  );
}

/** Lecturas del medidor, consumo del periodo y tarifa aplicada. */
function Consumo({ factura }) {
  return (
    <section className="panel" aria-labelledby="consumo-titulo">
      <h2 id="consumo-titulo">Consumo</h2>
      <div className="factura-lecturas">
        <div>
          <span>Lectura anterior</span>
          <strong className="cifra">{formatearConsumo(factura.lecturaAnterior)}</strong>
        </div>
        <Icono nombre="flecha" tamano={18} />
        <div>
          <span>Lectura actual</span>
          <strong className="cifra">{formatearConsumo(factura.lecturaActual)}</strong>
        </div>
        <div className="factura-consumo">
          <span>Consumo del periodo</span>
          <strong className="cifra">{formatearConsumo(factura.consumo)}</strong>
        </div>
      </div>
      <Datos
        filas={[
          { termino: "Tarifa", valor: factura.tarifa },
          { termino: "Valor por unidad", valor: `${formatearMoneda(factura.valorPorUnidad)} / m³`, cifra: true },
        ]}
      />
    </section>
  );
}

/** Liquidación (SWR-03): consumo × valor por unidad + cargo fijo. */
function Liquidacion({ factura }) {
  const filas = [
    { termino: "Valor del consumo", valor: factura.valorConsumo },
    { termino: "Cargo fijo", valor: factura.cargoFijo },
  ];

  return (
    <section className="factura-liquidacion" aria-labelledby="liquidacion-titulo">
      <h2 id="liquidacion-titulo">Liquidación</h2>
      <dl>
        {filas
        .filter((fila) => fila.valor)
        .map((fila) => (
          <div key={fila.termino}>
            <dt>{fila.termino}</dt>
            <dd className="cifra">{formatearMoneda(fila.valor)}</dd>
          </div>
        ))}
        <div className="factura-liquidacion-subtotal">
          <dt>Subtotal</dt>
          <dd className="cifra">{formatearMoneda(factura.subtotal)}</dd>
        </div>
      </dl>
      <div className="factura-liquidacion-total">
        <span>Total a pagar</span>
        <strong className="cifra">{formatearMoneda(factura.total)}</strong>
        <small>Vence el {formatearFecha(factura.fechaVencimiento)}</small>
      </div>
    </section>
  );
}

function ContenidoFactura({ factura }) {
  const estado = presentarEstadoFactura(factura.estado);
  const periodo = partesPeriodo(factura.periodo);
  const sincronizacion = useSincronizarFactura();
  const { contrato } = factura;

  return (
    <>
      <div className="detalle-factura-encabezado">
        <div>
          <span className="antetitulo">Detalle de factura</span>
          <h1 className="cifra">{factura.numeroFactura}</h1>
          <div className="detalle-factura-etiquetas">
            <Etiqueta tono={estado.tono}>{estado.etiqueta}</Etiqueta>
            <span>Periodo {periodo.texto}</span>
          </div>
        </div>
        {puedeSincronizarse(factura.estado) && (
          <Boton
            icono="sincronizar"
            onClick={() => sincronizacion.mutate(factura.numeroFactura)}
            disabled={sincronizacion.isPending}
          >
            {sincronizacion.isPending ? "Enviando al ERP…" : "Reintentar sincronización"}
          </Boton>
        )}
      </div>

      {sincronizacion.data && (
        <div
          className={`aviso ${sincronizacion.data.exitosa ? "aviso-exito" : "aviso-error"} detalle-factura-aviso`}
          role="status"
        >
          <p>
            {sincronizacion.data.exitosa
              ? "La factura se sincronizó con el ERP."
              : `El ERP no aceptó la factura tras ${sincronizacion.data.intentos} intentos: ${sincronizacion.data.mensaje}`}
          </p>
        </div>
      )}
      <MensajeError error={sincronizacion.error} />

      <div className="detalle-factura-rejilla">
        <div className="detalle-factura-principal">
          <section className="panel" aria-labelledby="informacion-titulo">
            <h2 id="informacion-titulo">Información de la factura</h2>
            <Datos
              filas={[
                { termino: "Número de factura", valor: factura.numeroFactura, cifra: true },
                { termino: "Periodo", valor: periodo.corto },
                { termino: "Fecha de generación", valor: formatearFecha(factura.fechaGeneracion) },
                { termino: "Fecha de vencimiento", valor: formatearFecha(factura.fechaVencimiento) },
              ]}
            />
          </section>

          <section className="panel" aria-labelledby="contrato-titulo">
            <h2 id="contrato-titulo">Información del contrato</h2>
            <Datos
              filas={[
                { termino: "Número de contrato", valor: contrato.numeroContrato, cifra: true },
                { termino: "Cliente", valor: contrato.cliente },
                {
                  termino: "Documento",
                  valor: `${contrato.tipoDocumentoCliente} ${enmascararDocumento(contrato.numeroDocumentoCliente)}`,
                },
                { termino: "Servicio", valor: contrato.servicio },
                { termino: "Número de medidor", valor: factura.numeroMedidor, cifra: true },
                { termino: "Dirección del servicio", valor: contrato.direccionServicio },
              ]}
            />
          </section>

          <Consumo factura={factura} />
          <BitacoraErp numeroFactura={factura.numeroFactura} />
        </div>

        <aside className="detalle-factura-lateral">
          <Liquidacion factura={factura} />
          <AvisoErp numeroFactura={factura.numeroFactura} estado={factura.estado} />
        </aside>
      </div>
    </>
  );
}

/** Detalle de una factura (RF-05, SWR-03, SWR-04). */
export function DetalleFactura() {
  const { numero } = useParams();
  useTituloPagina(`Factura ${numero}`);
  const factura = useFactura(numero);

  return (
    <>
      <div className="detalle-factura-volver">
        <EnlaceVolver a="/gestion/facturacion/facturas" historial>
          Volver a facturas
        </EnlaceVolver>
      </div>

      {factura.isPending && <Cargando texto="Cargando la factura…" />}
      {factura.error?.noEncontrado && (
        <div className="aviso aviso-error" role="alert">
          <p>
            No existe la factura <strong className="cifra">{numero}</strong>.
          </p>
        </div>
      )}
      {factura.error && !factura.error.noEncontrado && <MensajeError error={factura.error} />}
      {factura.data && <ContenidoFactura key={numero} factura={factura.data} />}
    </>
  );
}