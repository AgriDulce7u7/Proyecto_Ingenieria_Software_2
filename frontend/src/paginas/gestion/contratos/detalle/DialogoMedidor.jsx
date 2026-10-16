import { useState } from "react";
import { MensajeError } from "../../../../componentes/MensajeError";
import { enfocarPrimerError } from "../../../../componentes/enfocarPrimerError";
import { Boton } from "../../../../componentes/ui/Boton";
import { Campo } from "../../../../componentes/ui/Campo";
import { useAsignarMedidor } from "../../../../hooks/useContratos";
import { PATRON_MEDIDOR } from "../../../../presentacion/contratos";

/**
 * Instalación o cambio del medidor (SWR-19). El medidor anterior queda retirado. Si el contrato está
 * pendiente, se puede activar en la misma operación.
 */
export function DialogoMedidor({ contrato, onListo, onCancelar }) {
  const asignacion = useAsignarMedidor(contrato.numeroContrato);
  const pendiente = contrato.estado === "PENDIENTE";
  const [datos, setDatos] = useState({ numeroSerie: "", fechaInstalacion: "", activarContrato: pendiente });
  const [error, setError] = useState("");

  const enviar = (evento) => {
    evento.preventDefault();
    const serie = datos.numeroSerie.trim();
    if (!PATRON_MEDIDOR.test(serie)) {
      setError(serie ? "Use entre 4 y 50 letras, números o guiones." : "Ingrese el número de serie del medidor.");
      enfocarPrimerError(evento.currentTarget);
      return;
    }
    asignacion.mutate(
      {
        numeroSerie: serie,
        fechaInstalacion: datos.fechaInstalacion || null,
        activarContrato: pendiente && datos.activarContrato,
      },
      {
        onSuccess: (detalle) =>
          onListo(
            detalle.estado === "ACTIVO" && pendiente
              ? `Se instaló el medidor ${serie} y el contrato quedó activo.`
              : `Se instaló el medidor ${serie}.`,
          ),
      },
    );
  };

  return (
    <form className="dialogo-formulario" onSubmit={enviar} noValidate>
      {contrato.medidorActivo && (
        <p>
          El medidor actual <strong className="cifra">{contrato.medidorActivo.numeroSerie}</strong> quedará retirado.
        </p>
      )}
      <Campo
        etiqueta="Número de serie"
        obligatorio
        maxLength={50}
        autoComplete="off"
        placeholder="MED-001234"
        value={datos.numeroSerie}
        error={error || asignacion.error?.errores?.numeroSerie}
        onChange={(evento) => {
          setDatos((anteriores) => ({ ...anteriores, numeroSerie: evento.target.value }));
          setError("");
        }}
      />
      <Campo
        etiqueta="Fecha de instalación"
        type="date"
        ayuda="Si se deja vacía se toma la fecha de hoy."
        value={datos.fechaInstalacion}
        onChange={(evento) => setDatos((anteriores) => ({ ...anteriores, fechaInstalacion: evento.target.value }))}
      />
      {pendiente && (
        <label className="contrato-casilla">
          <input
            type="checkbox"
            checked={datos.activarContrato}
            onChange={(evento) => setDatos((anteriores) => ({ ...anteriores, activarContrato: evento.target.checked }))}
          />
          <span>
            <strong>Activar el contrato</strong>
            <small>El contrato está pendiente; con el medidor instalado ya se puede activar.</small>
          </span>
        </label>
      )}
      {asignacion.error && !asignacion.error.esValidacion && <MensajeError error={asignacion.error} />}
      <div className="dialogo-acciones">
        <Boton variante="discreto" onClick={onCancelar}>
          Cancelar
        </Boton>
        <Boton type="submit" disabled={asignacion.isPending}>
          {asignacion.isPending ? "Guardando…" : "Guardar medidor"}
        </Boton>
      </div>
    </form>
  );
}