/** Presentación y validaciones del portal ciudadano (F-06): contraseña, tarjeta y estado del contrato. */

/** PoliticaContrasena del backend: 8 a 64 caracteres con mayúscula, minúscula y número. */
export const PATRON_CONTRASENA = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,64}$/;
export const MENSAJE_CONTRASENA = "Use entre 8 y 64 caracteres, con al menos una mayúscula, una minúscula y un número.";

/** Reglas de la contraseña que se muestran mientras el ciudadano la escribe. */
export const reglasContrasena = (contrasena) => [
  { texto: "Entre 8 y 64 caracteres", cumple: contrasena.length >= 8 && contrasena.length <= 64 },
  { texto: "Una letra mayúscula", cumple: /[A-Z]/.test(contrasena) },
  { texto: "Una letra minúscula", cumple: /[a-z]/.test(contrasena) },
  { texto: "Un número", cumple: /\d/.test(contrasena) },
];

/** Solo los dígitos de la tarjeta: se escribe con espacios (4111 1111 1111 1111) y se envía sin ellos. */
export const digitosTarjeta = (valor) => valor.replace(/\D/g, "").slice(0, 19);

/** "4111111111111111" → "4111 1111 1111 1111" */
export const formatearTarjeta = (valor) => digitosTarjeta(valor).replace(/(\d{4})(?=\d)/g, "$1 ");

/**
 * Validación básica de la tarjeta (CO-02), igual a la del backend. Si el número es válido para el
 * banco lo decide la pasarela; aquí solo se revisa el formato y que no esté vencida.
 */
export function validarTarjeta(datos, hoy = new Date()) {
  const errores = {};
  const numero = digitosTarjeta(datos.numeroTarjeta);
  if (numero.length < 13) errores.numeroTarjeta = "Escriba el número completo de la tarjeta (13 a 19 dígitos).";
  if (!datos.titular.trim()) errores.titular = "Escriba el nombre como aparece en la tarjeta.";
  if (!datos.mesVencimiento) errores.mesVencimiento = "Seleccione el mes.";
  if (!datos.anioVencimiento) errores.anioVencimiento = "Seleccione el año.";
  if (datos.mesVencimiento && datos.anioVencimiento) {
    const vence = Number(datos.anioVencimiento) * 12 + Number(datos.mesVencimiento);
    const actual = hoy.getFullYear() * 12 + hoy.getMonth() + 1;
    if (vence < actual) errores.mesVencimiento = "La tarjeta está vencida.";
  }
  if (!/^\d{3,4}$/.test(datos.cvv)) errores.cvv = "Escriba los 3 o 4 dígitos del reverso.";
  return errores;
}

/** Años de vencimiento que se ofrecen: el actual y los 12 siguientes. */
export const aniosVencimiento = (hoy = new Date()) =>
  Array.from({ length: 13 }, (_, indice) => String(hoy.getFullYear() + indice));

/** Estado del contrato visto por el ciudadano. */
const ESTADOS_CONTRATO_PORTAL = {
  ACTIVO: { etiqueta: "Activo", tono: "exito" },
  PENDIENTE: { etiqueta: "En activación", tono: "info" },
  SUSPENDIDO: { etiqueta: "Suspendido", tono: "peligro" },
};

export const presentarEstadoContratoPortal = (estado) =>
  ESTADOS_CONTRATO_PORTAL[estado] ?? { etiqueta: estado, tono: "neutro" };

/** Primer nombre para el saludo: "María Fernanda López" → "María". */
export const primerNombre = (nombre = "") => nombre.trim().split(/\s+/)[0] ?? "";

/** ¿La factura está por pagar? Si además está en un acuerdo de pago lo indica su detalle (enAcuerdoDePago). */
export const estaPorPagar = (factura) => factura.estadoCobro === "PENDIENTE" || factura.estadoCobro === "VENCIDA";