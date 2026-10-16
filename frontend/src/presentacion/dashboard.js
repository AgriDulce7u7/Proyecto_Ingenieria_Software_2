import { formatearConsumo, formatearMoneda, partesPeriodo } from "./facturacion";
import { formatearRecaudo } from "./pagos";
import { formatearPorcentaje } from "./supervisionPqr";

/** Presentación de F-08: formato de los valores según la unidad, variación y escala de las gráficas. */

const FORMATO_ENTERO = new Intl.NumberFormat("es-CO", { maximumFractionDigits: 0 });
const FORMATO_DECIMAL = new Intl.NumberFormat("es-CO", { maximumFractionDigits: 1 });
const FORMATO_COMPACTO = new Intl.NumberFormat("es-CO", { notation: "compact", maximumFractionDigits: 1 });

/** Unidades que se grafican como línea (montos y tasas); las demás son conteos por mes y van en barras. */
const UNIDADES_CONTINUAS = ["COP", "%", "días", "m³"];

export const esConteo = (unidad) => !UNIDADES_CONTINUAS.includes(unidad);

/** Unidad de los conteos en singular, para "1 contrato" en vez de "1 contratos". */
const SINGULAR = { contratos: "contrato", usuarios: "usuario", facturas: "factura", acuerdos: "acuerdo" };

/** Unidad del conteo según la cantidad: "1 contrato", "12 contratos", "2 PQR". */
export const unidadConteo = (valor, unidad) => (Number(valor) === 1 ? (SINGULAR[unidad] ?? unidad) : unidad);

/** Valor principal del indicador: "$ 12.450.300", "86,4 %", "4,2 días", "1.240 m³" o "35". */
export function formatearValor(valor, unidad) {
  const numero = Number(valor ?? 0);
  if (unidad === "COP") return formatearRecaudo(numero);
  if (unidad === "%") return formatearPorcentaje(numero);
  if (unidad === "días") return `${FORMATO_DECIMAL.format(numero)} días`;
  if (unidad === "m³") return formatearConsumo(numero);
  return FORMATO_ENTERO.format(numero);
}

/** Valor con su unidad para la ayuda emergente y la tabla: "$ 12.450.300,00" o "35 contratos". */
export function formatearValorCompleto(valor, unidad) {
  if (unidad === "COP") return formatearMoneda(valor);
  if (!esConteo(unidad)) return formatearValor(valor, unidad);
  return `${FORMATO_ENTERO.format(Number(valor ?? 0))} ${unidadConteo(valor, unidad)}`;
}

/** Etiqueta corta del eje: "$ 12,5 M", "80 %", "1,2 mil". */
export function formatearEje(valor, unidad) {
  if (unidad === "COP") return `$ ${FORMATO_COMPACTO.format(valor)}`;
  if (unidad === "%") return `${FORMATO_ENTERO.format(valor)} %`;
  return FORMATO_COMPACTO.format(valor);
}

/** Máximo del eje: el mayor valor redondeado hacia arriba a una cifra cómoda (10, 15, 20, 25, 30, 40…). */
export function maximoEje(valores, unidad) {
  const maximo = Math.max(...valores.map(Number), 0);
  if (unidad === "%" && maximo > 50) return 100;
  if (maximo <= 0) return 1;
  const magnitud = 10 ** Math.floor(Math.log10(maximo));
  const paso = [1, 1.5, 2, 2.5, 3, 4, 5, 6, 8, 10].find((factor) => factor * magnitud >= maximo) ?? 10;
  return paso * magnitud;
}

/** "2026-09" → "sep" para el eje; el año se agrega en enero y en el primer mes. */
export function etiquetaMes(periodo, indice) {
  const partes = partesPeriodo(periodo);
  const corto = partes.mes.slice(0, 3).toLowerCase();
  return periodo.endsWith("-01") || indice === 0 ? `${corto} ${partes.anio.slice(2)}` : corto;
}

/**
 * La variación del backend compara el valor actual con el mes anterior de la tendencia. Solo tiene
 * sentido cuando el valor actual es el último punto de esa misma serie: en indicadores como "Acuerdos
 * vigentes" el valor es una foto del momento y la tendencia cuenta otra cosa (acuerdos registrados por mes).
 */
export function variacionComparable(indicador) {
  const { tendencia, valorActual, variacionPorcentual } = indicador;
  if (variacionPorcentual === null || variacionPorcentual === undefined || tendencia.length < 2) return null;
  return Number(tendencia.at(-1).valor) === Number(valorActual) ? Number(variacionPorcentual) : null;
}

/**
 * Lectura de la variación según el sentido deseado del indicador: subir el recaudo es bueno, subir la
 * cartera vencida es malo. Devuelve el tono (exito, peligro o neutro) y el texto con signo.
 */
export function presentarVariacion(variacion, sentidoDeseado) {
  if (variacion === null) return null;
  const favorable = sentidoDeseado === "BAJAR" ? variacion < 0 : variacion > 0;
  const tono = variacion === 0 ? "neutro" : favorable ? "exito" : "peligro";
  const signo = variacion > 0 ? "+" : variacion < 0 ? "−" : "";
  return {
    tono,
    direccion: variacion > 0 ? "sube" : variacion < 0 ? "baja" : "igual",
    texto: `${signo}${FORMATO_DECIMAL.format(Math.abs(variacion))} %`,
    lectura: variacion === 0 ? "Sin cambio" : favorable ? "Mejora" : "Empeora",
  };
}

/** Indicador con tendencia graficable (al menos dos meses). */
export const tieneTendencia = (indicador) => indicador.tendencia.length >= 2;

/** Todos los indicadores del dashboard en una sola lista, con su categoría. */
export const indicadoresDe = (dashboard) =>
  (dashboard.categorias ?? []).flatMap((categoria) =>
    categoria.indicadores.map((indicador) => ({ ...indicador, categoria: categoria.categoria })),
  );