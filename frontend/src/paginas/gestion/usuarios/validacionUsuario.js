/**
 * Reglas de los formularios de F-03. Replican RegistrarUsuarioRequest, ActualizarUsuarioRequest y
 * RegistrarPredioRequest del backend para avisar antes de enviar; el backend sigue decidiendo.
 */

import {
  PATRON_CORREO,
  PATRON_DOCUMENTO,
  PATRON_TELEFONO,
  TIPOS_DOCUMENTO,
  limpiarDocumento,
} from "../../../presentacion/datosPersonales";

export const LIMITES = { nombreCompleto: 150, correo: 150, direccion: 200, motivo: 500 };

export const USUARIO_VACIO = {
  tipoDocumento: "CC",
  numeroDocumento: "",
  tipoPersona: "",
  nombreCompleto: "",
  telefono: "",
  correo: "",
  municipioId: "",
  direccionPredio: "",
  estrato: "",
  usoPredio: "RESIDENCIAL",
  direccionNotificacion: "",
  servicioSolicitado: "",
};

const obligatorio = (mensaje) => (v) => (String(v ?? "").trim() ? null : mensaje);
const conMaximo = (maximo, mensaje) => (v) => {
  if (!String(v).trim()) return mensaje;
  return v.trim().length <= maximo ? null : `Máximo ${maximo} caracteres.`;
};

/** Reglas por campo. Se reutilizan en el registro, la edición y el registro de predios. */
const REGLAS = {
  tipoDocumento: (v) => (TIPOS_DOCUMENTO.some((t) => t.codigo === v) ? null : "Seleccione el tipo de documento."),
  numeroDocumento: (v) => {
    const limpio = limpiarDocumento(v);
    if (!limpio) return "Ingrese el número de identificación.";
    return PATRON_DOCUMENTO.test(limpio) ? null : "Use entre 4 y 20 letras, números o guiones.";
  },
  nombreCompleto: conMaximo(LIMITES.nombreCompleto, "Ingrese el nombre completo o la razón social."),
  telefono: (v) => {
    if (!v.trim()) return "Ingrese un teléfono de contacto.";
    return PATRON_TELEFONO.test(v.trim()) ? null : "El teléfono debe tener entre 7 y 20 dígitos.";
  },
  correo: (v) => {
    if (!v.trim()) return "Ingrese el correo electrónico.";
    if (v.trim().length > LIMITES.correo) return `Máximo ${LIMITES.correo} caracteres.`;
    return PATRON_CORREO.test(v.trim()) ? null : "El correo electrónico no es válido.";
  },
  municipioId: obligatorio("Seleccione el municipio."),
  direccionPredio: conMaximo(LIMITES.direccion, "Ingrese la dirección del predio."),
  direccion: conMaximo(LIMITES.direccion, "Ingrese la dirección del predio."),
  estrato: obligatorio("Seleccione el estrato."),
  direccionNotificacion: (v) =>
    v.trim().length <= LIMITES.direccion ? null : `Máximo ${LIMITES.direccion} caracteres.`,
  direccionNotificacionObligatoria: conMaximo(LIMITES.direccion, "Ingrese la dirección de notificación."),
};

export const validarCampo = (regla, valor) => REGLAS[regla]?.(valor) ?? null;

/** Valida los campos indicados ({ campo: regla }). Devuelve { campo: mensaje } solo con los inválidos. */
export function validar(datos, campos) {
  return Object.fromEntries(
    Object.entries(campos)
      .map(([campo, regla]) => [campo, validarCampo(regla, datos[campo])])
      .filter(([, error]) => error),
  );
}

/** Campos del registro y la regla de cada uno. */
export const CAMPOS_REGISTRO = {
  tipoDocumento: "tipoDocumento",
  numeroDocumento: "numeroDocumento",
  nombreCompleto: "nombreCompleto",
  telefono: "telefono",
  correo: "correo",
  municipioId: "municipioId",
  direccionPredio: "direccionPredio",
  estrato: "estrato",
  direccionNotificacion: "direccionNotificacion",
};

/** Campos de la edición del perfil. La identificación no se modifica (SWR-12). */
export const CAMPOS_EDICION = {
  nombreCompleto: "nombreCompleto",
  telefono: "telefono",
  correo: "correo",
  municipioId: "municipioId",
  direccionNotificacion: "direccionNotificacionObligatoria",
};

export const CAMPOS_PREDIO = { direccion: "direccion", municipioId: "municipioId", estrato: "estrato" };

const opcional = (valor) => valor.trim() || null;

/** Cuerpo de POST /api/usuarios-servicio. El tipo de persona vacío lo deduce el backend (NIT → jurídica). */
export function aSolicitudRegistro(datos) {
  return {
    tipoDocumento: datos.tipoDocumento,
    numeroDocumento: limpiarDocumento(datos.numeroDocumento),
    tipoPersona: datos.tipoPersona || null,
    nombreCompleto: datos.nombreCompleto.trim(),
    telefono: datos.telefono.trim(),
    correo: datos.correo.trim(),
    municipioId: Number(datos.municipioId),
    direccionPredio: datos.direccionPredio.trim(),
    estrato: Number(datos.estrato),
    usoPredio: datos.usoPredio || null,
    direccionNotificacion: opcional(datos.direccionNotificacion),
    servicioSolicitado: datos.servicioSolicitado || null,
  };
}

/** Cuerpo de PUT /api/usuarios-servicio/{codigo}. */
export const aSolicitudEdicion = (datos) => ({
  nombreCompleto: datos.nombreCompleto.trim(),
  telefono: datos.telefono.trim(),
  correo: datos.correo.trim(),
  direccionNotificacion: datos.direccionNotificacion.trim(),
  municipioId: Number(datos.municipioId),
});

/** Cuerpo de POST /api/usuarios-servicio/{codigo}/predios. */
export const aSolicitudPredio = (datos) => ({
  direccion: datos.direccion.trim(),
  municipioId: Number(datos.municipioId),
  estrato: Number(datos.estrato),
  uso: datos.uso || null,
});