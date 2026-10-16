import {
  esConteo,
  formatearValor,
  presentarVariacion,
  tieneTendencia,
  unidadConteo,
  variacionComparable,
} from "../../../presentacion/dashboard";

/**
 * Variación frente al mes anterior. El color dice si es buena o mala según el sentido deseado del
 * indicador, y el texto lo repite ("Mejora", "Empeora") para no depender solo del color.
 */
export function Variacion({ indicador }) {
  const variacion = presentarVariacion(variacionComparable(indicador), indicador.sentidoDeseado);
  if (!variacion) return null;
  return (
    <span className={`dashboard-variacion dashboard-variacion-${variacion.tono}`}>
      <span className="dashboard-variacion-flecha" data-direccion={variacion.direccion} aria-hidden="true" />
      <span className="cifra">{variacion.texto}</span>
      <span className="dashboard-variacion-lectura">{variacion.lectura}</span>
    </span>
  );
}

/** Tendencia en miniatura, sin ejes: línea para montos y tasas, barras para conteos. Es decorativa. */
function Minigrafica({ indicador }) {
  const valores = indicador.tendencia.map((punto) => Number(punto.valor));
  const maximo = Math.max(...valores, 0);
  const minimo = esConteo(indicador.unidad) ? 0 : Math.min(...valores);
  const rango = maximo - minimo || 1;
  const total = valores.length;
  const y = (valor) => 28 - ((valor - minimo) / rango) * 26;

  if (esConteo(indicador.unidad)) {
    const ancho = 100 / total;
    return (
      <svg className="dashboard-mini" viewBox="0 0 100 30" preserveAspectRatio="none" aria-hidden="true">
        {valores.map((valor, indice) => (
          <rect
            key={indicador.tendencia[indice].periodo}
            x={indice * ancho + ancho * 0.18}
            width={ancho * 0.64}
            y={y(valor)}
            height={Math.max(30 - y(valor), 0.8)}
            data-ultimo={indice === total - 1 || undefined}
          />
        ))}
      </svg>
    );
  }
  const puntos = valores.map((valor, indice) => `${(indice / (total - 1)) * 100},${y(valor)}`).join(" ");
  return (
    <svg className="dashboard-mini" viewBox="0 0 100 30" preserveAspectRatio="none" aria-hidden="true">
      <polyline points={puntos} vectorEffect="non-scaling-stroke" />
    </svg>
  );
}

/** Contenido común de la tarjeta: nombre, valor, variación y la miniatura o la nota de foto actual. */
function ContenidoTarjeta({ indicador }) {
  const conTendencia = tieneTendencia(indicador);
  return (
    <>
      <span className="dashboard-tarjeta-nombre">{indicador.nombre}</span>
      <span className="dashboard-tarjeta-valor">
        <strong className="cifra">{formatearValor(indicador.valorActual, indicador.unidad)}</strong>
        {esConteo(indicador.unidad) && <small>{unidadConteo(indicador.valorActual, indicador.unidad)}</small>}
      </span>
      <Variacion indicador={indicador} />
      {conTendencia ? (
        <Minigrafica indicador={indicador} />
      ) : (
        <span className="dashboard-tarjeta-foto">Valor actual · sin serie mensual</span>
      )}
      <small className="dashboard-tarjeta-descripcion">{indicador.descripcion}</small>
    </>
  );
}

/**
 * Tarjeta de un indicador. Si tiene tendencia es un botón que la abre en la gráfica principal;
 * los indicadores que son solo una foto del momento se muestran sin interacción.
 */
export function TarjetaIndicador({ indicador, seleccionado, onSeleccionar }) {
  if (!tieneTendencia(indicador)) {
    return (
      <li className="dashboard-tarjeta" data-foto>
        <ContenidoTarjeta indicador={indicador} />
      </li>
    );
  }
  return (
    <li className="dashboard-tarjeta-item">
      <button
        type="button"
        className="dashboard-tarjeta"
        aria-pressed={seleccionado}
        onClick={() => onSeleccionar(indicador.codigo)}
      >
        <ContenidoTarjeta indicador={indicador} />
        <span className="dashboard-tarjeta-accion">{seleccionado ? "En la gráfica" : "Ver tendencia"}</span>
      </button>
    </li>
  );
}