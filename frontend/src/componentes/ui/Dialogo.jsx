import { useEffect, useId, useRef } from "react";
import { Icono } from "./Icono";
import "./ui.css";

/**
 * Ventana modal basada en el elemento nativo <dialog>: el navegador mantiene el foco dentro,
 * la cierra con Esc y devuelve el foco al elemento que la abrió.
 */
export function Dialogo({ abierto, onCerrar, antetitulo, titulo, children }) {
  const dialogo = useRef(null);
  const idTitulo = useId();

  useEffect(() => {
    const elemento = dialogo.current;
    if (abierto && !elemento.open) elemento.showModal();
    if (!abierto && elemento.open) elemento.close();
  }, [abierto]);

  // Clic en el fondo oscuro (fuera del contenido) también cierra
  const alHacerClic = (evento) => {
    if (evento.target === dialogo.current) onCerrar();
  };

  return (
    <dialog ref={dialogo} className="dialogo" aria-labelledby={idTitulo} onClose={onCerrar} onClick={alHacerClic}>
      <div className="dialogo-contenido">
        <div className="dialogo-cabecera">
          <div>
            {antetitulo && <span className="antetitulo">{antetitulo}</span>}
            <h2 id={idTitulo}>{titulo}</h2>
          </div>
          <button type="button" className="dialogo-cerrar" onClick={onCerrar} aria-label="Cerrar">
            <Icono nombre="cerrar" />
          </button>
        </div>
        {abierto && children}
      </div>
    </dialog>
  );
}