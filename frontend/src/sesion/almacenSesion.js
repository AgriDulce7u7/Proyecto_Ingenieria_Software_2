/**
 * Almacén de la sesión fuera de React: lo usa el cliente HTTP para enviar el token y avisar
 * cuando el backend lo rechaza (401), y lo usa SesionProvider para reflejarlo en la interfaz.
 *
 * La sesión se guarda en localStorage para que sobreviva a recargas y a pestañas nuevas
 * mientras el token no haya vencido (8 horas). Nunca se guardan contraseñas.
 */

const CLAVE = "sigca-epq:sesion";
const CLAVE_ANTERIOR = "sigca-epq:gestor"; // selector de la entrega 1, ya no se usa

let sesionActual = leerGuardada();
const suscriptores = new Set();

function vigente(sesion) {
  return Boolean(sesion?.token) && new Date(sesion.expiraEn).getTime() > Date.now();
}

function leerGuardada() {
  try {
    localStorage.removeItem(CLAVE_ANTERIOR);
    const guardada = JSON.parse(localStorage.getItem(CLAVE));
    return vigente(guardada) ? guardada : null;
  } catch {
    return null;
  }
}

function persistir(sesion) {
  try {
    if (sesion) localStorage.setItem(CLAVE, JSON.stringify(sesion));
    else localStorage.removeItem(CLAVE);
  } catch {
    // Sin almacenamiento disponible: la sesión dura mientras la pestaña esté abierta.
  }
}

export const obtenerSesion = () => sesionActual;

export const obtenerToken = () => (vigente(sesionActual) ? sesionActual.token : null);

/** Guarda la respuesta del login ({ token, expiraEn, usuario }) o la borra con null. */
export function guardarSesion(sesion, motivo = null) {
  sesionActual = sesion;
  persistir(sesion);
  suscriptores.forEach((alCambiar) => alCambiar(sesion, motivo));
}

/** Notifica cada cambio de sesión. Devuelve la función para cancelar la suscripción. */
export function suscribirse(alCambiar) {
  suscriptores.add(alCambiar);
  return () => suscriptores.delete(alCambiar);
}

/** El backend rechazó el token (vencido o inválido): se cierra la sesión local. */
export function marcarSesionVencida() {
  if (sesionActual) guardarSesion(null, "vencida");
}