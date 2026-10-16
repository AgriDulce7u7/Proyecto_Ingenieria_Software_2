/** Presentación de administración: estado de las cuentas, módulos y acciones de la auditoría. */

/** Usuario de acceso de un funcionario (CrearFuncionarioRequest en el backend). */
export const PATRON_USUARIO = /^[a-zA-Z0-9._-]{3,60}$/;
export const MENSAJE_USUARIO = "Use de 3 a 60 caracteres: letras sin tildes, números, punto, guion o guion bajo.";

/** Estado de la cuenta: el bloqueo por intentos fallidos pesa más que el estado activo. */
export function presentarEstadoFuncionario(funcionario) {
  if (!funcionario.activo) return { codigo: "INACTIVO", etiqueta: "Inactiva", tono: "neutro" };
  if (funcionario.bloqueado) return { codigo: "BLOQUEADO", etiqueta: "Bloqueada", tono: "peligro" };
  return { codigo: "ACTIVO", etiqueta: "Activa", tono: "exito" };
}

/** Opciones del filtro de estado de la lista de funcionarios. */
export const ESTADOS_CUENTA = [
  { valor: "activas", texto: "Activas" },
  { valor: "bloqueadas", texto: "Bloqueadas" },
  { valor: "inactivas", texto: "Inactivas" },
];

/** Contraseña temporal aleatoria de 12 caracteres que cumple la política (mayúscula, minúscula y número). */
export function generarContrasena() {
  const grupos = ["ABCDEFGHJKLMNPQRSTUVWXYZ", "abcdefghijkmnpqrstuvwxyz", "23456789"];
  const todos = grupos.join("");
  const azar = new Uint32Array(12);
  crypto.getRandomValues(azar);
  const caracteres = Array.from(azar, (valor, indice) => {
    const fuente = indice < grupos.length ? grupos[indice] : todos;
    return fuente[valor % fuente.length];
  });
  // Se mezcla para que la mayúscula, la minúscula y el número no queden siempre al inicio
  for (let i = caracteres.length - 1; i > 0; i -= 1) {
    const j = azar[i] % (i + 1);
    [caracteres[i], caracteres[j]] = [caracteres[j], caracteres[i]];
  }
  return caracteres.join("");
}

/** Módulos de la auditoría (ModuloAuditoria en el backend). */
const MODULOS = {
  SISTEMA: "Sistema",
  SEGURIDAD: "Seguridad y acceso",
  USUARIOS: "Usuarios del servicio",
  CONTRATOS: "Contratos",
  FACTURACION: "Facturación",
  PAGOS: "Pagos",
  PQR: "PQR",
  CARTERA: "Cartera",
};

export const presentarModulo = (codigo) => MODULOS[codigo] ?? codigo;

