import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { Boton } from "../../../componentes/ui/Boton";
import { useAlertasEquipo } from "../../../hooks/usePqr";
import { useFiltrosUrl } from "../../../hooks/useFiltrosUrl";
import { NIVELES, cargaPorGestor, contarPorNivel } from "../../../presentacion/supervisionPqr";
import { useSesion } from "../../../sesion/useSesion";
import { CargaEquipo } from "./CargaEquipo";
import { TablaAlertasEquipo } from "./TablaAlertasEquipo";
import "../pqr/bandeja.css";
import "./supervision.css";

/** Franja con la cantidad de PQR del equipo en cada nivel de plazo. */
function MetricasNiveles({ conteo }) {
  return (
    <dl className="bandeja-metricas">
      {NIVELES.map((nivel) => (
        <div key={nivel.codigo}>
          <dt>{nivel.plural}</dt>
          <dd className={nivel.clase ? `bandeja-metrica-${nivel.clase}` : undefined}>{conteo[nivel.codigo]}</dd>
          <dd className="bandeja-metrica-detalle">{nivel.detalle}</dd>
        </div>
      ))}
    </dl>
  );
}

/**
 * Supervisión de la Oficina de PQR (StR-02c, SWR-07, RN-04): PQR pendientes del equipo ordenadas por
 * el tiempo que les queda, carga de cada gestor y filtros por nivel y gestor guardados en la URL.
 * El jefe ve a su equipo; el administrador, toda la oficina.
 */
export function Supervision() {
  const { usuario } = useSesion();
  const { filtros, hayFiltros, cambiarFiltro, limpiar } = useFiltrosUrl(["nivel", "gestor"]);
  const alertas = useAlertasEquipo();

  const todas = alertas.data ?? [];
  const carga = cargaPorGestor(todas);
  const filtradas = todas.filter(
    (alerta) =>
      (!filtros.nivel || alerta.nivel === filtros.nivel) &&
      (!filtros.gestor || String(alerta.gestorId ?? "sin-gestor") === filtros.gestor),
  );

  return (
    <>
      <EncabezadoGestion
        antetitulo={usuario.rol === "JEFE_PQR" ? "Mi equipo" : "Oficina de PQR"}
        titulo="Supervisión"
        descripcion="Solicitudes pendientes del equipo según el tiempo que les queda para responder."
      />

      {alertas.isPending && <Cargando texto="Cargando el estado del equipo…" />}
      <MensajeError error={alertas.error} />

      {alertas.isSuccess && (
        <>
          <MetricasNiveles conteo={contarPorNivel(todas)} />

          <div className="supervision-rejilla">
            <section className="bandeja-panel" aria-label="Solicitudes pendientes del equipo">
              <div className="bandeja-filtros" role="search">
                <select
                  aria-label="Filtrar por nivel"
                  value={filtros.nivel}
                  onChange={(evento) => cambiarFiltro("nivel", evento.target.value)}
                >
                  <option value="">Todos los niveles</option>
                  {NIVELES.map((nivel) => (
                    <option key={nivel.codigo} value={nivel.codigo}>
                      {nivel.etiqueta}
                    </option>
                  ))}
                </select>
                <select
                  aria-label="Filtrar por gestor"
                  value={filtros.gestor}
                  onChange={(evento) => cambiarFiltro("gestor", evento.target.value)}
                >
                  <option value="">Todos los gestores</option>
                  {carga.map((fila) => (
                    <option key={fila.clave} value={fila.clave}>
                      {fila.gestor}
                    </option>
                  ))}
                </select>
                <Boton variante="discreto" onClick={limpiar} disabled={!hayFiltros}>
                  Limpiar
                </Boton>
              </div>

              <div className="bandeja-resumen" aria-live="polite">
                <strong>
                  {filtradas.length} {filtradas.length === 1 ? "solicitud pendiente" : "solicitudes pendientes"}
                </strong>
                <small>{alertas.isFetching ? "Actualizando…" : "La más urgente primero"}</small>
              </div>

              {filtradas.length === 0 ? (
                <div className="bandeja-estado bandeja-vacia">
                  <p>
                    {hayFiltros
                      ? "Ninguna solicitud coincide con los filtros."
                      : "El equipo no tiene solicitudes pendientes de respuesta."}
                  </p>
                  {hayFiltros && (
                    <Boton variante="secundario" onClick={limpiar}>
                      Limpiar filtros
                    </Boton>
                  )}
                </div>
              ) : (
                <TablaAlertasEquipo alertas={filtradas} />
              )}
            </section>

            <CargaEquipo
              carga={carga}
              gestorElegido={filtros.gestor}
              onElegir={(clave) => cambiarFiltro("gestor", filtros.gestor === clave ? "" : clave)}
            />
          </div>
        </>
      )}
    </>
  );
}