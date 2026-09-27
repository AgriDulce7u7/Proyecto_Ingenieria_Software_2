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

const FORMATO_DIA_MES_ANIO = new Intl.DateTimeFormat(LOCALE, { day: "numeric", month: "short", year: "numeric" });
const FORMATO_HORA = new Intl.DateTimeFormat(LOCALE, { hour: "numeric", minute: "2-digit" });

/** Arma "16 sept 2026" con las partes de la fecha, sin los "de" ni los puntos que agrega es-CO. */
function formatoCorto(fecha) {
  const partes = Object.fromEntries(
    FORMATO_DIA_MES_ANIO.formatToParts(fecha).map((parte) => [parte.type, parte.value]),
  );
  return `${partes.day} ${partes.month.replace(".", "")} ${partes.year}`;
}

/** "16 sept 2026" */
export function formatearFechaCorta(texto) {
  const fecha = aFecha(texto);
  return fecha ? formatoCorto(fecha) : "";
}

const mismoDia = (a, b) =>
  a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();

/** Fecha límite en la bandeja: "Hoy, 5:00 p. m.", "Mañana, 11:59 p. m." o "16 sept 2026". */
export function formatearFechaLimite(texto, ahora = new Date()) {
  const fecha = aFecha(texto);
  if (!fecha) return "";
  const manana = new Date(ahora.getFullYear(), ahora.getMonth(), ahora.getDate() + 1);
  if (mismoDia(fecha, ahora)) return `Hoy, ${FORMATO_HORA.format(fecha)}`;
  if (mismoDia(fecha, manana)) return `Mañana, ${FORMATO_HORA.format(fecha)}`;
  return formatoCorto(fecha);
}

/** Tiempo transcurrido: "Hace un momento", "Hace 18 min", "Hace 3 h", "Ayer, 4:32 p. m." o "16 sept 2026". */
export function formatearTiempoRelativo(texto, ahora = new Date()) {
  const fecha = aFecha(texto);
  if (!fecha) return "";
  const minutos = Math.floor((ahora - fecha) / 60000);
  const ayer = new Date(ahora.getFullYear(), ahora.getMonth(), ahora.getDate() - 1);

  if (minutos < 1) return "Hace un momento";
  if (minutos < 60) return `Hace ${minutos} min`;
  if (mismoDia(fecha, ahora)) return `Hace ${Math.floor(minutos / 60)} h`;
  if (mismoDia(fecha, ayer)) return `Ayer, ${FORMATO_HORA.format(fecha)}`;
  return formatoCorto(fecha);
}

/** "4:32 p. m." */
export function formatearHora(texto) {
  const fecha = aFecha(texto);
  return fecha ? FORMATO_HORA.format(fecha) : "";
}