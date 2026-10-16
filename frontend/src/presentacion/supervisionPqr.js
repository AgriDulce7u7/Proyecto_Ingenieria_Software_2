/**
 * Presentación de la supervisión de PQR (StR-02c) y del indicador de cumplimiento (SyR-02f).
 * El nivel de cada PQR lo calcula el backend; aquí solo se define cómo se muestra.
 */

/** Niveles de AlertaPqrResponse.nivel, del más urgente al menos urgente. */
export const NIVELES = [
  { codigo: "VENCIDA", etiqueta: "Vencida", plural: "Vencidas", detalle: "Superaron el plazo", tono: "peligro", clase: "vencido" },
  { codigo: "CRITICA", etiqueta: "Crítica", plural: "Críticas", detalle: "Vencen en 48 h o menos", tono: "advertencia", clase: "plazo" },
  { codigo: "EN_RIESGO", etiqueta: "En riesgo", plural: "En riesgo", detalle: "Vencen en 5 días o menos", tono: "info" },
  { codigo: "EN_PLAZO", etiqueta: "En plazo", plural: "En plazo", detalle: "Más de 5 días", tono: "neutro" },
];

const POR_CODIGO = Object.fromEntries(NIVELES.map((nivel) => [nivel.codigo, nivel]));

export const presentarNivel = (codigo) => POR_CODIGO[codigo] ?? { codigo, etiqueta: codigo, tono: "neutro" };

/** Cantidad de PQR por nivel: { VENCIDA: 2, CRITICA: 1, ... }. */
export function contarPorNivel(alertas) {
  const conteo = Object.fromEntries(NIVELES.map((nivel) => [nivel.codigo, 0]));
  alertas.forEach((alerta) => {
    conteo[alerta.nivel] = (conteo[alerta.nivel] ?? 0) + 1;
  });
  return conteo;
}

/** Tiempo frente al plazo: "Quedan 5 h", "Quedan 3 días", "Venció hace 2 días". */
export function formatearHorasRestantes(horas) {
  const absolutas = Math.abs(horas);
  const texto = absolutas < 48 ? `${absolutas} h` : `${Math.floor(absolutas / 24)} días`;
  if (horas < 0) return `Venció hace ${texto}`;
  if (horas === 0) return "Vence en menos de 1 h";
  return `Quedan ${texto}`;
}

/**
 * Carga de cada gestor: PQR pendientes y cuántas están vencidas o críticas, ordenada de mayor a
 * menor urgencia para que el jefe vea primero a quién apoyar o redistribuir.
 */
export function cargaPorGestor(alertas) {
  const gestores = new Map();
  alertas.forEach((alerta) => {
    const clave = alerta.gestorId ?? "sin-gestor";
    if (!gestores.has(clave)) {
      gestores.set(clave, {
        clave,
        gestor: alerta.gestor ?? "Sin asignar",
        municipio: alerta.municipio,
        pendientes: 0,
        vencidas: 0,
        criticas: 0,
      });
    }
    const fila = gestores.get(clave);
    fila.pendientes += 1;
    if (alerta.nivel === "VENCIDA") fila.vencidas += 1;
    if (alerta.nivel === "CRITICA") fila.criticas += 1;
  });
  return [...gestores.values()].sort(
    (a, b) => b.vencidas - a.vencidas || b.criticas - a.criticas || b.pendientes - a.pendientes,
  );
}

const FORMATO_PORCENTAJE = new Intl.NumberFormat("es-CO", { maximumFractionDigits: 1 });

/** 86.4 → "86,4 %". El backend envía el porcentaje con un decimal (BigDecimal). */
export const formatearPorcentaje = (valor) => `${FORMATO_PORCENTAJE.format(Number(valor ?? 0))} %`;

/** Fecha local en formato ISO (AAAA-MM-DD) para los parámetros desde/hasta. */
export function aFechaIso(fecha) {
  const mes = String(fecha.getMonth() + 1).padStart(2, "0");
  const dia = String(fecha.getDate()).padStart(2, "0");
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}

/** Periodos rápidos del filtro de cumplimiento. */
export function periodosRapidos(hoy = new Date()) {
  const hasta = aFechaIso(hoy);
  return [
    { codigo: "mes", texto: "Este mes", desde: aFechaIso(new Date(hoy.getFullYear(), hoy.getMonth(), 1)), hasta },
    {
      codigo: "trimestre",
      texto: "Últimos 3 meses",
      desde: aFechaIso(new Date(hoy.getFullYear(), hoy.getMonth() - 2, 1)),
      hasta,
    },
    { codigo: "anio", texto: "Este año", desde: aFechaIso(new Date(hoy.getFullYear(), 0, 1)), hasta },
  ];
}