import { useState } from "react";
import { Link } from "react-router-dom";
import { Paginacion } from "../../../componentes/Paginacion";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { tonoEstado } from "../../../presentacion/estadosPqr";
import { formatearFechaCorta } from "../../../presentacion/fechas";
import { paginar } from "../../../presentacion/paginacion";
import { useSesion } from "../../../sesion/useSesion";

const TAMANO_PAGINA = 10;

/**
 * PQR del periodo que incumplieron el plazo: respondidas tarde o vencidas sin respuesta.
 * El radicado lleva al detalle solo si el rol puede atender PQR (el gerente y el directivo solo consultan).
 */
export function TablaIncumplimientos({ incumplimientos }) {
  const { puede } = useSesion();
  const [pagina, setPagina] = useState(1);
  const { visibles, paginaActual, totalPaginas, inicio } = paginar(incumplimientos, pagina, TAMANO_PAGINA);
  const conEnlace = puede("PQR_ATENDER");

  return (
    <section className="cumplimiento-incumplimientos" aria-labelledby="incumplimientos-titulo">
      <div className="cumplimiento-incumplimientos-titulo">
        <h2 id="incumplimientos-titulo">Solicitudes que incumplieron el plazo</h2>
        <span className="panel-nota">{incumplimientos.length} en el periodo</span>
      </div>

      {incumplimientos.length === 0 ? (
        <p className="cumplimiento-vacio cumplimiento-incumplimientos-vacio">
          Ninguna solicitud del periodo incumplió el plazo.
        </p>
      ) : (
        <>
          <div className="cumplimiento-tabla-contenedor">
            <table className="cumplimiento-tabla">
              <thead>
                <tr>
                  <th scope="col">Radicado</th>
                  <th scope="col">Tipo</th>
                  <th scope="col">Municipio</th>
                  <th scope="col">Gestor</th>
                  <th scope="col">Recepción</th>
                  <th scope="col">Fecha límite</th>
                  <th scope="col">Respuesta</th>
                  <th scope="col">Estado</th>
                </tr>
              </thead>
              <tbody>
                {visibles.map((fila) => (
                  <tr key={fila.radicado}>
                    <td>
                      {conEnlace ? (
                        <Link to={`/gestion/pqr/${fila.radicado}`} className="cumplimiento-radicado cifra">
                          {fila.radicado}
                        </Link>
                      ) : (
                        <strong className="cifra">{fila.radicado}</strong>
                      )}
                    </td>
                    <td data-etiqueta="Tipo">{fila.tipoSolicitud}</td>
                    <td data-etiqueta="Municipio">{fila.municipio ?? "Sin municipio"}</td>
                    <td data-etiqueta="Gestor">{fila.gestor ?? "Sin asignar"}</td>
                    <td data-etiqueta="Recepción">{formatearFechaCorta(fila.fechaRecepcion)}</td>
                    <td data-etiqueta="Fecha límite">{formatearFechaCorta(fila.fechaLimite)}</td>
                    <td data-etiqueta="Respuesta" className={fila.fechaRespuesta ? undefined : "cumplimiento-sin-respuesta"}>
                      {fila.fechaRespuesta ? formatearFechaCorta(fila.fechaRespuesta) : "Sin respuesta"}
                    </td>
                    <td>
                      <Etiqueta tono={tonoEstado(fila.estado)}>{fila.estado}</Etiqueta>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Paginacion
            total={incumplimientos.length}
            pagina={paginaActual}
            totalPaginas={totalPaginas}
            inicio={inicio}
            cantidadVisible={visibles.length}
            onCambiar={setPagina}
          />
        </>
      )}
    </section>
  );
}