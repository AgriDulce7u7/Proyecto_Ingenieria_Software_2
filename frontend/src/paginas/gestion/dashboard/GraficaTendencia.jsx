import { useState } from "react";
import { partesPeriodo } from "../../../presentacion/facturacion";
import {
  esConteo,
  etiquetaMes,
  formatearEje,
  formatearValorCompleto,
  maximoEje,
} from "../../../presentacion/dashboard";

/** Texto accesible de un mes: "Septiembre 2026: $ 12.450.300,00". */
const descripcionPunto = (punto, unidad, enCurso) =>
  `${partesPeriodo(punto.periodo).corto}${enCurso ? " (mes en curso)" : ""}: ${formatearValorCompleto(punto.valor, unidad)}`;

/**
 * Tendencia de 12 meses de un indicador (SWR-35), una sola serie y un solo color. Los montos y tasas van
 * en línea; los conteos por mes, en barras. Cada mes es una columna enfocable: al pasar el puntero o
 * llegar con el teclado se marca el mes y se muestra su valor. El mes en curso se distingue porque aún
 * no ha cerrado.
 */
export function GraficaTendencia({ indicador, mesReferencia }) {
  const [activo, setActivo] = useState(null);
  const { tendencia, unidad } = indicador;
  const barras = esConteo(unidad);
  const tope = maximoEje(
    tendencia.map((punto) => punto.valor),
    unidad,
  );
  const total = tendencia.length;
  const x = (indice) => ((indice + 0.5) / total) * 100;
  const y = (valor) => 100 - (Math.max(0, Number(valor)) / tope) * 100;
  const puntos = tendencia.map((punto, indice) => `${x(indice)},${y(punto.valor)}`).join(" ");
  const marcado = activo ?? total - 1;
  const punto = activo === null ? null : tendencia[activo];

  return (
    <div className="tendencia-grafica" data-forma={barras ? "barras" : "linea"}>
      <div className="tendencia-eje" aria-hidden="true">
        <span>{formatearEje(tope, unidad)}</span>
        <span>{formatearEje(tope / 2, unidad)}</span>
        <span>0</span>
      </div>
      <div className="tendencia-area" onMouseLeave={() => setActivo(null)}>
        <div className="tendencia-guias" aria-hidden="true">
          <i />
          <i />
          <i />
        </div>

        {!barras && (
          <>
            <svg className="tendencia-linea" viewBox="0 0 100 100" preserveAspectRatio="none" aria-hidden="true">
              <polyline points={puntos} vectorEffect="non-scaling-stroke" />
            </svg>
            <span
              className="tendencia-marca"
              aria-hidden="true"
              style={{ left: `${x(marcado)}%`, top: `${y(tendencia[marcado].valor)}%` }}
            />
          </>
        )}

        {punto && (
          <>
            <span className="tendencia-guia-vertical" aria-hidden="true" style={{ left: `${x(activo)}%` }} />
            <div
              className="tendencia-ayuda"
              aria-hidden="true"
              style={{ left: `${x(activo)}%` }}
              data-borde={activo < 2 ? "inicio" : activo > total - 3 ? "fin" : undefined}
            >
              <strong>{partesPeriodo(punto.periodo).corto}</strong>
              <span className="cifra">{formatearValorCompleto(punto.valor, unidad)}</span>
              {punto.periodo === mesReferencia && <small>Mes en curso, hasta hoy</small>}
            </div>
          </>
        )}

        <ol className="tendencia-columnas">
          {tendencia.map((mes, indice) => {
            const enCurso = mes.periodo === mesReferencia;
            return (
              <li key={mes.periodo} data-activo={activo === indice || undefined} data-en-curso={enCurso || undefined}>
                <button
                  type="button"
                  className="tendencia-columna"
                  aria-label={descripcionPunto(mes, unidad, enCurso)}
                  onMouseEnter={() => setActivo(indice)}
                  onFocus={() => setActivo(indice)}
                  onBlur={() => setActivo(null)}
                >
                  {barras && (
                    <span
                      className="tendencia-barra"
                      style={{ height: `${Number(mes.valor) > 0 ? Math.max((Number(mes.valor) / tope) * 100, 1.5) : 0}%` }}
                    />
                  )}
                </button>
                <small
                  className={indice % 2 === 0 || indice === total - 1 ? "tendencia-mes" : "tendencia-mes tendencia-mes-oculto"}
                  aria-hidden="true"
                >
                  {etiquetaMes(mes.periodo, indice)}
                </small>
              </li>
            );
          })}
        </ol>
      </div>
    </div>
  );
}

/** Los mismos datos en tabla, del mes más reciente al más antiguo (vista accesible de la gráfica). */
export function TablaTendencia({ indicador, mesReferencia }) {
  return (
    <div className="tendencia-tabla-contenedor">
      <table className="tendencia-tabla">
        <thead>
          <tr>
            <th scope="col">Mes</th>
            <th scope="col">{indicador.nombre}</th>
          </tr>
        </thead>
        <tbody>
          {[...indicador.tendencia].reverse().map((punto) => (
            <tr key={punto.periodo}>
              <th scope="row">
                {partesPeriodo(punto.periodo).corto}
                {punto.periodo === mesReferencia && <small> · en curso</small>}
              </th>
              <td className="cifra">{formatearValorCompleto(punto.valor, indicador.unidad)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}