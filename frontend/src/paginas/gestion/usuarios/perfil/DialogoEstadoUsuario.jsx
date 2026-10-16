import { useState } from "react";
import { MensajeError } from "../../../../componentes/MensajeError";
import { Boton } from "../../../../componentes/ui/Boton";
import { Campo } from "../../../../componentes/ui/Campo";
import { useDesactivarUsuario, useReactivarUsuario } from "../../../../hooks/useUsuarios";
import { LIMITES } from "../validacionUsuario";

/**
 * Desactivación lógica o reactivación del usuario, con motivo obligatorio que queda en la auditoría.
 * Un usuario con contratos sin cancelar no se puede desactivar (`bloqueado`), igual que en el backend.
 */
export function DialogoEstadoUsuario({ usuario, accion, bloqueado, onListo, onCancelar }) {
  const desactivacion = useDesactivarUsuario(usuario.codigoInterno);
  const reactivacion = useReactivarUsuario(usuario.codigoInterno);
  const mutacion = accion === "reactivar" ? reactivacion : desactivacion;
  const [motivo, setMotivo] = useState("");
  const [error, setError] = useState("");

  if (bloqueado) {
    return (
      <>
        <div className="aviso aviso-error" role="alert">
          <p>
            El usuario tiene contratos que no están cancelados. Cancélelos desde el detalle de cada contrato antes de
            desactivarlo.
          </p>
        </div>
        <div className="dialogo-acciones">
          <Boton variante="secundario" onClick={onCancelar}>
            Entendido
          </Boton>
        </div>
      </>
    );
  }

  const enviar = (evento) => {
    evento.preventDefault();
    if (!motivo.trim()) {
      setError("Indique el motivo.");
      const formulario = evento.currentTarget;
      requestAnimationFrame(() => formulario.querySelector('[aria-invalid="true"]')?.focus());
      return;
    }
    mutacion.mutate(motivo.trim(), {
      onSuccess: () => onListo(accion === "reactivar" ? "El usuario quedó activo de nuevo." : "El usuario quedó inactivo."),
    });
  };

  return (
    <form className="dialogo-formulario" onSubmit={enviar} noValidate>
      <p>
        {accion === "reactivar"
          ? "El usuario podrá volver a tener predios y contratos."
          : "Es una eliminación lógica: el usuario y su historial se conservan, pero queda inactivo."}
      </p>
      <Campo
        etiqueta="Motivo"
        como="textarea"
        obligatorio
        rows={3}
        maxLength={LIMITES.motivo}
        value={motivo}
        error={error || mutacion.error?.errores?.motivo}
        ayuda="Queda en la auditoría con su usuario."
        onChange={(evento) => {
          setMotivo(evento.target.value);
          setError("");
        }}
      />
      {mutacion.error && !mutacion.error.esValidacion && <MensajeError error={mutacion.error} />}
      <div className="dialogo-acciones">
        <Boton variante="discreto" onClick={onCancelar}>
          Cancelar
        </Boton>
        <Boton type="submit" variante={accion === "reactivar" ? "principal" : "peligro"} disabled={mutacion.isPending}>
          {mutacion.isPending ? "Guardando…" : accion === "reactivar" ? "Reactivar" : "Desactivar"}
        </Boton>
      </div>
    </form>
  );
}