/**
 * Presentación de los estados de una PQR (DE-02). Las transiciones las decide el backend;
 * aquí solo se define cómo se muestra cada estado. Las claves son los nombres que envía la API.
 */

export const ESTADO = {
  RADICADO: "Radicado",
  EN_TRAMITE: "En trámite",
  RESUELTO: "Resuelto",
  CERRADO: "Cerrado",
  VENCIDO: "Vencido",
};

/** Tono de la etiqueta de estado (ver .etiqueta-* en ui.css). */
const TONOS = {
  [ESTADO.RADICADO]: "neutro",
  [ESTADO.EN_TRAMITE]: "info",
  [ESTADO.VENCIDO]: "peligro",
  [ESTADO.RESUELTO]: "exito",
  [ESTADO.CERRADO]: "neutro",
};

export const tonoEstado = (estado) => TONOS[estado] ?? "neutro";

/** Recorrido normal de una PQR. Vencido reemplaza a "En trámite" cuando se supera el plazo. */
const RECORRIDO = [ESTADO.RADICADO, ESTADO.EN_TRAMITE, ESTADO.RESUELTO, ESTADO.CERRADO];

/**
 * Pasos de la línea de tiempo pública a partir del estado actual.
 * Cada paso tiene { nombre, situacion: "completo" | "actual" | "pendiente", vencido }.
 */
export function pasosSeguimiento(estado) {
  const vencido = estado === ESTADO.VENCIDO;
  const indiceActual = vencido ? 1 : RECORRIDO.indexOf(estado);

  return RECORRIDO.map((nombre, indice) => {
    const esActual = indice === indiceActual;
    let situacion = "pendiente";
    if (indice < indiceActual || (esActual && estado === ESTADO.CERRADO)) situacion = "completo";
    else if (esActual) situacion = "actual";
    return {
      nombre: esActual && vencido ? ESTADO.VENCIDO : nombre,
      situacion,
      vencido: esActual && vencido,
    };
  });
}