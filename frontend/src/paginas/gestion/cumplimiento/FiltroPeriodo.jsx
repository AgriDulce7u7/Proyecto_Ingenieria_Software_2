import { useState } from "react";
import { Boton } from "../../../componentes/ui/Boton";
import { periodosRapidos } from "../../../presentacion/supervisionPqr";

/**
 * Periodo del indicador: atajos (este mes, últimos 3 meses, este año) o un rango de fechas.
 * El rango se aplica al consultar, después de validar que la fecha inicial no sea posterior a la final.
 */
export function FiltroPeriodo({ desde, hasta, consultando, onAplicar }) {
  const [rango, setRango] = useState({ desde, hasta });
  const [error, setError] = useState("");
  const atajos = periodosRapidos();

  const consultar = (evento) => {
    evento.preventDefault();
    if (!rango.desde || !rango.hasta) {
      setError("Indique la fecha inicial y la final.");
      return;
    }
    if (rango.desde > rango.hasta) {
      setError("La fecha inicial no puede ser posterior a la final.");
      return;
    }
    setError("");
    onAplicar(rango);
  };

  const cambiar = (campo) => (evento) => {
    setRango((anterior) => ({ ...anterior, [campo]: evento.target.value }));
    setError("");
  };

  return (
    <form className="cumplimiento-periodo" onSubmit={consultar} noValidate aria-label="Periodo del indicador">
      <div className="cumplimiento-atajos" role="group" aria-label="Periodos rápidos">
        {atajos.map((atajo) => (
          <button
            key={atajo.codigo}
            type="button"
            className="cumplimiento-atajo"
            aria-pressed={desde === atajo.desde && hasta === atajo.hasta}
            onClick={() => onAplicar(atajo)}
          >
            {atajo.texto}
          </button>
        ))}
      </div>

      <div className="cumplimiento-rango">
        <label>
          <span>Desde</span>
          <input
            type="date"
            value={rango.desde}
            max={rango.hasta || undefined}
            aria-invalid={error ? true : undefined}
            aria-describedby={error ? "periodo-error" : undefined}
            onChange={cambiar("desde")}
          />
        </label>
        <label>
          <span>Hasta</span>
          <input
            type="date"
            value={rango.hasta}
            min={rango.desde || undefined}
            aria-invalid={error ? true : undefined}
            aria-describedby={error ? "periodo-error" : undefined}
            onChange={cambiar("hasta")}
          />
        </label>
        <Boton type="submit" variante="secundario" disabled={consultando}>
          {consultando ? "Consultando…" : "Consultar"}
        </Boton>
      </div>

      {error && (
        <p id="periodo-error" className="campo-error" role="alert">
          {error}
        </p>
      )}
    </form>
  );
}