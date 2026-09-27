/** Formato del número de radicado que genera el backend: año y mes + consecutivo (202609-0001). */
export const FORMATO_RADICADO = /^\d{6}-\d{4}$/;

export const normalizarRadicado = (valor) => valor.trim();