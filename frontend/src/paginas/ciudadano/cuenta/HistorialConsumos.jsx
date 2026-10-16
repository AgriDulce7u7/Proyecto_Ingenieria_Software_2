import { useState } from "react";
import { Link } from "react-router-dom";
import { Cargando } from "../../../componentes/Cargando";
import { MensajeError } from "../../../componentes/MensajeError";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { useHistorialConsumos } from "../../../hooks/usePortal";
import {
  formatearConsumo,
  formatearMoneda,
  partesPeriodo,
  presentarEstadoCobro,
} from "../../../presentacion/facturacion";

/** "2026-09" → { corto: "sep", anio: "2026", nuevoAnio: mes de enero } para las etiquetas del eje. */
function etiquetaMes(periodo) {
  const partes = partesPeriodo(periodo);
  return { corto: partes.mes.slice(0, 3).toLowerCase(), anio: partes.anio, nuevoAnio: periodo.endsWith("-01") };
}

/** Escala del eje: el máximo redondeado hacia arriba a una cifra cómoda (10, 15, 20, 25, 30, 40, 50…). */
function maximoEje(valores) {
  const maximo = Math.max(...valores, 1);
  const magnitud = 10 ** Math.floor(Math.log10(maximo));
  const paso = [1, 1.5, 2, 2.5, 3, 4, 5, 6, 8, 10].find((factor) => factor * magnitud >= maximo) ?? 10;
  return paso * magnitud;
}

/** Texto de la ayuda emergente de un mes. */
function descripcionMes(punto) {
  const periodo = partesPeriodo(punto.periodo).corto;
  if (punto.consumo === null) return `${periodo}: sin factura`;
  const cobro = presentarEstadoCobro(punto.estadoFactura).etiqueta.toLowerCase();
  return `${periodo}: ${formatearConsumo(punto.consumo)}, ${formatearMoneda(punto.valorFacturado)}, ${cobro}`;
}

/**
 * Barras del consumo mensual (una sola serie, un solo color). Cada barra es un botón: al pasar el puntero
 * o enfocarla con el teclado se muestra el detalle del mes. Los meses sin factura quedan como un trazo
 * gris en la base para que la escala de tiempo no se pierda.
 */
