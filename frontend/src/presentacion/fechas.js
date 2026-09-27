/**
 * Formato de fechas del backend para mostrarlas en español de Colombia.
 *
 * El backend envía LocalDate ("2026-09-22") y LocalDateTime ("2026-09-01T08:00:15"), sin zona horaria.
 * Una fecha sola no se pasa a `new Date(texto)` porque JavaScript la interpreta en UTC y en Colombia
 * (UTC-5) se mostraría el día anterior; por eso se construye con sus partes en hora local.
 */

const LOCALE = "es-CO";

const FORMATO_FECHA = new Intl.DateTimeFormat(LOCALE, { day: "numeric", month: "long", year: "numeric" });
const FORMATO_FECHA_HORA = new Intl.DateTimeFormat(LOCALE, {
  day: "numeric",
  month: "short",
  year: "numeric",
  hour: "numeric",
  minute: "2-digit",
});

function aFecha(texto) {
  if (!texto) return null;
  if (/^\d{4}-\d{2}-\d{2}$/.test(texto)) {
    const [anio, mes, dia] = texto.split("-").map(Number);
    return new Date(anio, mes - 1, dia);
  }
  const fecha = new Date(texto);
  return Number.isNaN(fecha.getTime()) ? null : fecha;
}

/** "22 de septiembre de 2026" */
export function formatearFecha(texto) {
  const fecha = aFecha(texto);
  return fecha ? FORMATO_FECHA.format(fecha) : "";
}

/** "1 sept 2026, 8:00 a. m." */
export function formatearFechaHora(texto) {
  const fecha = aFecha(texto);
  return fecha ? FORMATO_FECHA_HORA.format(fecha) : "";
}