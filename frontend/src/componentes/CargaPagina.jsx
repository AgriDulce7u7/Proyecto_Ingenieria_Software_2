import { Suspense } from "react";
import { Cargando } from "./Cargando";

/**
 * Límite de carga de las pantallas. Cada pantalla se descarga la primera vez que se abre (rutas.jsx);
 * mientras llega se muestra este aviso dentro del layout, sin perder el menú ni el encabezado.
 */
export function CargaPagina({ children }) {
  return (
    <Suspense
      fallback={
        <div className="carga-pagina">
          <Cargando texto="Cargando la pantalla…" />
        </div>
      }
    >
      {children}
    </Suspense>
  );
}