import { Etiqueta } from "../../../../componentes/ui/Etiqueta";
import { useNotificacionesPqr } from "../../../../hooks/usePqr";
import { formatearFechaHora } from "../../../../presentacion/fechas";
import { presentarNotificacion } from "../../../../presentacion/notificaciones";

/** Notificaciones que generó la PQR, al ciudadano y al gestor. */
export function NotificacionesPqr({ radicado }) {
  const notificaciones = useNotificacionesPqr(radicado);
  if (!notificaciones.data?.length) return null;

  return (
    <section className="panel" aria-labelledby="notificaciones-titulo">
      <h2 id="notificaciones-titulo">Notificaciones</h2>
      <ul className="detalle-notificaciones">
        {notificaciones.data.map((notificacion) => {
          const presentacion = presentarNotificacion(notificacion);
          return (
            <li key={notificacion.id}>
              <Etiqueta tono={presentacion.tono}>{presentacion.etiqueta}</Etiqueta>
              <span>Para: {notificacion.destinatario}</span>
              <small>
                {formatearFechaHora(notificacion.fechaEnvio)}
                {!notificacion.enviado && <strong>, no se pudo enviar</strong>}
              </small>
            </li>
          );
        })}
      </ul>
    </section>
  );
}