import { Link } from "react-router-dom";
import { Icono } from "./Icono";
import "./ui.css";

/**
 * Botón del sistema visual. Si recibe `a`, se renderiza como enlace de React Router
 * (navegar es un enlace, no un botón); si no, como <button>.
 */
export function Boton({
  children,
  variante = "principal",
  icono,
  a,
  type = "button",
  className = "",
  ...resto
}) {
  const clases = `boton boton-${variante} ${className}`.trim();
  const contenido = (
    <>
      {icono && <Icono nombre={icono} tamano={18} />}
      <span>{children}</span>
    </>
  );

  if (a) {
    return (
      <Link to={a} className={clases} {...resto}>
        {contenido}
      </Link>
    );
  }
  return (
    <button type={type} className={clases} {...resto}>
      {contenido}
    </button>
  );
}