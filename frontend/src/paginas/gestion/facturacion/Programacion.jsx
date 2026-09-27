import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { Boton } from "../../../componentes/ui/Boton";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { useContratos, useGenerarLote, useProgramacion } from "../../../hooks/useFacturacion";
import { formatearNumero, partesPeriodo, presentarEstadoLote } from "../../../presentacion/facturacion";
import { formatearFecha } from "../../../presentacion/fechas";
import { ResultadoLote } from "./ResultadoLote";
import "./programacion.css";

/** Etapas que ejecuta el backend al generar un lote (ver GeneracionFacturacionServiceImpl). */
const ETAPAS = [
  "Validación de contratos y lecturas",
  "Liquidación mensual",
  "Generación de facturas",
  "Sincronización con ERP",
];

/**
 * Programación de la facturación mensual (SWR-01, SWR-02) y generación manual del lote (RF-05, CU-03).
 */
export function Programacion() {
  const programacion = useProgramacion();
  const contratosActivos = useContratos("ACTIVO");
  const generacion = useGenerarLote();

  return (
    <>
      <EncabezadoGestion
        antetitulo="Ciclo mensual"
        titulo="Facturación"
        descripcion="Consulte la programación y genere el lote del periodo."
      />

      {programacion.isPending && <Cargando texto="Cargando la programación…" />}
      <MensajeError error={programacion.error} />

      {programacion.data && (
        <ContenidoProgramacion
          programacion={programacion.data}
          contratosActivos={contratosActivos.data?.length}
          generacion={generacion}
        />
      )}
    </>
  );
}

function ContenidoProgramacion({ programacion, contratosActivos, generacion }) {
  const periodo = partesPeriodo(programacion.periodoPendienteDeFacturar);
  const estadoLote = presentarEstadoLote(programacion.estadoLotePeriodoPendiente);
  const loteCompletado = programacion.estadoLotePeriodoPendiente === "COMPLETADO";

  return (
    <>
      <section className="programacion-destacado" aria-label="Próxima ejecución">
        <div>
          <span className="antetitulo">
            {programacion.ejecucionAutomaticaHabilitada
              ? "Próxima ejecución programada"
              : "Ejecución automática deshabilitada"}
          </span>
          <h2>{formatearFecha(programacion.proximaEjecucion)}</h2>
          <p>Periodo de facturación: {periodo.texto}</p>
        </div>
        <div className="programacion-limite">
          <span>Tiempo máximo</span>
          <strong>{programacion.tiempoMaximoLoteMinutos} min</strong>
          <small>límite por lote (SWR-02)</small>
        </div>
      </section>

      <div className="programacion-rejilla">
        <section className="panel programacion-periodo" aria-labelledby="periodo-titulo">
          <div className="panel-titulo">
            <h2 id="periodo-titulo">Periodo pendiente</h2>
            <Etiqueta tono={estadoLote.tono}>{estadoLote.etiqueta}</Etiqueta>
          </div>
          <p className="programacion-periodo-nombre">
            {periodo.mes} <strong>{periodo.anio}</strong>
          </p>
          <dl className="programacion-datos">
            <div>
              <dt>Contratos activos</dt>
              <dd>{contratosActivos === undefined ? "…" : formatearNumero(contratosActivos)}</dd>
            </div>
            <div>
              <dt>Vencimiento de facturas</dt>
              <dd>{programacion.diasParaVencimiento} días</dd>
            </div>
            <div>
              <dt>Ejecución automática</dt>
              <dd>{programacion.ejecucionAutomaticaHabilitada ? "Habilitada" : "Deshabilitada"}</dd>
            </div>
          </dl>
          <div className="programacion-accion">
            <p>
              {loteCompletado
                ? "El lote de este periodo ya se generó. Volver a ejecutarlo no duplica facturas."
                : "Puede generar el lote ahora o esperar la ejecución programada."}
            </p>
            <Boton icono="mas" onClick={() => generacion.mutate()} disabled={generacion.isPending}>
              {generacion.isPending ? "Generando lote…" : "Generar lote manual"}
            </Boton>
          </div>
        </section>

        <section className="panel programacion-flujo" aria-labelledby="flujo-titulo">
          <h2 id="flujo-titulo">Flujo de procesamiento</h2>
          <ol>
            {ETAPAS.map((etapa, indice) => (
              <li key={etapa}>
                <span aria-hidden="true">{indice + 1}</span>
                {etapa}
              </li>
            ))}
          </ol>
        </section>
      </div>

      <MensajeError error={generacion.error} />
      {generacion.data && (
        <ResultadoLote resultado={generacion.data} tiempoMaximoMinutos={programacion.tiempoMaximoLoteMinutos} />
      )}
    </>
  );
}