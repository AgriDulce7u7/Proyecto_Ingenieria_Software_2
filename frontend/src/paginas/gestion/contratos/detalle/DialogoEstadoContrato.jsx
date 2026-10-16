import { useState } from "react";
import { MensajeError } from "../../../../componentes/MensajeError";
import { enfocarPrimerError } from "../../../../componentes/enfocarPrimerError";
import { Boton } from "../../../../componentes/ui/Boton";
import { Campo } from "../../../../componentes/ui/Campo";
import { useCambiarEstadoContrato } from "../../../../hooks/useContratos";
import { EFECTO_DEL_CAMBIO, accionHacia } from "../../../../presentacion/contratos";
import { presentarEstadoContrato } from "../../../../presentacion/usuarios";

const MAXIMO_MOTIVO = 500;

/**
 * Cambio de estado del contrato (SWR-20) con motivo obligatorio, que queda en el historial y en la
 * auditoría. Activar un contrato que exige medidor y no lo tiene se explica antes de enviar (SWR-19).
 */
export function DialogoEstadoContrato({ contrato, destino, onListo, onCancelar }) {
  const cambio = useCambiarEstadoContrato(contrato.numeroContrato);
  const [motivo, setMotivo] = useState("");
  const [error, setError] = useState("");
  const accion = accionHacia(destino, contrato.estado);
  const sinMedidor = destino === "ACTIVO" && contrato.requiereMedidor && !contrato.medidorActivo;

  if (sinMedidor) {
    return (
      <>
        <div className="aviso aviso-error" role="alert">
          <p>
            Los contratos de {contrato.servicio.toLowerCase()} requieren un medidor instalado para activarse. Use
            «Instalar medidor»: ahí mismo puede activar el contrato.
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
      setError("Indique el motivo del cambio.");
      enfocarPrimerError(evento.currentTarget);
      return;
    }
    cambio.mutate(
      { estado: destino, motivo: motivo.trim() },
      {
        onSuccess: (detalle) =>
          onListo(`El contrato quedó ${presentarEstadoContrato(detalle.estado).etiqueta.toLowerCase()}.`),
      },
    );
  };

  return (
    <form className="dialogo-formulario" onSubmit={enviar} noValidate>
      <p>{EFECTO_DEL_CAMBIO[destino]}</p>
      <Campo
        etiqueta="Motivo"
        como="textarea"
        obligatorio
        rows={3}
        maxLength={MAXIMO_MOTIVO}
        placeholder={destino === "SUSPENDIDO" ? "Por ejemplo: mora de tres facturas" : undefined}
        value={motivo}
        error={error || cambio.error?.errores?.motivo}
        ayuda="Queda en el historial del contrato con su usuario."
        contador={`${motivo.length} / ${MAXIMO_MOTIVO}`}
        onChange={(evento) => {
          setMotivo(evento.target.value);
          setError("");
        }}
      />
      {cambio.error && !cambio.error.esValidacion && <MensajeError error={cambio.error} />}
      <div className="dialogo-acciones">
        <Boton variante="discreto" onClick={onCancelar}>
          Volver
        </Boton>
        <Boton type="submit" variante={accion.variante === "secundario" ? "principal" : accion.variante} disabled={cambio.isPending}>
          {cambio.isPending ? "Guardando…" : accion.texto}
        </Boton>
      </div>
    </form>
  );
}