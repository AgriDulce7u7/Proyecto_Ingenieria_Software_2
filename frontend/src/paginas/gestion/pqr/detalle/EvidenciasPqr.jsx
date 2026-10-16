import { useState } from "react";
import { descargarEvidencia } from "../../../../api/pqr";
import { MensajeError } from "../../../../componentes/MensajeError";
import { Icono } from "../../../../componentes/ui/Icono";
import { useEvidenciasPqr } from "../../../../hooks/usePqr";
import { formatearTamano } from "../../../../presentacion/archivos";
import { ESTADO } from "../../../../presentacion/estadosPqr";
import { formatearFechaHora } from "../../../../presentacion/fechas";
import { FormularioEvidencia } from "./FormularioEvidencia";

/** Una evidencia con su botón de descarga. */
function FilaEvidencia({ radicado, evidencia }) {
  const [descargando, setDescargando] = useState(false);
  const [error, setError] = useState(null);

  const bajar = async () => {
    setDescargando(true);
    setError(null);
    try {
      await descargarEvidencia(radicado, evidencia);
    } catch (falla) {
      setError(falla);
    } finally {
      setDescargando(false);
    }
  };

  return (
    <li>
      <div className="evidencia-fila">
        <Icono nombre="documento" />
        <div className="evidencia-fila-datos">
          <strong>{evidencia.nombreArchivo}</strong>
          {evidencia.descripcion && <p>{evidencia.descripcion}</p>}
          <small>
            {formatearTamano(evidencia.tamanoBytes)} · {evidencia.usuario} · {formatearFechaHora(evidencia.fechaCarga)}
          </small>
        </div>
        <button
          type="button"
          className="evidencia-descargar"
          onClick={bajar}
          disabled={descargando}
          aria-label={`Descargar ${evidencia.nombreArchivo}`}
          title="Descargar"
        >
          <Icono nombre={descargando ? "sincronizar" : "descargar"} tamano={18} />
        </button>
      </div>
      <MensajeError error={error} />
    </li>
  );
}

/**
 * Soporte documental de la PQR (RN-06, RF-09): lista, descarga y carga de evidencias.
 * Una PQR cerrada ya no admite evidencias nuevas.
 */
export function EvidenciasPqr({ pqr }) {
  const evidencias = useEvidenciasPqr(pqr.radicado);
  const admiteNuevas = pqr.estado !== ESTADO.CERRADO;
  const lista = evidencias.data ?? [];

  return (
    <section className="panel" aria-labelledby="evidencias-titulo">
      <div className="panel-titulo">
        <h2 id="evidencias-titulo">Evidencias</h2>
        {lista.length > 0 && <span className="panel-nota">{lista.length} adjunta{lista.length > 1 ? "s" : ""}</span>}
      </div>

      <MensajeError error={evidencias.error} />
      {evidencias.isSuccess && lista.length === 0 && (
        <p className="evidencias-vacio">
          Aún no hay evidencias. Para responder la solicitud debe adjuntar al menos un soporte.
        </p>
      )}
      {lista.length > 0 && (
        <ul className="evidencias-lista">
          {lista.map((evidencia) => (
            <FilaEvidencia key={evidencia.id} radicado={pqr.radicado} evidencia={evidencia} />
          ))}
        </ul>
      )}

      {admiteNuevas && <FormularioEvidencia radicado={pqr.radicado} />}
    </section>
  );
}