import { ESTADO, esPendiente, situacionPlazo } from "../../../presentacion/estadosPqr";
import { buscarEn } from "../../../presentacion/texto";

export const TAMANO_PAGINA = 10;

/** Filtros que resuelve el backend (GET /api/pqr). El texto libre se filtra en el cliente. */
export const aFiltrosApi = ({ estado, tipo, gestor }) => ({
  ...(estado && { estado }),
  ...(tipo && { tipo }),
  ...(gestor && { gestorId: gestor }),
});

/** Búsqueda libre por radicado, asunto o ciudadano. */
export const buscar = (pqrs, texto) => buscarEn(pqrs, texto, ["radicado", "asunto", "ciudadano"]);

/** Indicadores de la bandeja, calculados sobre todas las PQR (sin filtros). */
export function calcularMetricas(pqrs) {
  const pendientes = pqrs.filter((pqr) => esPendiente(pqr.estado));
  return {
    pendientes: pendientes.length,
    sinAsignar: pendientes.filter((pqr) => !pqr.gestor).length,
    proximas: pqrs.filter((pqr) => situacionPlazo(pqr) === "proximo").length,
    vencidas: pqrs.filter((pqr) => pqr.estado === ESTADO.VENCIDO).length,
    resueltas: pqrs.filter((pqr) => !esPendiente(pqr.estado)).length,
  };
}