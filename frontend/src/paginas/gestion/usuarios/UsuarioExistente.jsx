import { useEffect, useRef } from "react";
import { Boton } from "../../../componentes/ui/Boton";

/**
 * Aviso de identificación duplicada (SWR-12, CU-01 4a). El 409 del backend trae el código del usuario
 * existente para ofrecer su perfil en lugar de registrarlo dos veces. El aviso queda arriba del
 * formulario, así que recibe el foco para que se vea y lo anuncien los lectores de pantalla.
 */
export function UsuarioExistente({ error }) {
  const codigo = error.propiedades.codigoUsuarioExistente;
  const aviso = useRef(null);
  useEffect(() => {
    aviso.current?.focus();
  }, [error]);

  return (
    <div ref={aviso} tabIndex={-1} className="aviso aviso-error usuarios-duplicado" role="alert">
      <p>
        <strong>{error.detalle}.</strong> No se puede registrar dos veces la misma identificación.
      </p>
      {codigo && (
        <Boton a={`/gestion/usuarios/${codigo}`} variante="secundario">
          Ver perfil {codigo}
        </Boton>
      )}
    </div>
  );
}