import { useState } from "react";
import { Boton } from "../../../componentes/ui/Boton";
import { Campo } from "../../../componentes/ui/Campo";
import { Icono } from "../../../componentes/ui/Icono";
import { generarContrasena } from "../../../presentacion/administracion";
import { reglasContrasena } from "../../../presentacion/portal";
import "./administracion.css";

/**
 * Contraseña temporal que el administrador entrega al funcionario: se puede ver, generar al azar y copiar.
 * Las reglas de la política (8 a 64 caracteres, mayúscula, minúscula y número) se marcan mientras se escribe.
 */
export function CampoContrasena({ etiqueta, valor, error, onCambiar }) {
  const [visible, setVisible] = useState(false);
  const [copiada, setCopiada] = useState(false);

  const generar = () => {
    onCambiar(generarContrasena());
    setVisible(true);
    setCopiada(false);
  };

  const copiar = async () => {
    try {
      await navigator.clipboard.writeText(valor);
      setCopiada(true);
    } catch {
      setCopiada(false);
    }
  };

  return (
    <div className="admin-contrasena formulario-campo-completo">
      <Campo
        etiqueta={etiqueta}
        obligatorio
        type={visible ? "text" : "password"}
        className="cifra"
        autoComplete="new-password"
        spellCheck={false}
        maxLength={64}
        value={valor}
        error={error}
        ayuda="Entréguela al funcionario por un canal seguro."
        onChange={(evento) => {
          onCambiar(evento.target.value);
          setCopiada(false);
        }}
        accesorio={
          <button
            type="button"
            onClick={() => setVisible((anterior) => !anterior)}
            aria-label={visible ? "Ocultar contraseña" : "Mostrar contraseña"}
            aria-pressed={visible}
          >
            <Icono nombre="ojo" tamano={19} />
          </button>
        }
      />
      <div className="admin-contrasena-acciones">
        <Boton variante="secundario" icono="llave" onClick={generar}>
          Generar contraseña
        </Boton>
        {valor && (
          <Boton variante="discreto" onClick={copiar}>
            {copiada ? "Copiada" : "Copiar"}
          </Boton>
        )}
      </div>
      <ul className="admin-reglas" aria-label="Requisitos de la contraseña">
        {reglasContrasena(valor).map((regla) => (
          <li key={regla.texto} data-cumple={regla.cumple}>
            <Icono nombre={regla.cumple ? "check" : "cerrar"} tamano={14} />
            {regla.texto}
          </li>
        ))}
      </ul>
    </div>
  );
}