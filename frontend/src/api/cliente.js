/**
 * Cliente HTTP único del frontend.
 *
 * - Toma la URL base de VITE_API_URL (archivo .env).
 * - Envía el token JWT de la sesión en el encabezado Authorization.
 * - Envía JSON o, si el cuerpo es un FormData, archivos (multipart).
 * - Convierte las respuestas de error del backend (ProblemDetail, RFC 7807) en ErrorApi,
 *   con el código HTTP, el título, el detalle y los errores por campo de las validaciones.
 * - Si el backend rechaza el token (401), cierra la sesión local para volver al login.
 */

import { marcarSesionVencida, obtenerToken } from "../sesion/almacenSesion";

const URL_BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api";

export class ErrorApi extends Error {
  constructor({ estado, titulo, detalle, errores, propiedades }) {
    super(detalle || titulo || "Error inesperado");
    this.name = "ErrorApi";
    this.estado = estado;          // código HTTP (0 si no hubo respuesta)
    this.titulo = titulo;          // "Datos inválidos", "Recurso no encontrado"...
    this.detalle = detalle;
    this.errores = errores ?? {};  // { campo: "mensaje" } en los 400 de validación
    // Datos extra del ProblemDetail, p. ej. { codigoUsuarioExistente } en el 409 de un usuario duplicado
    this.propiedades = propiedades ?? {};
  }

  get esValidacion() {
    return this.estado === 400 && Object.keys(this.errores).length > 0;
  }

    get conflicto() {
    return this.estado === 409;
  }

  get noEncontrado() {
    return this.estado === 404;
  }

  get noAutenticado() {
    return this.estado === 401;
  }

  get sinPermiso() {
    return this.estado === 403;
  }

  get cuentaBloqueada() {
    return this.estado === 423;
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
    propiedades: cuerpo ?? undefined,
  });
}

function construirUrl(ruta, parametros) {
  const url = new URL(URL_BASE + ruta);
  Object.entries(parametros ?? {}).forEach(([clave, valor]) => {
    if (valor !== undefined && valor !== null && valor !== "") {
      url.searchParams.set(clave, valor);
    }
  });
  return url;
}

/** Hace la petición y lanza ErrorApi si falla. Devuelve la respuesta HTTP sin leer el cuerpo. */
async function enviar(ruta, { metodo = "GET", cuerpo, parametros } = {}) {
  const token = obtenerToken();
  const esArchivo = cuerpo instanceof FormData;
  const encabezados = {};
  if (token) encabezados.Authorization = `Bearer ${token}`;
  // Con FormData el navegador pone el Content-Type multipart con su separador
  if (cuerpo !== undefined && !esArchivo) encabezados["Content-Type"] = "application/json";

  let respuesta;
  try {
    respuesta = await fetch(construirUrl(ruta, parametros), {
      method: metodo,
      headers: encabezados,
      body: cuerpo === undefined ? undefined : esArchivo ? cuerpo : JSON.stringify(cuerpo),
    });
  } catch {
    throw new ErrorApi({
      estado: 0,
      titulo: "Sin conexión",
      detalle: "No fue posible comunicarse con el servidor. Verifica que el backend esté en ejecución.",
    });
  }

  if (!respuesta.ok) {
    // Un 401 con token significa que venció o es inválido. Sin token (el login) es solo un error.
    if (respuesta.status === 401 && token) marcarSesionVencida();
    throw await leerError(respuesta);
  }
  return respuesta;
}

/**
 * Petición JSON.
 * @param {string} ruta ruta relativa a la API, por ejemplo "/pqr/consulta/202609-0001"
 * @param {{ metodo?: string, cuerpo?: unknown, parametros?: Record<string, unknown> }} opciones
 */
export async function solicitar(ruta, opciones) {
  const respuesta = await enviar(ruta, opciones);
  if (respuesta.status === 204) {
    return null;
  }
  return respuesta.json();
}

/** Nombre del archivo según Content-Disposition (filename*=UTF-8''… o filename="…"). */
function nombreDelArchivo(encabezado, porDefecto) {
  if (!encabezado) return porDefecto;
  const codificado = /filename\*=UTF-8''([^;]+)/i.exec(encabezado);
  if (codificado) return decodeURIComponent(codificado[1]);
  const simple = /filename="?([^";]+)"?/i.exec(encabezado);
  return simple ? simple[1] : porDefecto;
}

/** Descarga un archivo (PDF, Excel, evidencia) y lo guarda con el nombre que indica el backend. */
export async function descargar(ruta, { parametros, nombre = "archivo" } = {}) {
  const respuesta = await enviar(ruta, { parametros });
  const archivo = await respuesta.blob();
  const enlace = document.createElement("a");
  enlace.href = URL.createObjectURL(archivo);
  enlace.download = nombreDelArchivo(respuesta.headers.get("Content-Disposition"), nombre);
  document.body.append(enlace);
  enlace.click();
  enlace.remove();
  // Se libera la URL después de que el navegador inicia la descarga
  setTimeout(() => URL.revokeObjectURL(enlace.href), 1000);
}