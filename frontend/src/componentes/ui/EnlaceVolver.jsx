import { Link, useLocation, useNavigate } from "react-router-dom";
import { Icono } from "./Icono";
import "./ui.css";

/**
 * Enlace para regresar a la pantalla anterior de un flujo.
 * Con `historial`, si se llegó navegando dentro de la aplicación, vuelve atrás en el historial
 * (así se conservan los filtros y la página del listado); si se abrió directo, va a `a`.
 */
export function EnlaceVolver({ a, historial = false, children }) {
  const navegar = useNavigate();
  const ubicacion = useLocation();
  const puedeVolver = historial && ubicacion.key !== "default";

  const alHacerClic = (evento) => {
    if (puedeVolver) {
      evento.preventDefault();
      navegar(-1);
    }
  };

  return (
    <Link to={a} className="enlace-volver" onClick={alHacerClic}>
      <Icono nombre="volver" tamano={18} />
      {children}
    </Link>
  );
}