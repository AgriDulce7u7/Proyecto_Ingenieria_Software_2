import { solicitar } from "./cliente";

/** Endpoints de F-08, dashboard gerencial (CU-09, RF-13, SWR-33 a SWR-35). */

/** Indicadores de las cuatro categorías con la tendencia de los últimos 12 meses. */
export const obtenerDashboard = () => solicitar("/dashboard");