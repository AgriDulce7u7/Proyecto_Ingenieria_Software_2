import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { Boton } from "../../../componentes/ui/Boton";
import { useEjecutarMonitoreo, useNotificacionesGestor } from "../../../hooks/usePqr";
import { useSesion } from "../../../sesion/useSesion";
import { FilaAlerta } from "./FilaAlerta";
import { ResultadoMonitoreo } from "./ResultadoMonitoreo";
import "./alertas.css";

/**
 * Alertas del gestor actual (RF-09, SWR-07, RN-04): asignaciones, avisos de 48 h y vencimientos,
 * y ejecución a demanda del monitoreo de plazos.
 */
export function Alertas() {
  const { gestor } = useSesion();
  const notificaciones = useNotificacionesGestor(gestor?.id);
  const monitoreo = useEjecutarMonitoreo();

  return (
    <>
      <EncabezadoGestion
        antetitulo={gestor?.nombre}
        titulo="Alertas"
        descripcion="Eventos que requieren su atención."
        accion={
          <Boton icono="sincronizar" onClick={() => monitoreo.mutate()} disabled={monitoreo.isPending}>
            {monitoreo.isPending ? "Ejecutando…" : "Ejecutar monitoreo"}
          </Boton>
        }
      />

      {monitoreo.data && <ResultadoMonitoreo resultado={monitoreo.data} />}
      <MensajeError error={monitoreo.error} />

      {!gestor && (
        <div className="alertas-vacio">
          <p>Seleccione su nombre en <strong>Gestor actual</strong>, en la parte superior, para ver sus alertas.</p>
        </div>
      )}

      {gestor && notificaciones.isPending && <Cargando texto="Cargando alertas…" />}
      {gestor && <MensajeError error={notificaciones.error} />}

      {notificaciones.data?.length === 0 && (
        <div className="alertas-vacio">
          <p>No tiene alertas. Aquí aparecerán las solicitudes asignadas, las próximas a vencer y las vencidas.</p>
        </div>
      )}

      {notificaciones.data?.length > 0 && (
        <ul className="alertas-lista" aria-label={`Alertas de ${gestor.nombre}`}>
          {notificaciones.data.map((notificacion) => (
            <FilaAlerta key={notificacion.id} notificacion={notificacion} />
          ))}
        </ul>
      )}
    </>
  );
}