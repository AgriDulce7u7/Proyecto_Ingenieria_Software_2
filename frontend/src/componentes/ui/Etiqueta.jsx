import "./ui.css";

/** Etiqueta de estado. Tonos: neutro, info, exito, advertencia, peligro. */
export function Etiqueta({ tono = "neutro", children }) {
  return <span className={`etiqueta etiqueta-${tono}`}>{children}</span>;
}