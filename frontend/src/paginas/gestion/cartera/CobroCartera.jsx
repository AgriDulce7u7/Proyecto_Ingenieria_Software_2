import { Link } from "react-router-dom";
import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { Boton } from "../../../componentes/ui/Boton";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import { useMonitoreoCartera, useProcesosCobro } from "../../../hooks/useCartera";
import { useFiltrosUrl } from "../../../hooks/useFiltrosUrl";
import { ESTADOS_PROCESO_CODIGOS, presentarEstadoProceso } from "../../../presentacion/cartera";
import { formatearFechaCorta, formatearFechaHora } from "../../../presentacion/fechas";
import { buscarEn } from "../../../presentacion/texto";
import "../pqr/bandeja.css";
import "./cartera.css";

/**
 * Ejecución manual del monitoreo de cartera, que normalmente corre cada madrugada: vence facturas, marca
 * incumplidos los acuerdos con cuotas vencidas (SWR-31) y actualiza los candidatos a suspensión (SWR-32).
 */
function Monitoreo() {
  const monitoreo = useMonitoreoCartera();
  const resultado = monitoreo.data;

  return (
    <section className="panel cobro-monitoreo" aria-labelledby="monitoreo-titulo">
      <div className="cobro-monitoreo-cabecera">
        <span className="cobro-monitoreo-icono" aria-hidden="true">
          <Icono nombre="sincronizar" tamano={22} />
        </span>
        <div>
          <h2 id="monitoreo-titulo">Monitoreo de cartera</h2>
          <p>
            Corre solo cada día a las 2:30 a. m. Ejecútelo ahora para ver de inmediato los incumplimientos y
            candidatos a suspensión.
          </p>
        </div>
        <Boton icono="sincronizar" onClick={() => monitoreo.mutate()} disabled={monitoreo.isPending}>
          {monitoreo.isPending ? "Ejecutando…" : "Ejecutar ahora"}
        </Boton>
      </div>
      <MensajeError error={monitoreo.error} />
      {resultado && (
        <div className="cobro-monitoreo-resultado" aria-live="polite">
          <p className="panel-nota">Ejecutado el {formatearFechaHora(resultado.fechaEjecucion)}</p>
          <ol>
            {resultado.pasos.map((paso) => {
              const fallo = paso.detalle?.startsWith("ERROR");
              return (
                <li key={paso.nombre} data-fallo={fallo || undefined}>
                  <span className="cobro-paso-cifra cifra">{paso.registrosAfectados}</span>
                  <div>
                    <strong>{paso.nombre}</strong>
                    {paso.detalle && <p>{paso.detalle}</p>}
                  </div>
                </li>
              );
            })}
          </ol>
        </div>
      )}
    </section>
  );
}

/** Procesos de cobro coercitivo por contrato (RN-07): activos, suspendidos por un acuerdo o cerrados. */
function ProcesosCobro() {
  const { filtros, hayFiltros, cambiarFiltro, limpiar } = useFiltrosUrl(["q", "estado"]);
  const procesos = useProcesosCobro(filtros.estado || undefined);
  const resultado = buscarEn(procesos.data ?? [], filtros.q, ["numeroContrato", "usuario"]);

  return (
    <section className="bandeja-panel" aria-label="Procesos de cobro coercitivo">
      <div className="bandeja-filtros" role="search">
        <label className="bandeja-buscador">
          <Icono nombre="buscar" tamano={18} />
          <span className="solo-lectores">Buscar</span>
          <input
            type="search"
            placeholder="Buscar contrato o usuario..."
            value={filtros.q}
            onChange={(evento) => cambiarFiltro("q", evento.target.value)}
          />
        </label>
        <select
          aria-label="Filtrar por estado"
          value={filtros.estado}
          onChange={(evento) => cambiarFiltro("estado", evento.target.value)}
        >
          <option value="">Todos los estados</option>
          {ESTADOS_PROCESO_CODIGOS.map((codigo) => (
            <option key={codigo} value={codigo}>
              {presentarEstadoProceso(codigo).etiqueta}
            </option>
          ))}
        </select>
        <Boton variante="discreto" onClick={limpiar} disabled={!hayFiltros}>
          Limpiar
        </Boton>
      </div>

      <div className="bandeja-resumen" aria-live="polite">
        <strong>
          {resultado.length} {resultado.length === 1 ? "proceso de cobro" : "procesos de cobro"}
        </strong>
        <small>{procesos.isFetching ? "Actualizando…" : "Del más reciente al más antiguo"}</small>
      </div>

      {procesos.isPending && (
        <div className="bandeja-estado">
          <Cargando texto="Cargando procesos…" />
        </div>
      )}
      {procesos.isError && (
        <div className="bandeja-estado">
          <MensajeError error={procesos.error} />
        </div>
      )}
      {procesos.isSuccess && resultado.length === 0 && (
        <div className="bandeja-estado">
          <p>{hayFiltros ? "Ningún proceso coincide con los filtros." : "No hay procesos de cobro."}</p>
        </div>
      )}

      {resultado.length > 0 && (
        <div className="bandeja-tabla-contenedor">
          <table className="bandeja-tabla cobro-tabla">
            <thead>
              <tr>
                <th scope="col">Contrato</th>
                <th scope="col">Usuario</th>
                <th scope="col">Motivo</th>
                <th scope="col">Inicio</th>
                <th scope="col">Estado</th>
              </tr>
            </thead>
            <tbody>
              {resultado.map((proceso) => {
                const estado = presentarEstadoProceso(proceso.estado);
                return (
                  <tr key={proceso.numeroContrato}>
                    <td>
                      <span className="bandeja-celda-doble">
                        <Link to={`/gestion/contratos/${proceso.numeroContrato}`} className="bandeja-radicado cifra">
                          {proceso.numeroContrato}
                        </Link>
                        <small>{proceso.servicio}</small>
                      </span>
                    </td>
                    <td>{proceso.usuario}</td>
                    <td>
                      <span className="bandeja-celda-doble">
                        <span className="cobro-motivo">{proceso.motivo}</span>
                        <small>Actualizado {formatearFechaHora(proceso.fechaActualizacion)}</small>
                      </span>
                    </td>
                    <td>{formatearFechaCorta(proceso.fechaInicio)}</td>
                    <td>
                      <Etiqueta tono={estado.tono}>{estado.etiqueta}</Etiqueta>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

/** Cobro coercitivo (RN-07) y monitoreo de cartera (SWR-31, SWR-32). */
export function CobroCartera() {
  return (
    <>
      <EncabezadoGestion
        antetitulo="Cobro persuasivo"
        titulo="Cobro y monitoreo"
        descripcion="El cobro coercitivo se suspende mientras hay un acuerdo vigente y se reactiva si el acuerdo se incumple."
      />
      <Monitoreo />
      <ProcesosCobro />
    </>
  );
}