/**
 * Presentación de F-03 (usuarios del servicio y predios) y de los estados de contrato que se ven
 * en el perfil. Los códigos son los de los enums del backend.
 */

export const TIPOS_PERSONA = [
  { codigo: "NATURAL", nombre: "Persona natural" },
  { codigo: "JURIDICA", nombre: "Persona jurídica" },
];

export const USOS_PREDIO = [
  { codigo: "RESIDENCIAL", nombre: "Residencial" },
  { codigo: "COMERCIAL", nombre: "Comercial" },
  { codigo: "INDUSTRIAL", nombre: "Industrial" },
];

/** TipoServicio del backend. Acueducto y gas requieren medidor para activar el contrato (SWR-19). */
export const SERVICIOS = [
  { codigo: "ACUEDUCTO", nombre: "Acueducto", requiereMedidor: true },
  { codigo: "ALCANTARILLADO", nombre: "Alcantarillado", requiereMedidor: false },
  { codigo: "GAS", nombre: "Gas", requiereMedidor: true },
];

export const ESTRATOS = [1, 2, 3, 4, 5, 6];

const nombreDe = (lista) => (codigo) => lista.find((item) => item.codigo === codigo)?.nombre ?? codigo;
export const nombreUsoPredio = nombreDe(USOS_PREDIO);
export const nombreTipoPersona = nombreDe(TIPOS_PERSONA);
export const nombreServicio = nombreDe(SERVICIOS);

/** Validación de identidad contra la RNEC (EstadoValidacionIdentidad, SWR-14). */
const VALIDACIONES = {
  VALIDADO: { etiqueta: "Identidad validada", corta: "Validada", tono: "exito" },
  PENDIENTE: { etiqueta: "Validación pendiente", corta: "Pendiente", tono: "advertencia" },
  RECHAZADO: { etiqueta: "Identidad no encontrada", corta: "Rechazada", tono: "peligro" },
};

export const presentarValidacion = (estado) =>
  VALIDACIONES[estado] ?? { etiqueta: estado, corta: estado, tono: "neutro" };

/** Estado del contrato (EstadoContrato, SWR-20). */
const ESTADOS_CONTRATO = {
  PENDIENTE: { etiqueta: "Pendiente", tono: "advertencia" },
  ACTIVO: { etiqueta: "Activo", tono: "exito" },
  SUSPENDIDO: { etiqueta: "Suspendido", tono: "peligro" },
  CANCELADO: { etiqueta: "Cancelado", tono: "neutro" },
};

export const ESTADOS_CONTRATO_CODIGOS = Object.keys(ESTADOS_CONTRATO);

export const presentarEstadoContrato = (estado) => ESTADOS_CONTRATO[estado] ?? { etiqueta: estado, tono: "neutro" };

/** Solo se desactiva un usuario cuando todos sus contratos están cancelados (regla del backend). */
export const tieneContratosVigentes = (usuario) =>
  usuario.contratos.some((contrato) => contrato.estado !== "CANCELADO");