import { Navigate, useLocation } from "react-router-dom";
import { useSesion } from "../sesion/useSesion";

/**
 * Exige la sesión de un ciudadano (portal de autogestión, RF-07). Sin ella lleva al ingreso y
 * recuerda la página pedida para volver a ella. Una sesión de funcionario no sirve aquí.
 */
export function RutaCiudadano({ children }) {
  const { autenticado, esCiudadano } = useSesion();
  const ubicacion = useLocation();

  if (!autenticado || !esCiudadano) {
    return <Navigate to="/ingresar" replace state={{ desde: ubicacion.pathname + ubicacion.search }} />;
  }
  return children;
}