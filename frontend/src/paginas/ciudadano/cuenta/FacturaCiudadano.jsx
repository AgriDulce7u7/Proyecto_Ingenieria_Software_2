import { useState } from "react";
import { useParams } from "react-router-dom";
import { descargarFacturaPortal } from "../../../api/portal";
import { BotonDescarga } from "../../../componentes/BotonDescarga";
import { Cargando } from "../../../componentes/Cargando";
import { MensajeError } from "../../../componentes/MensajeError";
import { Boton } from "../../../componentes/ui/Boton";
import { EnlaceVolver } from "../../../componentes/ui/EnlaceVolver";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import { useFacturaPortal } from "../../../hooks/usePortal";
import { useTituloPagina } from "../../../hooks/useTituloPagina";
import { formatearConsumo, formatearMoneda, partesPeriodo, presentarEstadoCobro } from "../../../presentacion/facturacion";
import { formatearFecha, formatearFechaHora } from "../../../presentacion/fechas";
import { motivoNoPagable } from "../../../presentacion/pagos";
import "../../gestion/facturacion/factura/detalle-factura.css";

/** Rejilla de datos con el mismo estilo de la factura impresa del back-office. */
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

/** Situación de la factura en palabras del ciudadano: pagada, vencida, en acuerdo de pago o anulada. */
function AvisoFactura({ factura }) {
  let aviso = null;
  if (factura.estadoCobro === "PAGADA") {
    aviso = { clase: "exito", icono: "check", titulo: "Factura pagada", texto: `Pago recibido el ${formatearFechaHora(factura.fechaPago)}.` };
  } else if (factura.estadoCobro === "ANULADA") {
    aviso = { clase: "neutro", icono: "cerrar", titulo: "Factura anulada", texto: "Esta factura ya no se cobra." };
  } else if (factura.enAcuerdoDePago) {
    aviso = {
      clase: "info",
      icono: "documento",
      titulo: "En acuerdo de pago",
      texto: "Se paga con las cuotas del acuerdo que firmó con EPQ, no por el portal.",
    };
  } else if (factura.estadoCobro === "VENCIDA") {
    aviso = {
      clase: "error",
      icono: "alerta",
      titulo: "Factura vencida",
      texto: `El plazo terminó el ${formatearFecha(factura.fechaVencimiento)}. Pague cuanto antes para evitar la suspensión del servicio.`,
    };
  }
  if (!aviso) return null;
  return (
    <div className={`factura-aviso factura-aviso-${aviso.clase}`} role="status">
      <Icono nombre={aviso.icono} tamano={18} />
      <div>
        <strong>{aviso.titulo}</strong>
        <p>{aviso.texto}</p>
      </div>
    </div>
  );
}

function ContenidoFactura({ factura }) {
  const [errorPdf, setErrorPdf] = useState(null);
  const cobro = presentarEstadoCobro(factura.estadoCobro);
  const periodo = partesPeriodo(factura.periodo);
  const { contrato } = factura;
  const pagable = !motivoNoPagable(factura);

  return (
    <>
      <div className="detalle-factura-encabezado">
        <div>
          <span className="antetitulo">Factura de {contrato.servicio.toLowerCase()}</span>
          <h2 className="cuenta-titulo-factura cifra">{factura.numeroFactura}</h2>
          <div className="detalle-factura-etiquetas">
            <Etiqueta tono={cobro.tono}>{cobro.etiqueta}</Etiqueta>
            <span>Periodo {periodo.texto}</span>
          </div>
        </div>
        <div className="detalle-factura-acciones">
          <BotonDescarga descargar={() => descargarFacturaPortal(factura.numeroFactura)} onError={setErrorPdf}>
            Descargar PDF
          </BotonDescarga>
          {pagable && (
            <Boton a={`/mi-cuenta/facturas/${factura.numeroFactura}/pagar`} icono="tarjeta">
              Pagar en línea
            </Boton>
          )}
        </div>
      </div>

      {errorPdf && (
        <div className="detalle-factura-aviso">
          <MensajeError error={errorPdf} />
        </div>
      )}

      <div className="detalle-factura-rejilla">
        <div className="detalle-factura-principal">
          <section className="cuenta-panel" aria-labelledby="informacion-titulo">
            <h3 id="informacion-titulo">Información de la factura</h3>
            <Datos
              filas={[
                { termino: "Periodo", valor: periodo.corto },
                { termino: "Fecha de expedición", valor: formatearFecha(factura.fechaGeneracion) },
                { termino: "Fecha de vencimiento", valor: formatearFecha(factura.fechaVencimiento) },
                { termino: "Contrato", valor: contrato.numeroContrato, cifra: true },
                { termino: "Dirección del servicio", valor: contrato.direccionServicio },
                { termino: "Medidor", valor: factura.numeroMedidor, cifra: true },
              ]}
            />
          </section>

          <section className="cuenta-panel" aria-labelledby="consumo-titulo">
            <h3 id="consumo-titulo">Consumo</h3>
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
                { termino: "Valor por m³", valor: formatearMoneda(factura.valorPorUnidad), cifra: true },
              ]}
            />
          </section>
        </div>

        <aside className="detalle-factura-lateral">
          <section className="factura-liquidacion" aria-labelledby="liquidacion-titulo">
            <h3 id="liquidacion-titulo">Liquidación</h3>
            <dl>
              <div>
                <dt>Valor del consumo</dt>
                <dd className="cifra">{formatearMoneda(factura.valorConsumo)}</dd>
              </div>
              <div>
                <dt>Cargo fijo</dt>
                <dd className="cifra">{formatearMoneda(factura.cargoFijo)}</dd>
              </div>
              <div className="factura-liquidacion-subtotal">
                <dt>Subtotal</dt>
                <dd className="cifra">{formatearMoneda(factura.subtotal)}</dd>
              </div>
            </dl>
            <div
              className={`factura-liquidacion-total${factura.estadoCobro === "ANULADA" ? " factura-liquidacion-anulada" : ""}`}
            >
              <span>{factura.estadoCobro === "PAGADA" ? "Total pagado" : "Total a pagar"}</span>
              <strong className="cifra">{formatearMoneda(factura.total)}</strong>
              <small>Vence el {formatearFecha(factura.fechaVencimiento)}</small>
            </div>
          </section>
          <AvisoFactura factura={factura} />
        </aside>
      </div>
    </>
  );
}

/** Detalle de una factura del ciudadano (SWR-25) con su PDF oficial (SWR-27) y el acceso al pago en línea. */
export function FacturaCiudadano() {
  const { numero } = useParams();
  useTituloPagina(`Factura ${numero}`);
  const factura = useFacturaPortal(numero);

  return (
    <>
      <div className="cuenta-volver">
        <EnlaceVolver a="/mi-cuenta/facturas" historial>
          Volver
        </EnlaceVolver>
      </div>
      {factura.isPending && <Cargando texto="Cargando la factura…" />}
      {factura.error?.noEncontrado && (
        <div className="aviso aviso-error" role="alert">
          <p>
            No encontramos la factura <strong className="cifra">{numero}</strong> entre sus facturas.
          </p>
        </div>
      )}
      {factura.error && !factura.error.noEncontrado && <MensajeError error={factura.error} />}
      {factura.data && <ContenidoFactura key={numero} factura={factura.data} />}
    </>
  );
}