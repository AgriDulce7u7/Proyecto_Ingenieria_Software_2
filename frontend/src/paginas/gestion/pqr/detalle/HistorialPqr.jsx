import { Cargando } from "../../../../componentes/Cargando";
import { MensajeError } from "../../../../componentes/MensajeError";
import { useHistorialPqr } from "../../../../hooks/usePqr";
import { formatearFechaHora } from "../../../../presentacion/fechas";

/** Trazabilidad de la PQR (SWR-09), del movimiento más reciente al más antiguo. */
export function HistorialPqr({ radicado }) {
  const historial = useHistorialPqr(radicado);
  const movimientos = [...(historial.data ?? [])].reverse();

  return (
    <section className="panel" aria-labelledby="historial-titulo">
      <div className="panel-titulo">
        <h2 id="historial-titulo">Historial</h2>
        {historial.data && (
          <span className="panel-nota">
            {movimientos.length} {movimientos.length === 1 ? "movimiento" : "movimientos"}
          </span>
        )}
      </div>
      {historial.isPending && <Cargando texto="Cargando historial…" />}
      <MensajeError error={historial.error} />
      <ol className="detalle-historial">
        {movimientos.map((movimiento, indice) => (
          <li key={movimiento.id} className={indice === 0 ? "detalle-historial-actual" : undefined}>
            <i aria-hidden="true" />
            <div>
              <strong>{movimiento.estado}</strong>
              <p>{movimiento.comentario}</p>
              <small>
                {movimiento.usuario}, {formatearFechaHora(movimiento.fechaCambio)}
              </small>
            </div>
          </li>
        ))}
      </ol>
    </section>
  );
}