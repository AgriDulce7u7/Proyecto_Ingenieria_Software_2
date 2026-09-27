import { useEffect } from "react";

const SUFIJO = "EPQ";

/** Actualiza el título de la pestaña del navegador con el nombre de la pantalla actual. */
export function useTituloPagina(titulo) {
  useEffect(() => {
    document.title = titulo ? `${titulo} - ${SUFIJO}` : SUFIJO;
  }, [titulo]);
}
