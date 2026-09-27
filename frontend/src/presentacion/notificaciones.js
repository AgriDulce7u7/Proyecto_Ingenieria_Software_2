/**
 * Presentación de las notificaciones que recibe un gestor. Las claves son los códigos
 * que envía la API en el campo "tipo" (ver TipoNotificacion en el backend).
 */

const TIPOS = {
  pqr_vencida: {
    etiqueta: "Solicitud vencida",
    tono: "peligro",
    icono: "alerta",
    gravedad: "critica",
    titulo: (radicado) => `${radicado} superó su fecha límite`,
  },
  vencimiento_48h: {
    etiqueta: "Próxima a vencer",
    tono: "advertencia",
    icono: "reloj",
    gravedad: "advertencia",
    titulo: (radicado) => `${radicado} vence en menos de 48 horas`,
  },
  asignacion_gestor: {
    etiqueta: "Asignación",
    tono: "info",
    icono: "personas",
    gravedad: "normal",
    titulo: (radicado) => `Se le asignó la solicitud ${radicado}`,
  },
};

const GENERICO = {
  etiqueta: "Notificación",
  tono: "neutro",
  icono: "documento",
  gravedad: "normal",
  titulo: (radicado) => `Novedad en la solicitud ${radicado}`,
};

/** { etiqueta, tono, icono, gravedad, titulo } de una notificación. */
export function presentarNotificacion(notificacion) {
  const tipo = TIPOS[notificacion.tipo] ?? GENERICO;
  return { ...tipo, titulo: tipo.titulo(notificacion.radicado) };
}