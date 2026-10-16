import { formatearPorcentaje } from "../../../presentacion/supervisionPqr";

/**
 * Cumplimiento por grupo (tipo de solicitud o municipio). Cada barra es el porcentaje en plazo
 * sobre las evaluadas del grupo; el valor y las cantidades van siempre en texto junto a la barra.
 */
export function BarrasCumplimiento({ id, titulo, grupos }) {
  return (
    <section className="panel" aria-labelledby={`${id}-titulo`}>
      <h2 id={`${id}-titulo`}>{titulo}</h2>
      {grupos.length === 0 ? (
        <p className="cumplimiento-vacio">Sin solicitudes en el periodo.</p>
      ) : (
        <ul className="cumplimiento-barras">
          {grupos.map((grupo) => {
            const evaluadas = grupo.respondidasEnPlazo + grupo.incumplidas;
            const porcentaje = Number(grupo.porcentajeCumplimiento ?? 0);
            return (
              <li key={grupo.grupo}>
                <div className="cumplimiento-barra-cabecera">
                  <strong>{grupo.grupo}</strong>
                  <span className="cifra">{evaluadas > 0 ? formatearPorcentaje(porcentaje) : "Sin evaluar"}</span>
                </div>
                <div className="cumplimiento-barra" aria-hidden="true">
                  {evaluadas > 0 && <span style={{ width: `${Math.min(100, porcentaje)}%` }} />}
                </div>
                <small>
                  {grupo.radicadas} radicadas · {grupo.respondidasEnPlazo} en plazo
                  {grupo.incumplidas > 0 && ` · ${grupo.incumplidas} ${grupo.incumplidas === 1 ? "incumplida" : "incumplidas"}`}
                </small>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}