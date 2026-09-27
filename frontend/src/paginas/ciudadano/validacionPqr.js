/**
 * Reglas del formulario de radicación. Replican las validaciones de RegistrarPqrRequest en el backend
 * para avisar al ciudadano antes de enviar; el backend sigue siendo la validación definitiva.
 */

export const TIPOS_DOCUMENTO = [
  { codigo: "CC", nombre: "Cédula de ciudadanía" },
  { codigo: "CE", nombre: "Cédula de extranjería" },
  { codigo: "TI", nombre: "Tarjeta de identidad" },
  { codigo: "NIT", nombre: "NIT" },
  { codigo: "PA", nombre: "Pasaporte" },
];

export const LIMITES = {
  nombreCompleto: 150,
  correo: 150,
  direccion: 200,
  asunto: 200,
  descripcionMinima: 10,
  descripcionMaxima: 5000,
};

export const PQR_VACIA = {
  tipoDocumento: "CC",
  numeroDocumento: "",
  nombreCompleto: "",
  correo: "",
  telefono: "",
  direccion: "",
  tipoSolicitud: "",
  asunto: "",
  descripcion: "",
};

const PATRON_DOCUMENTO = /^[0-9A-Za-z-]{4,20}$/;
const PATRON_CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PATRON_TELEFONO = /^[0-9+ ]{7,20}$/;

/** El documento se acepta con puntos o espacios (1.094.912.580) y se envía sin ellos. */
export const limpiarDocumento = (valor) => valor.replace(/[.\s]/g, "");

const REGLAS = {
  tipoDocumento: (v) =>
    TIPOS_DOCUMENTO.some((t) => t.codigo === v) ? null : "Seleccione el tipo de documento.",
  numeroDocumento: (v) => {
    const limpio = limpiarDocumento(v);
    if (!limpio) return "Ingrese su número de documento.";
    return PATRON_DOCUMENTO.test(limpio) ? null : "Use entre 4 y 20 letras, números o guiones.";
  },
  nombreCompleto: (v) => {
    if (!v.trim()) return "Ingrese su nombre completo.";
    return v.trim().length <= LIMITES.nombreCompleto ? null : `Máximo ${LIMITES.nombreCompleto} caracteres.`;
  },
  correo: (v) => {
    if (!v.trim()) return "Ingrese su correo electrónico.";
    if (v.trim().length > LIMITES.correo) return `Máximo ${LIMITES.correo} caracteres.`;
    return PATRON_CORREO.test(v.trim()) ? null : "El correo electrónico no es válido.";
  },
  telefono: (v) =>
    !v.trim() || PATRON_TELEFONO.test(v.trim()) ? null : "El teléfono debe tener entre 7 y 20 dígitos.",
  direccion: (v) => (v.trim().length <= LIMITES.direccion ? null : `Máximo ${LIMITES.direccion} caracteres.`),
  tipoSolicitud: (v) => (v ? null : "Seleccione el tipo de solicitud."),
  asunto: (v) => {
    if (!v.trim()) return "Escriba un asunto breve.";
    return v.trim().length <= LIMITES.asunto ? null : `Máximo ${LIMITES.asunto} caracteres.`;
  },
  descripcion: (v) => {
    const largo = v.trim().length;
    if (!largo) return "Describa su solicitud.";
    if (largo < LIMITES.descripcionMinima) return `Escriba al menos ${LIMITES.descripcionMinima} caracteres.`;
    return largo <= LIMITES.descripcionMaxima ? null : `Máximo ${LIMITES.descripcionMaxima} caracteres.`;
  },
};

/** Valida un campo. Devuelve el mensaje de error o null. */
export const validarCampo = (nombre, valor) => REGLAS[nombre]?.(valor) ?? null;

/** Valida el formulario completo. Devuelve { campo: mensaje } solo con los campos inválidos. */
export function validarPqr(datos) {
  return Object.fromEntries(
    Object.keys(REGLAS)
      .map((campo) => [campo, validarCampo(campo, datos[campo])])
      .filter(([, error]) => error),
  );
}

/** Convierte el formulario al cuerpo que espera POST /api/pqr (el canal por defecto es WEB). */
export function aSolicitudPqr(datos) {
  const opcional = (valor) => valor.trim() || null;
  return {
    tipoDocumento: datos.tipoDocumento,
    numeroDocumento: limpiarDocumento(datos.numeroDocumento),
    nombreCompleto: datos.nombreCompleto.trim(),
    correo: datos.correo.trim(),
    telefono: opcional(datos.telefono),
    direccion: opcional(datos.direccion),
    tipoSolicitud: datos.tipoSolicitud,
    canal: "WEB",
    asunto: datos.asunto.trim(),
    descripcion: datos.descripcion.trim(),
  };
}