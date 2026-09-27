import { Boton } from "./ui/Boton";

/** Controles de paginación de un listado. No se muestra si todo cabe en una página. */
export function Paginacion({ total, pagina, totalPaginas, inicio, cantidadVisible, onCambiar }) {
  if (totalPaginas <= 1) return null;

  return (
    <nav className="paginacion" aria-label="Paginación">
      <span>
        Mostrando {inicio + 1}–{inicio + cantidadVisible} de {total}
      </span>
      <div>
        <Boton variante="discreto" disabled={pagina === 1} onClick={() => onCambiar(pagina - 1)}>
          Anterior
        </Boton>
        <Boton variante="secundario" disabled={pagina === totalPaginas} onClick={() => onCambiar(pagina + 1)}>
          Siguiente
        </Boton>
      </div>
    </nav>
  );
}