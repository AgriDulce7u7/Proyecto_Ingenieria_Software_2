import { Icono } from "./Icono";
import "./ui.css";

/** Marca de SIGCA. En modo compacto solo muestra el nombre, sin la línea de la empresa. */
export function Logo({ compacto = false }) {
  return (
    <span className="logo">
      <span className="logo-marca">
        <Icono nombre="gota" tamano={22} />
      </span>
      <span className="logo-texto">
        <strong>SIGCA</strong>
        {!compacto && <small>Empresas Públicas del Quindío</small>}
      </span>
    </span>
  );
}