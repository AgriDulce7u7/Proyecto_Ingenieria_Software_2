import { useState } from "react";
import { MensajeError } from "../../../../componentes/MensajeError";
import { Boton } from "../../../../componentes/ui/Boton";
import { Campo } from "../../../../componentes/ui/Campo";
import { formatearMoneda } from "../../../../presentacion/facturacion";

const MINIMO = 10;
const MAXIMO = 500;

/**
 * Confirmación de la anulación (DE-01, RF-15). El motivo es obligatorio (10 a 500 caracteres) y queda
 * en la auditoría con el usuario que anuló. La anulación no se puede deshacer.
 */
export function DialogoAnularFactura({ factura, accion, onListo, onCancelar }) {
  const [motivo, setMotivo] = useState("");
  const [error, setError] = useState("");

  const enviar = (evento) => {
    evento.preventDefault();
    const texto = motivo.trim();
    if (texto.length < MINIMO) {
      setError(`Explique el motivo en al menos ${MINIMO} caracteres.`);
      const formulario = evento.currentTarget;
      requestAnimationFrame(() => formulario.querySelector('[aria-invalid="true"]')?.focus());
      return;
    }
    accion.mutate(texto, { onSuccess: () => onListo() });
  };

  return (
    <form className="dialogo-formulario" onSubmit={enviar} noValidate>
      <div className="aviso aviso-error">
        <p>
          La factura <strong className="cifra">{factura.numeroFactura}</strong> por{" "}
          <strong className="cifra">{formatearMoneda(factura.total)}</strong> dejará de cobrarse. Esta acción no se
          puede deshacer.
        </p>
      </div>
      <Campo
        etiqueta="Motivo de la anulación"
        como="textarea"
        obligatorio
        rows={4}
        maxLength={MAXIMO}
        placeholder="Por ejemplo: lectura del medidor registrada por error en la visita del periodo."
        value={motivo}
        error={error || accion.error?.errores?.motivo}
        ayuda="Queda en la auditoría con su usuario."
        contador={`${motivo.length} / ${MAXIMO}`}
        onChange={(evento) => {
          setMotivo(evento.target.value);
          setError("");
        }}
      />
      {accion.error && !accion.error.esValidacion && <MensajeError error={accion.error} />}
      <div className="dialogo-acciones">
        <Boton variante="discreto" onClick={onCancelar}>
          Cancelar
        </Boton>
        <Boton type="submit" variante="peligro" disabled={accion.isPending}>
          {accion.isPending ? "Anulando…" : "Anular factura"}
        </Boton>
      </div>
    </form>
  );
}