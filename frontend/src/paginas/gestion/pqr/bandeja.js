import { ESTADO, esPendiente, situacionPlazo } from "../../../presentacion/estadosPqr";

export const TAMANO_PAGINA = 10;

/** Filtros que resuelve el backend (GET /api/pqr). El texto libre se filtra en el cliente. */
export const aFiltrosApi = ({ estado, tipo, gestor }) => ({
  ...(estado && { estado }),
  ...(tipo && { tipo }),
  ...(gestor && { gestorId: gestor }),
});

/** Texto en minúsculas y sin tildes, para buscar "tramite" y encontrar "trámite". */
const normalizar = (texto = "") =>
  texto
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .toLowerCase();

/** Búsqueda libre por radicado, asunto o ciudadano. */
export function buscar(pqrs, texto) {
  const buscado = normalizar(texto.trim());
  if (!buscado) return pqrs;
  return pqrs.filter((pqr) =>
    [pqr.radicado, pqr.asunto, pqr.ciudadano].some((campo) => normalizar(campo).includes(buscado)),
  );
}

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