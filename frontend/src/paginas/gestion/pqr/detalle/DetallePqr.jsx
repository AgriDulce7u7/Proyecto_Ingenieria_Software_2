import { useState } from "react";
import { useParams } from "react-router-dom";
import { Cargando } from "../../../../componentes/Cargando";
import { MensajeError } from "../../../../componentes/MensajeError";
import { EnlaceVolver } from "../../../../componentes/ui/EnlaceVolver";
import { Etiqueta } from "../../../../componentes/ui/Etiqueta";
import { Icono } from "../../../../componentes/ui/Icono";
import { usePqrDetalle } from "../../../../hooks/usePqr";
import { useTituloPagina } from "../../../../hooks/useTituloPagina";
import { esPendiente, situacionPlazo, tonoEstado } from "../../../../presentacion/estadosPqr";
import { formatearFecha, formatearFechaHora, formatearFechaLimite } from "../../../../presentacion/fechas";
import { enmascararDocumento } from "../../../../presentacion/texto";
import { AccionesPqr } from "./AccionesPqr";
import { HistorialPqr } from "./HistorialPqr";
import { NotificacionesPqr } from "./NotificacionesPqr";
import "./detalle.css";

/** Lista de datos (término y valor) de los paneles laterales. */
function Datos({ filas }) {
  return (
    <dl className="detalle-datos">
      {filas
        .filter((fila) => fila.valor)
        .map((fila) => (
          <div key={fila.termino}>
            <dt>{fila.termino}</dt>
            <dd className={fila.clase}>{fila.valor}</dd>
          </div>
        ))}
    </dl>
  );
}

/** Aviso de la alerta de 48 horas al gestor (SWR-07) mientras la PQR está pendiente. */
function AvisoAlerta({ pqr }) {
  if (!esPendiente(pqr.estado)) return null;
  return (
    <div className="detalle-aviso-alerta">
      <Icono nombre="campana" />
      <div>
        <strong>{pqr.alertaVencimientoEnviada ? "Alerta de vencimiento enviada" : "Alerta de vencimiento activa"}</strong>
        <p>
          {pqr.alertaVencimientoEnviada
            ? "Se avisó al gestor que la solicitud vence en menos de 48 horas."
            : "Se avisará al gestor 48 horas antes de la fecha límite."}
        </p>
      </div>
    </div>
  );
}

function ContenidoDetalle({ pqr }) {
  const [resultado, setResultado] = useState(null);
  const plazo = situacionPlazo(pqr);

  return (
    <>
      <div className="detalle-encabezado">
        <div>
          <span className="antetitulo cifra">{pqr.radicado}</span>
          <h1>{pqr.asunto}</h1>
          <div className="detalle-etiquetas">
            <Etiqueta tono={tonoEstado(pqr.estado)}>{pqr.estado}</Etiqueta>
            <span>{pqr.tipoSolicitud}</span>
            {pqr.canal && <span>Canal {pqr.canal.toLowerCase()}</span>}
            {esPendiente(pqr.estado) && (
              <span className={`detalle-plazo detalle-plazo-${plazo}`}>
                <i aria-hidden="true" />
                {plazo === "vencido" ? "Venció" : "Vence"} {formatearFechaLimite(pqr.fechaLimiteRespuesta).toLowerCase()}
              </span>
            )}
          </div>
        </div>
        <AccionesPqr pqr={pqr} onResultado={setResultado} />
      </div>

      {resultado?.tipo === "exito" && (
        <div className="aviso aviso-exito detalle-resultado" role="status">
          <p>{resultado.mensaje}</p>
        </div>
      )}
      {resultado?.tipo === "error" && (
        <div className="detalle-resultado">
          <MensajeError error={resultado.error} />
        </div>
      )}

      <div className="detalle-rejilla">
        <div className="detalle-principal">
          <section className="panel" aria-labelledby="descripcion-titulo">
            <div className="panel-titulo">
              <h2 id="descripcion-titulo">Descripción de la solicitud</h2>
              <span className="panel-nota">Recibida el {formatearFecha(pqr.fechaRecepcion)}</span>
            </div>
            <p className="detalle-texto">{pqr.descripcion}</p>
          </section>

          {pqr.respuesta && (
            <section className="panel detalle-respuesta" aria-labelledby="respuesta-titulo">
              <div className="panel-titulo">
                <h2 id="respuesta-titulo">Respuesta al ciudadano</h2>
                <span className="panel-nota">Enviada el {formatearFecha(pqr.fechaResolucion)}</span>
              </div>
              <p className="detalle-texto">{pqr.respuesta}</p>
            </section>
          )}

          <HistorialPqr radicado={pqr.radicado} />
        </div>

        <aside className="detalle-lateral">
          <section className="panel" aria-labelledby="ciudadano-titulo">
            <h2 id="ciudadano-titulo">Ciudadano</h2>
            <Datos
              filas={[
                { termino: "Nombre", valor: pqr.ciudadano.nombreCompleto },
                {
                  termino: "Documento",
                  valor: `${pqr.ciudadano.tipoDocumento} ${enmascararDocumento(pqr.ciudadano.numeroDocumento)}`,
                },
                { termino: "Correo", valor: pqr.ciudadano.correo },
                { termino: "Teléfono", valor: pqr.ciudadano.telefono },
                { termino: "Dirección", valor: pqr.ciudadano.direccion },
              ]}
            />
          </section>

          <section className="panel" aria-labelledby="gestion-titulo">
            <h2 id="gestion-titulo">Gestión</h2>
            <Datos
              filas={[
                { termino: "Gestor asignado", valor: pqr.gestor?.nombre ?? "Sin asignar" },
                {
                  termino: "Fecha límite",
                  valor: formatearFechaHora(pqr.fechaLimiteRespuesta),
                  clase: esPendiente(pqr.estado) && plazo !== "en-plazo" ? `detalle-dato-${plazo}` : undefined,
                },
                { termino: "Respuesta estimada", valor: formatearFecha(pqr.fechaEstimadaRespuesta) },
              ]}
            />
          </section>

          <AvisoAlerta pqr={pqr} />
          <NotificacionesPqr radicado={pqr.radicado} />
        </aside>
      </div>
    </>
  );
}

/** Detalle de una PQR en el back-office (RF-09, CU-06, DE-02, RN-06, SWR-09). */
export function DetallePqr() {
  const { radicado } = useParams();
  useTituloPagina(`PQR ${radicado}`);
  const pqr = usePqrDetalle(radicado);

  return (
    <>
      <div className="detalle-volver">
        <EnlaceVolver a="/gestion/pqr" historial>
          Volver a la bandeja
        </EnlaceVolver>
      </div>

      {pqr.isPending && <Cargando texto="Cargando la solicitud…" />}
      {pqr.error?.noEncontrado && (
        <div className="aviso aviso-error" role="alert">
          <p>
            No existe una solicitud con el radicado <strong className="cifra">{radicado}</strong>.
          </p>
        </div>
      )}
      {pqr.error && !pqr.error.noEncontrado && <MensajeError error={pqr.error} />}
      {pqr.data && <ContenidoDetalle key={radicado} pqr={pqr.data} />}
    </>
  );
}