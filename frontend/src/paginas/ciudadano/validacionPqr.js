/**
 * Reglas del formulario de radicación. Replican las validaciones de RegistrarPqrRequest en el backend
 * para avisar al ciudadano antes de enviar; el backend sigue siendo la validación definitiva.
 */

import {
  PATRON_CORREO,
  PATRON_DOCUMENTO,
  PATRON_TELEFONO,
  TIPOS_DOCUMENTO,
  limpiarDocumento,
} from "../../presentacion/datosPersonales";

export { TIPOS_DOCUMENTO, limpiarDocumento };

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
  municipioId: "",
  numeroContrato: "",
  tipoSolicitud: "",
  asunto: "",
  descripcion: "",
};

const PATRON_CONTRATO = /^[A-Za-z0-9-]{4,30}$/;

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
  numeroContrato: (v) =>
    !v.trim() || PATRON_CONTRATO.test(v.trim()) ? null : "Use entre 4 y 30 letras, números o guiones.",
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
    // Entrega 2: con contrato el backend toma el municipio de su predio; sin él, el que elija el ciudadano
    numeroContrato: opcional(datos.numeroContrato),
    municipioId: datos.municipioId ? Number(datos.municipioId) : null,
    tipoSolicitud: datos.tipoSolicitud,
    canal: "WEB",
    asunto: datos.asunto.trim(),
    descripcion: datos.descripcion.trim(),
  };
}