/** Acciones auditadas (AccionAuditoria en el backend) con su módulo, para filtrar las acciones por módulo. */
const ACCIONES = {
  INICIO_SESION: ["Inicio de sesión", "SEGURIDAD"],
  INICIO_SESION_FALLIDO: ["Inicio de sesión fallido", "SEGURIDAD"],
  CUENTA_BLOQUEADA: ["Cuenta bloqueada", "SEGURIDAD"],
  CIERRE_SESION: ["Cierre de sesión", "SEGURIDAD"],
  ACTIVAR_CUENTA_PORTAL: ["Activación de cuenta del portal", "SEGURIDAD"],
  CREAR_FUNCIONARIO: ["Creación de funcionario", "SEGURIDAD"],
  MODIFICAR_FUNCIONARIO: ["Modificación de funcionario", "SEGURIDAD"],
  CAMBIAR_ROL: ["Cambio de rol", "SEGURIDAD"],
  CAMBIAR_ESTADO_FUNCIONARIO: ["Activación o desactivación de cuenta", "SEGURIDAD"],
  RESTABLECER_CONTRASENA: ["Restablecimiento de contraseña", "SEGURIDAD"],
  REGISTRAR_USUARIO: ["Registro de usuario", "USUARIOS"],
  MODIFICAR_USUARIO: ["Modificación de usuario", "USUARIOS"],
  DESACTIVAR_USUARIO: ["Desactivación de usuario", "USUARIOS"],
  VALIDAR_IDENTIDAD_RNEC: ["Validación de identidad RNEC", "USUARIOS"],
  REGISTRAR_PREDIO: ["Registro de predio", "USUARIOS"],
  CREAR_CONTRATO: ["Creación de contrato", "CONTRATOS"],
  CAMBIAR_ESTADO_CONTRATO: ["Cambio de estado de contrato", "CONTRATOS"],
  ASIGNAR_MEDIDOR: ["Asignación de medidor", "CONTRATOS"],
  GENERAR_LOTE_FACTURAS: ["Generación de lote de facturas", "FACTURACION"],
  ANULAR_FACTURA: ["Anulación de factura", "FACTURACION"],
  VENCER_FACTURAS: ["Vencimiento de facturas", "FACTURACION"],
  REGISTRAR_PAGO: ["Registro de pago", "PAGOS"],
  PAGO_EN_LINEA_RECHAZADO: ["Pago en línea rechazado", "PAGOS"],
  REGISTRAR_PQR: ["Radicación de PQR", "PQR"],
  ASIGNAR_PQR: ["Asignación de PQR", "PQR"],
  CAMBIAR_ESTADO_PQR: ["Cambio de estado de PQR", "PQR"],
  RESPONDER_PQR: ["Respuesta de PQR", "PQR"],
  VENCER_PQR: ["Vencimiento de PQR", "PQR"],
  ADJUNTAR_EVIDENCIA_PQR: ["Evidencia de PQR", "PQR"],
  REGISTRAR_ACUERDO_PAGO: ["Registro de acuerdo de pago", "CARTERA"],
  PAGAR_CUOTA_ACUERDO: ["Pago de cuota de acuerdo", "CARTERA"],
  INCUMPLIR_ACUERDO_PAGO: ["Incumplimiento de acuerdo", "CARTERA"],
  CUMPLIR_ACUERDO_PAGO: ["Acuerdo cumplido", "CARTERA"],
  REACTIVAR_COBRO_COERCITIVO: ["Reactivación de cobro coercitivo", "CARTERA"],
  MARCAR_CANDIDATO_SUSPENSION: ["Candidato a suspensión", "CARTERA"],
  EXPORTAR_REPORTE_CARTERA: ["Exportación del reporte de cartera", "CARTERA"],
};

/** "CAMBIAR_ROL" → "Cambio de rol". Una acción nueva del backend se muestra con su código legible. */
export function presentarAccion(codigo) {
  const conocida = ACCIONES[codigo];
  if (conocida) return conocida[0];
  const texto = codigo.toLowerCase().replaceAll("_", " ");
  return texto[0].toUpperCase() + texto.slice(1);
}

/** Acciones del catálogo que pertenecen al módulo elegido (todas si no hay módulo). */
export const accionesDelModulo = (acciones, modulo) =>
  modulo ? acciones.filter((codigo) => !ACCIONES[codigo] || ACCIONES[codigo][1] === modulo) : acciones;

/** Nombre legible de la entidad afectada por la operación. */
const ENTIDADES = {
  funcionario: "Funcionario",
  cliente: "Usuario del servicio",
  predio: "Predio",
  contrato: "Contrato",
  factura: "Factura",
  lote_facturacion: "Lote de facturación",
  pqr: "PQR",
  acuerdo_pago: "Acuerdo de pago",
  reporte: "Reporte de cartera",
};

export const presentarEntidad = (entidad) => ENTIDADES[entidad] ?? entidad;

/** Pantalla del back-office donde se ve la entidad, si existe. */
export function rutaEntidad(entidad, id) {
  if (!id) return null;
  const rutas = {
    funcionario: `/gestion/administracion/funcionarios/${id}`,
    cliente: `/gestion/usuarios/${id}`,
    contrato: `/gestion/contratos/${id}`,
    factura: `/gestion/facturacion/facturas/${id}`,
    lote_facturacion: `/gestion/facturacion/lotes/${id}`,
    pqr: `/gestion/pqr/${id}`,
    acuerdo_pago: `/gestion/cartera/acuerdos/${id}`,
  };
  return rutas[entidad] ?? null;
}