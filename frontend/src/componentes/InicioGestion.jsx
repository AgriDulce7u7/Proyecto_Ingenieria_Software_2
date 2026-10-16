import { Navigate } from "react-router-dom";
import { useSesion } from "../sesion/useSesion";
import { AccesoDenegado } from "./AccesoDenegado";
import { menuPermitido } from "./menuGestion";

/** Inicio del back-office: lleva a la primera sección que permite el rol del usuario. */
export function InicioGestion() {
  const { puede } = useSesion();
  const [primera] = menuPermitido(puede);
  return primera ? <Navigate to={primera.a} replace /> : <AccesoDenegado />;
}