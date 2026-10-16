import { useState } from "react";
import { MensajeError } from "../../../../componentes/MensajeError";
import { Boton } from "../../../../componentes/ui/Boton";
import { Campo } from "../../../../componentes/ui/Campo";
import { Icono } from "../../../../componentes/ui/Icono";
import { useEvidenciasPqr } from "../../../../hooks/usePqr";
import { enfocarPrimerError } from "../../../../componentes/enfocarPrimerError";
import { FormularioEvidencia } from "./FormularioEvidencia";

const MINIMO = 20;
const MAXIMO = 10000;

/** Estado del soporte documental: la respuesta exige al menos una evidencia (RN-06). */
function SoporteRespuesta({ radicado, evidencias }) {
  const cantidad = evidencias.data?.length ?? 0;
  if (evidencias.isPending) return null;
  if (cantidad > 0) {
    return (
      <p className="responder-soporte">
        <Icono nombre="check" tamano={16} />
        {cantidad === 1 ? "1 evidencia adjunta" : `${cantidad} evidencias adjuntas`} como soporte de la respuesta.
      </p>
    );
  }
  return (
    <div className="responder-sin-soporte">
      <p>
        <strong>Adjunte el soporte de la respuesta.</strong> Para responder debe haber al menos una evidencia.
      </p>
      <FormularioEvidencia radicado={radicado} />
    </div>
  );
}

/**
 * Respuesta formal al ciudadano (RN-06): obligatoria, entre 20 y 10.000 caracteres y con al menos
 * una evidencia adjunta. Se registra a nombre del gestor asignado (`gestorId`); si el usuario no puede
 * actuar como él, `gestorId` llega en null y se explica por qué.
 */
export function DialogoResponder({ pqr, gestorId, accion, onListo, onCancelar }) {
  const [respuesta, setRespuesta] = useState("");
  const [error, setError] = useState("");
  const evidencias = useEvidenciasPqr(pqr.radicado);
  const sinEvidencias = evidencias.isSuccess && evidencias.data.length === 0;

  const enviar = (evento) => {
    evento.preventDefault();
    const texto = respuesta.trim();
    if (texto.length < MINIMO) {
      setError(`Escriba una respuesta de al menos ${MINIMO} caracteres.`);
      enfocarPrimerError(evento.currentTarget);
      return;
    }
    accion.mutate(
      { gestorId, respuesta: texto },
      { onSuccess: () => onListo("Se registró la respuesta y se envió al ciudadano.") },
    );
  };

  if (gestorId === null) {
    return (
      <>
        <div className="aviso aviso-error" role="alert">
          <p>
            {pqr.gestor ? (
              <>
                Esta solicitud está asignada a <strong>{pqr.gestor.nombre}</strong>. Solo el gestor asignado puede
                responderla; si debe atenderla usted, pida al jefe de PQR que se la reasigne.
              </>
            ) : (
              "Esta solicitud aún no tiene gestor asignado. Tómela o asígnela antes de responder."
            )}
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
      <SoporteRespuesta radicado={pqr.radicado} evidencias={evidencias} />
      {accion.error && !accion.error.esValidacion && <MensajeError error={accion.error} />}
      <div className="dialogo-acciones">
        <Boton variante="discreto" onClick={onCancelar}>
          Cancelar
        </Boton>
        <Boton type="submit" disabled={accion.isPending || sinEvidencias}>
          {accion.isPending ? "Enviando…" : "Enviar respuesta"}
        </Boton>
      </div>
    </form>
  );
}