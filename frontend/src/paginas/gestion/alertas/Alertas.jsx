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
 * Alertas del usuario que inició sesión (RF-09, SWR-07, RN-04): asignaciones, avisos de 48 h y
 * vencimientos de su registro de gestor. El jefe de PQR y el administrador además pueden ejecutar
 * a demanda el monitoreo de plazos.
 */
export function Alertas() {
  const { usuario, puede } = useSesion();
  const gestorId = usuario.gestorId ?? null;
  const notificaciones = useNotificacionesGestor(gestorId);
  const monitoreo = useEjecutarMonitoreo();

  return (
    <>
      <EncabezadoGestion
        antetitulo={usuario.nombre}
        titulo="Alertas"
        descripcion="Eventos que requieren su atención."
        accion={
          puede("PQR_SUPERVISAR") && (
            <Boton icono="sincronizar" onClick={() => monitoreo.mutate()} disabled={monitoreo.isPending}>
              {monitoreo.isPending ? "Ejecutando…" : "Ejecutar monitoreo"}
            </Boton>
          )
        }
      />

      {monitoreo.data && <ResultadoMonitoreo resultado={monitoreo.data} />}
      <MensajeError error={monitoreo.error} />

      {gestorId === null && (
        <div className="alertas-vacio">
          <p>Su usuario no está registrado como gestor de PQR, así que no recibe alertas de solicitudes asignadas.</p>
        </div>
      )}

      {gestorId !== null && notificaciones.isPending && <Cargando texto="Cargando alertas…" />}
      <MensajeError error={notificaciones.error} />

      {notificaciones.data?.length === 0 && (
        <div className="alertas-vacio">
          <p>No tiene alertas. Aquí aparecerán las solicitudes asignadas, las próximas a vencer y las vencidas.</p>
        </div>
      )}

      {notificaciones.data?.length > 0 && (
        <ul className="alertas-lista" aria-label={`Alertas de ${usuario.nombre}`}>
          {notificaciones.data.map((notificacion) => (
            <FilaAlerta key={notificacion.id} notificacion={notificacion} />
          ))}
        </ul>
      )}
    </>
  );
}