import { Link, useNavigate } from "react-router-dom";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import { formatearFechaLimite } from "../../../presentacion/fechas";
import { formatearHorasRestantes, presentarNivel } from "../../../presentacion/supervisionPqr";

/** Clase de la fila y del plazo según el nivel (reutiliza los estilos de la bandeja). */
const CLASE_PLAZO = { VENCIDA: "vencido", CRITICA: "proximo" };

/**
 * PQR pendientes del equipo. Cada fila lleva al detalle de la PQR, donde el jefe puede reasignarla.
 * En pantallas pequeñas cada fila se muestra como una tarjeta (ver bandeja.css).
 */
export function TablaAlertasEquipo({ alertas }) {
  const navegar = useNavigate();
  const abrirDesdeFila = (radicado) => (evento) => {
    if (!evento.target.closest("a")) navegar(`/gestion/pqr/${radicado}`);
  };

  return (
    <div className="bandeja-tabla-contenedor">
      <table className="bandeja-tabla supervision-tabla">
        <thead>
          <tr>
            <th scope="col">Radicado</th>
            <th scope="col">Tipo / asunto</th>
            <th scope="col">Gestor</th>
            <th scope="col">Nivel</th>
            <th scope="col">Fecha límite</th>
            <th scope="col">
              <span className="solo-lectores">Acciones</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {alertas.map((alerta) => {
            const nivel = presentarNivel(alerta.nivel);
            const plazo = CLASE_PLAZO[alerta.nivel] ?? "en-plazo";
            return (
              <tr key={alerta.radicado} className={`bandeja-fila-${plazo}`} onClick={abrirDesdeFila(alerta.radicado)}>
                <td>
                  <Link to={`/gestion/pqr/${alerta.radicado}`} className="bandeja-radicado cifra">
                    {alerta.radicado}
                  </Link>
                </td>
                <td>
                  <span className="bandeja-celda-doble">
                    <strong>{alerta.tipoSolicitud}</strong>
                    <small>{alerta.asunto}</small>
                  </span>
                </td>
                <td>
                  <span className="bandeja-celda-doble">
                    <strong className={alerta.gestor ? undefined : "bandeja-sin-asignar"}>
                      {alerta.gestor ?? "Sin asignar"}
                    </strong>
                    {alerta.municipio && <small>{alerta.municipio}</small>}
                  </span>
                </td>
                <td>
                  <span className="supervision-nivel">
                    <Etiqueta tono={nivel.tono}>{nivel.etiqueta}</Etiqueta>
                    {alerta.alertaEnviada && (
                      <span className="supervision-aviso" title="Se envió la alerta de 48 horas al gestor">
                        <Icono nombre="campana" tamano={14} />
                        <span className="solo-lectores">Alerta enviada al gestor</span>
                      </span>
                    )}
                  </span>
                </td>
                <td>
                  <span className="bandeja-celda-doble">
                    <span className={`bandeja-plazo bandeja-plazo-${plazo}`}>
                      <i aria-hidden="true" />
                      {formatearFechaLimite(alerta.fechaLimiteRespuesta)}
                    </span>
                    <small>{formatearHorasRestantes(alerta.horasRestantes)}</small>
                  </span>
                </td>
                <td>
                  <Link
                    to={`/gestion/pqr/${alerta.radicado}`}
                    className="bandeja-ver"
                    aria-label={`Ver detalle de ${alerta.radicado}`}
                  >
                    <Icono nombre="chevron" tamano={18} />
                  </Link>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}