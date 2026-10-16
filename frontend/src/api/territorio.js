import { solicitar } from "./cliente";

/** Catálogo de municipios del Quindío (público: lo usa también el portal ciudadano). */
export const obtenerMunicipios = () => solicitar("/municipios");