import { useTituloPagina } from "../hooks/useTituloPagina";

/** Encabezado de las pantallas del back-office: antetítulo, título, descripción y acción opcional. */
export function EncabezadoGestion({ antetitulo, titulo, descripcion, accion }) {
  useTituloPagina(titulo);

  return (
    <div className="gestion-titulo">
      <div>
        {antetitulo && <span className="antetitulo">{antetitulo}</span>}
        <h1>{titulo}</h1>
        {descripcion && <p>{descripcion}</p>}
      </div>
      {accion}
    </div>
  );
}