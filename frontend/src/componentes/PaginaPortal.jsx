import { useTituloPagina } from "../hooks/useTituloPagina";
import { EnlaceVolver } from "./ui/EnlaceVolver";
import "./pagina-portal.css";

/**
 * Estructura común de las páginas internas del portal ciudadano: enlace para volver,
 * antetítulo, título, descripción y contenido. También actualiza el título de la pestaña.
 */
export function PaginaPortal({ antetitulo, titulo, descripcion, volverA, textoVolver, ancho = "normal", children }) {
  useTituloPagina(titulo);

  return (
    <div className={`pagina-portal pagina-portal-${ancho}`}>
      <header className="pagina-portal-intro">
        {volverA && <EnlaceVolver a={volverA}>{textoVolver}</EnlaceVolver>}
        {antetitulo && <span className="antetitulo">{antetitulo}</span>}
        <h1>{titulo}</h1>
        {descripcion && <p>{descripcion}</p>}
      </header>
      {children}
    </div>
  );
}