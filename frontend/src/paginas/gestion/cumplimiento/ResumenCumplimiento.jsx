import { formatearFecha } from "../../../presentacion/fechas";
import { formatearPorcentaje } from "../../../presentacion/supervisionPqr";

const formatearCantidad = (valor) => Number(valor).toLocaleString("es-CO");

/**
 * Cifra principal del periodo: porcentaje de PQR que cumplieron el plazo sobre las ya evaluadas
 * (respondidas o vencidas) y el desglose de cómo terminó cada una.
 */
export function ResumenCumplimiento({ datos }) {
  const { totales } = datos;
  const evaluadas = totales.respondidasEnPlazo + totales.respondidasFueraDePlazo + totales.vencidasSinRespuesta;
  const enTramite = Math.max(0, totales.radicadas - evaluadas);
  const porcentaje = Number(totales.porcentajeCumplimiento ?? 0);

  const desglose = [
    { etiqueta: "Respondidas en plazo", valor: totales.respondidasEnPlazo, tono: "exito" },
    { etiqueta: "Respondidas fuera de plazo", valor: totales.respondidasFueraDePlazo, tono: "plazo" },
    { etiqueta: "Vencidas sin respuesta", valor: totales.vencidasSinRespuesta, tono: "vencido" },
    { etiqueta: "Aún dentro del plazo", valor: enTramite, detalle: "Sin evaluar todavía" },
  ];

  return (
    <section className="cumplimiento-resumen" aria-labelledby="resumen-titulo">
      <div className="cumplimiento-principal">
        <h2 id="resumen-titulo" className="antetitulo">
          Cumplimiento del periodo
        </h2>
        <p className="cumplimiento-cifra cifra">{evaluadas > 0 ? formatearPorcentaje(porcentaje) : "—"}</p>
        <div
          className="cumplimiento-medidor"
          role="meter"
          aria-label="Cumplimiento del plazo"
          aria-valuemin={0}
          aria-valuemax={100}
          aria-valuenow={porcentaje}
        >
          <span style={{ width: `${Math.min(100, porcentaje)}%` }} />
        </div>
        <p className="cumplimiento-contexto">
          {evaluadas > 0
            ? `${formatearCantidad(totales.respondidasEnPlazo)} de ${formatearCantidad(evaluadas)} solicitudes evaluadas cumplieron el plazo.`
            : "Aún no hay solicitudes respondidas ni vencidas en este periodo."}{" "}
          {formatearCantidad(totales.radicadas)} radicadas entre el {formatearFecha(datos.desde)} y el{" "}
          {formatearFecha(datos.hasta)}.
        </p>
      </div>

      <dl className="cumplimiento-desglose">
        {desglose.map((fila) => (
          <div key={fila.etiqueta}>
            <dt>
              <i className={`cumplimiento-punto${fila.tono ? ` cumplimiento-punto-${fila.tono}` : ""}`} aria-hidden="true" />
              {fila.etiqueta}
            </dt>
            <dd className="cifra">{formatearCantidad(fila.valor)}</dd>
          </div>
        ))}
      </dl>
    </section>
  );
}