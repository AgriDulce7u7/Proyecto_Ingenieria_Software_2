/**
 * Cliente HTTP único del frontend.
 *
 * - Toma la URL base de VITE_API_URL (archivo .env).
 * - Convierte las respuestas de error del backend (ProblemDetail, RFC 7807) en ErrorApi,
 *   con el código HTTP, el título, el detalle y los errores por campo de las validaciones.
 * - Es el único punto donde se agregaría el token cuando exista autenticación.
 */

const URL_BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api";

export class ErrorApi extends Error {
  constructor({ estado, titulo, detalle, errores }) {
    super(detalle || titulo || "Error inesperado");
    this.name = "ErrorApi";
    this.estado = estado;          // código HTTP (0 si no hubo respuesta)
    this.titulo = titulo;          // "Datos inválidos", "Recurso no encontrado"...
    this.detalle = detalle;
    this.errores = errores ?? {};  // { campo: "mensaje" } en los 400 de validación
  }

  get esValidacion() {
    return this.estado === 400 && Object.keys(this.errores).length > 0;
  }

  get noEncontrado() {
    return this.estado === 404;
  }
}

async function leerError(respuesta) {
  let cuerpo = null;
  try {
    cuerpo = await respuesta.json();
  } catch {
    // El cuerpo no es JSON (por ejemplo, un error del proxy): se usa solo el código HTTP.
  }
  return new ErrorApi({
    estado: respuesta.status,
    titulo: cuerpo?.title,
    detalle: cuerpo?.detail ?? `El servidor respondió con el código ${respuesta.status}`,
    errores: cuerpo?.errores,
  });
}

/**
 * @param {string} ruta ruta relativa a la API, por ejemplo "/pqr/consulta/202609-0001"
 * @param {{ metodo?: string, cuerpo?: unknown, parametros?: Record<string, unknown> }} opciones
 */
export async function solicitar(ruta, { metodo = "GET", cuerpo, parametros } = {}) {
  const url = new URL(URL_BASE + ruta);
  Object.entries(parametros ?? {}).forEach(([clave, valor]) => {
    if (valor !== undefined && valor !== null && valor !== "") {
      url.searchParams.set(clave, valor);
    }
  });

  let respuesta;
  try {
    respuesta = await fetch(url, {
      method: metodo,
      headers: cuerpo !== undefined ? { "Content-Type": "application/json" } : undefined,
      body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined,
    });
  } catch {
    throw new ErrorApi({
      estado: 0,
      titulo: "Sin conexión",
      detalle: "No fue posible comunicarse con el servidor. Verifica que el backend esté en ejecución.",
    });
  }

  if (!respuesta.ok) {
    throw await leerError(respuesta);
  }
  if (respuesta.status === 204) {
    return null;
  }
  return respuesta.json();
}
