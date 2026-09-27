import { useState } from "react";
import { useParams } from "react-router-dom";
import { Cargando } from "../../../componentes/Cargando";
import { MensajeError } from "../../../componentes/MensajeError";
import { Paginacion } from "../../../componentes/Paginacion";
import { Boton } from "../../../componentes/ui/Boton";
import { EnlaceVolver } from "../../../componentes/ui/EnlaceVolver";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import { useFacturasLote, useLote, useSincronizarFactura, useSincronizarLote } from "../../../hooks/useFacturacion";
import { useTituloPagina } from "../../../hooks/useTituloPagina";
import {
  formatearDuracion,
  formatearMoneda,
  formatearNumero,
  partesPeriodo,
  presentarEstadoLote,
} from "../../../presentacion/facturacion";
import { formatearFechaHora } from "../../../presentacion/fechas";
import { paginar } from "../../../presentacion/paginacion";
import { TablaFacturas } from "./TablaFacturas";
import "./detalle-lote.css";

const TAMANO_PAGINA = 10;

/** Porcentaje con un decimal: 5 de 6 → "83,3 %". */
const porcentaje = (parte, total) =>
  total ? `${((parte / total) * 100).toLocaleString("es-CO", { maximumFractionDigits: 1 })} %` : "0 %";

/** Tiempos, total facturado y conteos del lote. */
function ResumenLote({ lote }) {
  const total = lote.totalFacturasGeneradas;
  const tiempos = [
    { termino: "Inicio", valor: formatearFechaHora(lote.fechaInicioProceso) },
    { termino: "Finalización", valor: lote.fechaFinProceso ? formatearFechaHora(lote.fechaFinProceso) : "En curso" },
    {
      termino: "Duración",
      valor: lote.duracionSegundos == null ? "En curso" : formatearDuracion(lote.duracionSegundos * 1000),
    },
  ];
  const conteos = [
    { termino: "Facturas generadas", valor: total, detalle: "Total del periodo" },
    {
      termino: "Sincronizadas",
      valor: lote.facturasSincronizadas,
      detalle: `${porcentaje(lote.facturasSincronizadas, total)} del lote`,
      tono: "exito",
    },
    { termino: "Pendientes", valor: lote.facturasPendientesSincronizacion, detalle: "Sin enviar al ERP", tono: "plazo" },
    {
      termino: "Con error",
      valor: lote.facturasConErrorSincronizacion,
      detalle: "Requieren reintento",
      tono: "vencido",
    },
  ];

  return (
    <section className="panel lote-resumen" aria-labelledby="resumen-titulo">
      <h2 id="resumen-titulo">Resumen del proceso</h2>
      <dl className="lote-resumen-tiempos">
        {tiempos.map((dato) => (
          <div key={dato.termino}>
            <dt>{dato.termino}</dt>
            <dd>{dato.valor}</dd>
          </div>
        ))}
        <div className="lote-resumen-total">
          <dt>Valor total facturado</dt>
          <dd className="cifra">{formatearMoneda(lote.valorTotalFacturado)}</dd>
        </div>
      </dl>
      <dl className="lote-resumen-conteos">
        {conteos.map((dato) => (
          <div key={dato.termino}>
            <dt>{dato.termino}</dt>
            <dd className={dato.tono && dato.valor > 0 ? `lote-conteo-${dato.tono}` : undefined}>
              {formatearNumero(dato.valor)}
            </dd>
            <dd className="lote-conteo-detalle">{dato.detalle}</dd>
          </div>
        ))}
      </dl>
    </section>
  );
}

/** Resultado del reintento del lote completo. */
function ResultadoSincronizacionLote({ resultado }) {
  if (resultado.facturasProcesadas === 0) {
    return (
      <div className="aviso aviso-exito detalle-lote-aviso" role="status">
        <p>No había facturas pendientes ni con error por sincronizar.</p>
      </div>
    );
  }
  const { facturasProcesadas, exitosas, fallidas, facturasConError } = resultado;
  const todoBien = fallidas === 0;
  const enviadas = facturasProcesadas === 1 ? "Se envió 1 factura" : `Se enviaron ${facturasProcesadas} facturas`;
  const sincronizadas = exitosas === 1 ? "1 sincronizada" : `${exitosas} sincronizadas`;
  return (
    <div className={`aviso ${todoBien ? "aviso-exito" : "aviso-error"} detalle-lote-aviso`} role="status">
      <p>
        {enviadas} al ERP: {sincronizadas}
        {todoBien ? "." : ` y ${fallidas} con error (${facturasConError.join(", ")}).`}
      </p>
    </div>
  );
}

