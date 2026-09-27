import { useTituloPagina } from "../hooks/useTituloPagina";
import { Boton } from "./ui/Boton";
import "./no-encontrada.css";

export function NoEncontrada() {
  useTituloPagina("Página no encontrada");
  return (
    <main className="pagina-no-encontrada">
      <span className="antetitulo">Error 404</span>
      <h1>Esta página no existe</h1>
      <p>Revise la dirección o vuelva al inicio del portal.</p>
      <Boton a="/" icono="inicio">
        Ir al inicio
      </Boton>
    </main>
  );
}