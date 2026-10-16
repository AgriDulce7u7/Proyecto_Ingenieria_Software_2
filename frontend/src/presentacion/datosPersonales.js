/**
 * Datos personales compartidos por los formularios de F-02 (radicar PQR) y F-03 (registrar usuario):
 * tipos de documento y patrones de validación iguales a los del backend.
 */

export const TIPOS_DOCUMENTO = [
  { codigo: "CC", nombre: "Cédula de ciudadanía" },
  { codigo: "CE", nombre: "Cédula de extranjería" },
  { codigo: "TI", nombre: "Tarjeta de identidad" },
  { codigo: "NIT", nombre: "NIT" },
  { codigo: "PA", nombre: "Pasaporte" },
];

/** Mismo patrón del backend: 4 a 20 letras, números o guiones. */
export const PATRON_DOCUMENTO = /^[0-9A-Za-z-]{4,20}$/;

export const PATRON_CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
export const PATRON_TELEFONO = /^[0-9+ ]{7,20}$/;

/** El documento se acepta con puntos o espacios (1.094.912.580) y se envía sin ellos. */
export const limpiarDocumento = (valor) => valor.replace(/[.\s]/g, "");