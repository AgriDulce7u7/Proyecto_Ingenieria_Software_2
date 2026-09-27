import { Boton } from "../../../componentes/ui/Boton";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import { formatearFechaHora, formatearTiempoRelativo } from "../../../presentacion/fechas";
import { presentarNotificacion } from "../../../presentacion/notificaciones";

/** Una alerta del gestor: tipo, momento, mensaje enviado y acceso a la PQR. */
export function FilaAlerta({ notificacion }) {
  const alerta = presentarNotificacion(notificacion);

  return (
    <li className={`alerta alerta-${alerta.gravedad}`}>
      <span className="alerta-icono" aria-hidden="true">
        <Icono nombre={alerta.icono} />
      </span>
      <div className="alerta-contenido">
        <div className="alerta-cabecera">
          <Etiqueta tono={alerta.tono}>{alerta.etiqueta}</Etiqueta>
          <time dateTime={notificacion.fechaEnvio} title={formatearFechaHora(notificacion.fechaEnvio)}>
            {formatearTiempoRelativo(notificacion.fechaEnvio)}
          </time>
        </div>
        <h3>{alerta.titulo}</h3>
        <p>{notificacion.mensaje}</p>
        {!notificacion.enviado && <small className="alerta-sin-envio">El correo no se pudo enviar.</small>}
      </div>
      <Boton a={`/gestion/pqr/${notificacion.radicado}`} variante="discreto">
        Revisar
      </Boton>
    </li>
  );
}