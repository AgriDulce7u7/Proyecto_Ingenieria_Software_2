import { Link } from "react-router-dom";
import { Icono } from "./Icono";
import "./ui.css";

/** Enlace para regresar a la pantalla anterior de un flujo (por ejemplo, al inicio o a la bandeja). */
export function EnlaceVolver({ a, children }) {
  return (
    <Link to={a} className="enlace-volver">
      <Icono nombre="volver" tamano={18} />
      {children}
    </Link>
  );
}