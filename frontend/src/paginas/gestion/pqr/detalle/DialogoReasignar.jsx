import { useState } from "react";
import { MensajeError } from "../../../../componentes/MensajeError";
import { Boton } from "../../../../componentes/ui/Boton";
import { Campo } from "../../../../componentes/ui/Campo";
import { enfocarError } from "./enfocarError";
import { useGestores } from "../../../../hooks/usePqr";

/** Formulario para cambiar el gestor responsable. Queda auditado con el usuario y el motivo (SWR-09). */
export function DialogoReasignar({ pqr, usuario, accion, onListo, onCancelar }) {
  const { data: gestores = [] } = useGestores();
  const candidatos = gestores.filter((gestor) => gestor.id !== pqr.gestor?.id);
  const [gestorId, setGestorId] = useState("");
  const [motivo, setMotivo] = useState("");
  const [error, setError] = useState("");

  const enviar = (evento) => {
    evento.preventDefault();
    if (!gestorId) {
      setError("Seleccione el nuevo gestor.");
      enfocarError(evento.currentTarget);
      return;
    }
    const nuevo = candidatos.find((gestor) => gestor.id === Number(gestorId));
    accion.mutate(
      { gestorId: Number(gestorId), usuario, motivo: motivo.trim() || null },
      { onSuccess: () => onListo(`La solicitud se reasignó a ${nuevo.nombre}.`) },
    );
  };

  return (
    <form className="dialogo-formulario" onSubmit={enviar} noValidate>
      <Campo
        etiqueta="Nuevo gestor"
        como="select"
        obligatorio
        value={gestorId}
        error={error || accion.error?.errores?.gestorId}
        onChange={(evento) => {
          setGestorId(evento.target.value);
          setError("");
        }}
      >
        <option value="">Seleccione un gestor</option>
        {candidatos.map((gestor) => (
          <option key={gestor.id} value={gestor.id}>
            {gestor.nombre}
          </option>
        ))}
      </Campo>
      <Campo
        etiqueta="Motivo del cambio"
        placeholder="Indique brevemente el motivo (opcional)"
        maxLength={500}
        value={motivo}
        error={accion.error?.errores?.motivo}
        onChange={(evento) => setMotivo(evento.target.value)}
      />
      {accion.error && !accion.error.esValidacion && <MensajeError error={accion.error} />}
      <div className="dialogo-acciones">
        <Boton variante="discreto" onClick={onCancelar}>
          Cancelar
        </Boton>
        <Boton type="submit" disabled={accion.isPending}>
          {accion.isPending ? "Reasignando…" : "Confirmar reasignación"}
        </Boton>
      </div>
    </form>
  );
}