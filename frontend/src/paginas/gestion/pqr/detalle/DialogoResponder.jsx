import { useState } from "react";
import { MensajeError } from "../../../../componentes/MensajeError";
import { Boton } from "../../../../componentes/ui/Boton";
import { Campo } from "../../../../componentes/ui/Campo";
import { enfocarError } from "./enfocarError";

const MINIMO = 20;
const MAXIMO = 10000;

/**
 * Respuesta formal al ciudadano (RN-06): obligatoria, entre 20 y 10.000 caracteres.
 * Solo la registra el gestor asignado; el backend lo valida y aquí se avisa antes de escribir.
 */
export function DialogoResponder({ pqr, gestor, accion, onListo, onCancelar }) {
  const [respuesta, setRespuesta] = useState("");
  const [error, setError] = useState("");
  const esAsignado = pqr.gestor?.id === gestor.id;

  const enviar = (evento) => {
    evento.preventDefault();
    const texto = respuesta.trim();
    if (texto.length < MINIMO) {
      setError(`Escriba una respuesta de al menos ${MINIMO} caracteres.`);
      enfocarError(evento.currentTarget);
      return;
    }
    accion.mutate(
      { gestorId: gestor.id, respuesta: texto },
      { onSuccess: () => onListo("Se registró la respuesta y se envió al ciudadano.") },
    );
  };

  if (!esAsignado) {
    return (
      <>
        <div className="aviso aviso-error" role="alert">
          <p>
            Esta solicitud está asignada a <strong>{pqr.gestor?.nombre ?? "otro gestor"}</strong>. Solo el gestor
            asignado puede responderla. Si debe atenderla usted, reasígnela primero.
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

  return (
    <form className="dialogo-formulario" onSubmit={enviar} noValidate>
      <Campo
        etiqueta="Respuesta al ciudadano"
        como="textarea"
        obligatorio
        rows={7}
        maxLength={MAXIMO}
        placeholder="Escriba una respuesta clara y completa..."
        value={respuesta}
        error={error || accion.error?.errores?.respuesta}
        ayuda="Se enviará al correo del ciudadano y quedará en el historial."
        contador={`${respuesta.length} / ${MAXIMO}`}
        onChange={(evento) => {
          setRespuesta(evento.target.value);
          setError("");
        }}
      />
      {accion.error && !accion.error.esValidacion && <MensajeError error={accion.error} />}
      <div className="dialogo-acciones">
        <Boton variante="discreto" onClick={onCancelar}>
          Cancelar
        </Boton>
        <Boton type="submit" disabled={accion.isPending}>
          {accion.isPending ? "Enviando…" : "Enviar respuesta"}
        </Boton>
      </div>
    </form>
  );
}