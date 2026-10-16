import { useTituloPagina } from "../hooks/useTituloPagina";
import { useSesion } from "../sesion/useSesion";
import { Boton } from "./ui/Boton";
import { Icono } from "./ui/Icono";

/** Se muestra cuando el rol del usuario no permite abrir una pantalla (RNF-05). */
export function AccesoDenegado() {
  useTituloPagina("Acceso no permitido");
  const { usuario } = useSesion();

  return (
    <section className="panel acceso-denegado" aria-labelledby="acceso-denegado-titulo">
      <span className="acceso-denegado-icono" aria-hidden="true">
        <Icono nombre="candado" tamano={26} />
      </span>
      <h1 id="acceso-denegado-titulo">No tiene acceso a esta sección</h1>
      <p>
        Su rol, <strong>{usuario?.descripcionRol}</strong>, no incluye este permiso. Si lo necesita, solicítelo al
        administrador del sistema.
      </p>
      <Boton a="/gestion" variante="secundario" icono="volver">
        Volver al inicio
      </Boton>
    </section>
  );
}