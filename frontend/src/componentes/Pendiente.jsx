import { useTituloPagina } from "../hooks/useTituloPagina";

/** Marcador temporal para las pantallas que se construyen en las siguientes etapas. */
export function Pendiente({ titulo, requisitos }) {
  useTituloPagina(titulo);
  return (
    <section>
      <h1>{titulo}</h1>
      <p className="texto-secundario">
        Pantalla en construcción. Requisitos que cubre: {requisitos}.
      </p>
    </section>
  );
}
