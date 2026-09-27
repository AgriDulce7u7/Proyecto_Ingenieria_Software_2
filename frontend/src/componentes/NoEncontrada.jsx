import { useTituloPagina } from "../hooks/useTituloPagina";
import { Boton } from "./ui/Boton";

export function NoEncontrada() {
  useTituloPagina("Página no encontrada");
  return (
    <main className="pagina-simple">
      <h1>Esta página no existe</h1>
      <p>Revise la dirección o vuelva al inicio del portal.</p>
      <Boton a="/" icono="inicio">
        Ir al inicio
      </Boton>
    </main>
  );
}