function ContenidoLote({ lote }) {
  const periodo = partesPeriodo(lote.periodo);
  const estado = presentarEstadoLote(lote.estado);
  const facturas = useFacturasLote(lote.periodo);
  const sincronizacionLote = useSincronizarLote(lote.periodo);
  const sincronizacionFactura = useSincronizarFactura();
  const [pagina, setPagina] = useState(1);

  const porSincronizar = lote.facturasPendientesSincronizacion + lote.facturasConErrorSincronizacion;
  const lista = facturas.data ?? [];
  const conError = lista.filter((factura) => factura.estado === "ERROR_SINCRONIZACION");
  const { visibles, paginaActual, totalPaginas, inicio } = paginar(lista, pagina, TAMANO_PAGINA);

  return (
    <>
      <div className="detalle-lote-encabezado">
        <div>
          <span className="antetitulo cifra">Lote {lote.periodo}</span>
          <h1>Lote de {periodo.texto}</h1>
          <Etiqueta tono={estado.tono}>{estado.etiqueta}</Etiqueta>
        </div>
        <Boton
          variante="secundario"
          icono="sincronizar"
          onClick={() => sincronizacionLote.mutate()}
          disabled={porSincronizar === 0 || sincronizacionLote.isPending}
          title={porSincronizar === 0 ? "Todas las facturas del lote ya están sincronizadas" : undefined}
        >
          {sincronizacionLote.isPending ? "Sincronizando…" : "Reintentar sincronización del lote"}
        </Boton>
      </div>

      {sincronizacionLote.data && <ResultadoSincronizacionLote resultado={sincronizacionLote.data} />}
      <MensajeError error={sincronizacionLote.error} />
      {sincronizacionFactura.data && (
        <div
          className={`aviso ${sincronizacionFactura.data.exitosa ? "aviso-exito" : "aviso-error"} detalle-lote-aviso`}
          role="status"
        >
          <p>
            {sincronizacionFactura.data.exitosa
              ? `La factura ${sincronizacionFactura.data.numeroFactura} se sincronizó con el ERP.`
              : `El ERP no aceptó la factura ${sincronizacionFactura.data.numeroFactura}: ${sincronizacionFactura.data.mensaje}`}
          </p>
        </div>
      )}

      <ResumenLote lote={lote} />

      <section className="detalle-lote-seccion" aria-labelledby="facturas-lote-titulo">
        <div className="detalle-lote-seccion-titulo">
          <div>
            <h2 id="facturas-lote-titulo">Facturas del lote</h2>
            <p>Facturas generadas para el periodo de {periodo.texto}.</p>
          </div>
          <Etiqueta>{formatearNumero(lista.length)} registros</Etiqueta>
        </div>
        <div className="facturas-panel">
          {facturas.isPending && (
            <div className="facturas-estado">
              <Cargando texto="Cargando facturas…" />
            </div>
          )}
          {facturas.isError && (
            <div className="facturas-estado">
              <MensajeError error={facturas.error} />
            </div>
          )}
          {facturas.isSuccess && lista.length === 0 && (
            <div className="facturas-estado">
              <p>Este lote no generó facturas.</p>
            </div>
          )}
          {visibles.length > 0 && (
            <TablaFacturas
              facturas={visibles}
              onReintentar={(numero) => sincronizacionFactura.mutate(numero)}
              reintentando={sincronizacionFactura.isPending ? sincronizacionFactura.variables : null}
            />
          )}
          <Paginacion
            total={lista.length}
            pagina={paginaActual}
            totalPaginas={totalPaginas}
            inicio={inicio}
            cantidadVisible={visibles.length}
            onCambiar={setPagina}
          />
        </div>
      </section>

      {conError.length > 0 && (
        <section className="panel detalle-lote-errores" aria-labelledby="errores-titulo">
          <div className="panel-titulo">
            <div>
              <h2 id="errores-titulo">Facturas rechazadas por el ERP</h2>
              <p>Siguen generadas y pueden reenviarse individualmente o con el reintento del lote.</p>
            </div>
            <Etiqueta tono="peligro">{conError.length}</Etiqueta>
          </div>
          <ul>
            {conError.map((factura) => (
              <li key={factura.numeroFactura}>
                <Icono nombre="alerta" tamano={18} />
                <span className="cifra">{factura.numeroFactura}</span>
                <small>
                  {factura.numeroContrato}, {factura.cliente}
                </small>
              </li>
            ))}
          </ul>
        </section>
      )}
    </>
  );
}

/** Detalle de un lote de facturación (RF-05, CU-03, SWR-02, SWR-04). */
export function DetalleLote() {
  const { periodo } = useParams();
  useTituloPagina(`Lote ${periodo}`);
  const lote = useLote(periodo);

  return (
    <>
      <div className="detalle-lote-volver">
        <EnlaceVolver a="/gestion/facturacion/lotes" historial>
          Volver a lotes de facturación
        </EnlaceVolver>
      </div>

      {lote.isPending && <Cargando texto="Cargando el lote…" />}
      {lote.error?.noEncontrado && (
        <div className="aviso aviso-error" role="alert">
          <p>
            No existe un lote para el periodo <strong className="cifra">{periodo}</strong>.
          </p>
        </div>
      )}
      {lote.error && !lote.error.noEncontrado && <MensajeError error={lote.error} />}
      {lote.data && <ContenidoLote key={periodo} lote={lote.data} />}
    </>
  );
}