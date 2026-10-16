import { formatearFechaHora } from "../../../../presentacion/fechas";
import { presentarEstadoContrato } from "../../../../presentacion/usuarios";

/** Cambios de estado del contrato con motivo, responsable y fecha (SWR-20), del más reciente al más antiguo. */
export function HistorialContrato({ historial }) {
  const movimientos = [...historial].reverse();

  return (
    <section className="panel" aria-labelledby="historial-contrato-titulo">
      <div className="panel-titulo">
        <h2 id="historial-contrato-titulo">Historial</h2>
        <span className="panel-nota">
          {movimientos.length} {movimientos.length === 1 ? "movimiento" : "movimientos"}
        </span>
      </div>
      <ol className="detalle-historial">
        {movimientos.map((movimiento, indice) => (
          <li
            key={`${movimiento.fecha}-${movimiento.estadoNuevo}`}
            className={indice === 0 ? "detalle-historial-actual" : undefined}
          >
            <i aria-hidden="true" />
            <div>
              <strong>
                {movimiento.estadoAnterior
                  ? `${presentarEstadoContrato(movimiento.estadoAnterior).etiqueta} → ${presentarEstadoContrato(movimiento.estadoNuevo).etiqueta}`
                  : presentarEstadoContrato(movimiento.estadoNuevo).etiqueta}
              </strong>
              <p>{movimiento.motivo}</p>
              <small>
                {movimiento.usuario}, {formatearFechaHora(movimiento.fecha)}
              </small>
            </div>
          </li>
        ))}
      </ol>
    </section>
  );
}