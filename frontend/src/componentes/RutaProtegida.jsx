import { Navigate, useLocation } from "react-router-dom";
import { useSesion } from "../sesion/useSesion";
import { AccesoDenegado } from "./AccesoDenegado";

/**
 * Exige una sesión de funcionario para mostrar `children`; si no la hay, lleva al login y
 * recuerda la página pedida para volver a ella. Con `capacidad`, además exige ese permiso.
 */
export function RutaProtegida({ capacidad, children }) {
  const { autenticado, esFuncionario, puede } = useSesion();
  const ubicacion = useLocation();

  if (!autenticado || !esFuncionario) {
    const desde = ubicacion.pathname + ubicacion.search;
    return <Navigate to="/acceso" replace state={{ desde }} />;
  }
  if (capacidad && !puede(capacidad)) {
    return <AccesoDenegado />;
  }
  return children;
}