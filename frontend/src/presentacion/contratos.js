/**
 * Presentación de F-04. Los estados se muestran con presentarEstadoContrato (presentacion/usuarios.js),
 * que también usa el perfil del usuario.
 */

/** Acción de cada estado destino, según el estado actual (accionesDisponibles del backend). */
export function accionHacia(destino, estadoActual) {
  if (destino === "ACTIVO") {
    return estadoActual === "SUSPENDIDO"
      ? { texto: "Reactivar", titulo: "Reactivar contrato", variante: "principal" }
      : { texto: "Activar", titulo: "Activar contrato", variante: "principal" };
  }
  if (destino === "SUSPENDIDO") return { texto: "Suspender", titulo: "Suspender contrato", variante: "secundario" };
  if (destino === "CANCELADO") return { texto: "Cancelar contrato", titulo: "Cancelar contrato", variante: "peligro" };
  return { texto: destino, titulo: destino, variante: "secundario" };
}

/** Explicación de cada cambio de estado en el diálogo de confirmación. */
export const EFECTO_DEL_CAMBIO = {
  ACTIVO: "El contrato entra en la facturación mensual desde el próximo lote.",
  SUSPENDIDO: "El servicio queda suspendido y el contrato deja de facturarse hasta que se reactive.",
  CANCELADO: "Es definitivo: el contrato no se puede reactivar y el medidor queda retirado.",
};

/**
 * Las advertencias del backend a veces traen la ruta de la API para el desarrollador
 * ("Asígnelo con PUT /api/..."). Esa frase no le sirve a quien atiende, así que se quita.
 */
export const limpiarAdvertencia = (texto) =>
  texto
    .split(/(?<=\.)\s+/)
    .filter((frase) => !frase.includes("/api/"))
    .join(" ");

/** Mismo patrón del backend para el número de medidor. */
export const PATRON_MEDIDOR = /^[A-Za-z0-9-]{4,50}$/;