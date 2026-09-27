import { Icono } from "../../../componentes/ui/Icono";
import { formatearHora } from "../../../presentacion/fechas";

/** Resume lo que hizo el monitoreo, mencionando solo lo que ocurrió. */
function describir({ alertasEnviadas, pqrMarcadasVencidas }) {
  const novedades = [];
  if (alertasEnviadas > 0) {
    novedades.push(
      alertasEnviadas === 1 ? "se envió 1 alerta de vencimiento" : `se enviaron ${alertasEnviadas} alertas de vencimiento`,
    );
  }
  if (pqrMarcadasVencidas > 0) {
    novedades.push(
      pqrMarcadasVencidas === 1
        ? "se marcó 1 solicitud como vencida"
        : `se marcaron ${pqrMarcadasVencidas} solicitudes como vencidas`,
    );
  }
  if (novedades.length === 0) {
    return "No hay solicitudes nuevas próximas a vencer ni vencidas.";
  }
  const texto = novedades.join(" y ");
  return `${texto[0].toUpperCase()}${texto.slice(1)}.`;
}

/** Confirmación del monitoreo ejecutado a demanda, con lo que realmente hizo el backend. */
export function ResultadoMonitoreo({ resultado }) {
  return (
    <div className="monitoreo-resultado" role="status">
      <span className="monitoreo-resultado-icono" aria-hidden="true">
        <Icono nombre="check" />
      </span>
      <div>
        <strong>Monitoreo completado</strong>
        <p>{describir(resultado)}</p>
      </div>
      <span className="monitoreo-resultado-hora">{formatearHora(resultado.fechaEjecucion)}</span>
    </div>
  );
}