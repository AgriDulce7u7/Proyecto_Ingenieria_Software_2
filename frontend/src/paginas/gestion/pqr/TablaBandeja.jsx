import { Link, useNavigate } from "react-router-dom";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import { situacionPlazo, tonoEstado } from "../../../presentacion/estadosPqr";
import { formatearFechaLimite } from "../../../presentacion/fechas";

/** Tabla de PQR. En pantallas pequeñas cada fila se muestra como una tarjeta (ver bandeja.css). */
export function TablaBandeja({ pqrs }) {
  const navegar = useNavigate();

  // Toda la fila lleva al detalle; los enlaces internos ya navegan por sí mismos
  const abrirDesdeFila = (radicado) => (evento) => {
    if (!evento.target.closest("a")) navegar(`/gestion/pqr/${radicado}`);
  };

  return (
    <div className="bandeja-tabla-contenedor">
      <table className="bandeja-tabla">
        <thead>
          <tr>
            <th scope="col">Radicado</th>
            <th scope="col">Tipo / asunto</th>
            <th scope="col">Ciudadano</th>
            <th scope="col">Gestor</th>
            <th scope="col">Estado</th>
            <th scope="col">Fecha límite</th>
            <th scope="col">
              <span className="solo-lectores">Acciones</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {pqrs.map((pqr) => {
            const plazo = situacionPlazo(pqr);
            return (
              <tr key={pqr.radicado} className={`bandeja-fila-${plazo}`} onClick={abrirDesdeFila(pqr.radicado)}>
                <td>
                  <Link to={`/gestion/pqr/${pqr.radicado}`} className="bandeja-radicado cifra">
                    {pqr.radicado}
                  </Link>
                </td>
                <td>
                  <span className="bandeja-celda-doble">
                    <strong>{pqr.tipoSolicitud}</strong>
                    <small>{pqr.asunto}</small>
                  </span>
                </td>
                <td>{pqr.ciudadano}</td>
                <td className={pqr.gestor ? undefined : "bandeja-sin-asignar"}>{pqr.gestor ?? "Sin asignar"}</td>
                <td>
                  <Etiqueta tono={tonoEstado(pqr.estado)}>{pqr.estado}</Etiqueta>
                </td>
                <td>
                  <span className={`bandeja-plazo bandeja-plazo-${plazo}`}>
                    <i aria-hidden="true" />
                    {formatearFechaLimite(pqr.fechaLimiteRespuesta)}
                  </span>
                </td>
                <td>
                  <Link
                    to={`/gestion/pqr/${pqr.radicado}`}
                    className="bandeja-ver"
                    aria-label={`Ver detalle de ${pqr.radicado}`}
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