function GraficaConsumos({ meses }) {
  const [activo, setActivo] = useState(null);
  const tope = maximoEje(meses.map((punto) => Number(punto.consumo ?? 0)));
  const punto = activo === null ? null : meses[activo];

  return (
    <div className="consumos-grafica">
      <div className="consumos-eje" aria-hidden="true">
        <span>{formatearConsumo(tope)}</span>
        <span>{formatearConsumo(tope / 2)}</span>
        <span>0</span>
      </div>
      <div className="consumos-area" onMouseLeave={() => setActivo(null)}>
        <div className="consumos-guias" aria-hidden="true">
          <i />
          <i />
          <i />
        </div>
        {punto && (
          <div
            className="consumos-ayuda"
            aria-hidden="true"
            style={{ left: `${((activo + 0.5) / meses.length) * 100}%` }}
            data-borde={activo < 3 ? "inicio" : activo > meses.length - 4 ? "fin" : undefined}
          >
            <strong>{partesPeriodo(punto.periodo).corto}</strong>
            {punto.consumo === null ? (
              <span>Sin factura en este mes</span>
            ) : (
              <>
                <span className="cifra">{formatearConsumo(punto.consumo)}</span>
                <span className="cifra">{formatearMoneda(punto.valorFacturado)}</span>
                <span>{presentarEstadoCobro(punto.estadoFactura).etiqueta}</span>
              </>
            )}
          </div>
        )}
        <ol className="consumos-barras">
          {meses.map((mes, indice) => {
            const etiqueta = etiquetaMes(mes.periodo);
            const sinDatos = mes.consumo === null;
            const altura = sinDatos ? 0 : (Number(mes.consumo) / tope) * 100;
            return (
              <li key={mes.periodo} data-activo={activo === indice}>
                <button
                  type="button"
                  className="consumos-barra"
                  aria-label={descripcionMes(mes)}
                  onMouseEnter={() => setActivo(indice)}
                  onFocus={() => setActivo(indice)}
                  onBlur={() => setActivo(null)}
                >
                  <span
                    className={sinDatos ? "consumos-barra-vacia" : undefined}
                    style={sinDatos ? undefined : { height: `${Math.max(altura, 1.5)}%` }}
                  />
                </button>
                <small className={indice % 3 === 0 ? "consumos-mes" : "consumos-mes consumos-mes-oculto"} aria-hidden="true">
                  {etiqueta.corto}
                  {(etiqueta.nuevoAnio || indice === 0) && <b>{etiqueta.anio}</b>}
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
function TablaConsumos({ meses }) {
  return (
    <div className="consumos-tabla-contenedor">
      <table className="consumos-tabla">
        <thead>
          <tr>
            <th scope="col">Periodo</th>
            <th scope="col">Consumo</th>
            <th scope="col">Valor facturado</th>
            <th scope="col">Estado</th>
          </tr>
        </thead>
        <tbody>
          {[...meses].reverse().map((mes) => {
            const cobro = mes.estadoFactura ? presentarEstadoCobro(mes.estadoFactura) : null;
            return (
              <tr key={mes.periodo}>
                <th scope="row">
                  {mes.numeroFactura ? (
                    <Link to={`/mi-cuenta/facturas/${mes.numeroFactura}`} className="cuenta-enlace">
                      {partesPeriodo(mes.periodo).corto}
                    </Link>
                  ) : (
                    partesPeriodo(mes.periodo).corto
                  )}
                </th>
                <td className="cifra">{mes.consumo === null ? "—" : formatearConsumo(mes.consumo)}</td>
                <td className="cifra">{mes.valorFacturado === null ? "—" : formatearMoneda(mes.valorFacturado)}</td>
                <td>{cobro ? <Etiqueta tono={cobro.tono}>{cobro.etiqueta}</Etiqueta> : "Sin factura"}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

/** Promedio, máximo, mínimo y total facturado del periodo consultado. */
function ResumenConsumos({ resumen }) {
  const datos = [
    { termino: "Promedio mensual", valor: formatearConsumo(resumen.consumoPromedio) },
    { termino: "Mes de mayor consumo", valor: formatearConsumo(resumen.consumoMaximo) },
    { termino: "Mes de menor consumo", valor: formatearConsumo(resumen.consumoMinimo) },
    { termino: "Total facturado", valor: formatearMoneda(resumen.totalFacturado) },
  ];
  return (
    <dl className="consumos-resumen">
      {datos.map((dato) => (
        <div key={dato.termino}>
          <dt>{dato.termino}</dt>
          <dd className="cifra">{dato.valor}</dd>
        </div>
      ))}
    </dl>
  );
}

/** Historial de consumos de los últimos 24 meses en gráfica y en tabla (SWR-26, RF-07). */
export function HistorialConsumos({ numeroContrato }) {
  const historial = useHistorialConsumos(numeroContrato);
  const [vista, setVista] = useState("grafica");
  const datos = historial.data;

  return (
    <section className="cuenta-panel" aria-labelledby="consumos-titulo">
      <div className="cuenta-panel-titulo">
        <div>
          <h2 id="consumos-titulo">Historial de consumo</h2>
          {datos && (
            <p>
              {partesPeriodo(datos.desde).corto} a {partesPeriodo(datos.hasta).corto} · metros cúbicos (m³)
            </p>
          )}
        </div>
        {datos?.resumen.mesesConDatos > 0 && (
          <div className="cuenta-filtros" role="group" aria-label="Vista del historial">
            <button
              type="button"
              className="cuenta-filtro"
              aria-pressed={vista === "grafica"}
              onClick={() => setVista("grafica")}
            >
              Gráfica
            </button>
            <button type="button" className="cuenta-filtro" aria-pressed={vista === "tabla"} onClick={() => setVista("tabla")}>
              Tabla
            </button>
          </div>
        )}
      </div>

      {historial.isPending && <Cargando texto="Cargando el historial…" />}
      <MensajeError error={historial.error} />
      {datos && datos.resumen.mesesConDatos === 0 && (
        <p className="cuenta-vacio">Aún no hay consumos facturados para este servicio.</p>
      )}
      {datos && datos.resumen.mesesConDatos > 0 && (
        <>
          <ResumenConsumos resumen={datos.resumen} />
          {vista === "grafica" ? <GraficaConsumos meses={datos.meses} /> : <TablaConsumos meses={datos.meses} />}
        </>
      )}
    </section>
  );
}