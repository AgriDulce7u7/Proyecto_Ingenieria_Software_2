import { useState } from "react";
import { MensajeError } from "../../../../componentes/MensajeError";
import { Boton } from "../../../../componentes/ui/Boton";
import { Campo } from "../../../../componentes/ui/Campo";

/** Cierre de una PQR resuelta, con observación opcional que queda en el historial. */
export function DialogoCerrar({ usuario, accion, onListo, onCancelar }) {
  const [observacion, setObservacion] = useState("");

  const enviar = (evento) => {
    evento.preventDefault();
    accion.mutate(
      { usuario, observacion: observacion.trim() || null },
      { onSuccess: () => onListo("La solicitud se cerró.") },
    );
  };

  return (
    <form className="dialogo-formulario" onSubmit={enviar} noValidate>
      <p>La solicitud ya tiene respuesta. Al cerrarla se da por terminado el trámite.</p>
      <Campo
        etiqueta="Observación"
        placeholder="Por ejemplo: ciudadano conforme con la respuesta (opcional)"
        maxLength={500}
        value={observacion}
        error={accion.error?.errores?.observacion}
        onChange={(evento) => setObservacion(evento.target.value)}
      />
      {accion.error && !accion.error.esValidacion && <MensajeError error={accion.error} />}
      <div className="dialogo-acciones">
        <Boton variante="discreto" onClick={onCancelar}>
          Cancelar
        </Boton>
        <Boton type="submit" disabled={accion.isPending}>
          {accion.isPending ? "Cerrando…" : "Cerrar solicitud"}
        </Boton>
      </div>
    </form>
  );
}