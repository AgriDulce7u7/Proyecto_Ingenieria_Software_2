import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Cargando } from "../../componentes/Cargando";
import { MensajeError } from "../../componentes/MensajeError";
import { PaginaPortal } from "../../componentes/PaginaPortal";
import { Boton } from "../../componentes/ui/Boton";
import { Campo } from "../../componentes/ui/Campo";
import { Etiqueta } from "../../componentes/ui/Etiqueta";
import { Icono } from "../../componentes/ui/Icono";
import { useConsultaPqr } from "../../hooks/usePqr";
import { ESTADO, pasosSeguimiento, tonoEstado } from "../../presentacion/estadosPqr";
import { formatearFecha } from "../../presentacion/fechas";
import { FORMATO_RADICADO, normalizarRadicado } from "./radicado";
import "./consultar.css";

/** Texto de apoyo de cada paso de la línea de tiempo, según la información pública disponible. */
function detallePaso(paso, solicitud) {
  if (paso.situacion === "pendiente") return "Pendiente";
  if (paso.vencido) return "Se superó la fecha límite de respuesta. Su solicitud sigue en atención.";

  switch (paso.nombre) {
    case ESTADO.RADICADO:
      return paso.situacion === "actual"
        ? `Recibida el ${formatearFecha(solicitud.fechaRecepcion)}. Pronto la atenderá un gestor.`
        : `Recibida el ${formatearFecha(solicitud.fechaRecepcion)}`;
    case ESTADO.EN_TRAMITE:
      return paso.situacion === "actual" ? "Un gestor está atendiendo su solicitud." : "Atendida por un gestor";
    case ESTADO.RESUELTO:
      return solicitud.fechaResolucion
        ? `Respondida el ${formatearFecha(solicitud.fechaResolucion)}. La respuesta se envió a su correo.`
        : "Respuesta enviada a su correo.";
    case ESTADO.CERRADO:
      return "Trámite finalizado.";
    default:
      return "";
  }
}

/** Resultado de la consulta: datos públicos y línea de tiempo del estado (sin datos personales). */
function ResultadoConsulta({ solicitud }) {
  const pasos = pasosSeguimiento(solicitud.estado);

  return (
    <section className="consulta-resultado" aria-labelledby="consulta-titulo" aria-live="polite">
      <div className="consulta-resultado-encabezado">
        <div>
          <span className="antetitulo cifra">{solicitud.radicado}</span>
          <h2 id="consulta-titulo">{solicitud.tipoSolicitud}</h2>
        </div>
        <Etiqueta tono={tonoEstado(solicitud.estado)}>{solicitud.estado}</Etiqueta>
      </div>

      <dl className="consulta-datos">
        <div>
          <dt>Fecha de recepción</dt>
          <dd>{formatearFecha(solicitud.fechaRecepcion)}</dd>
        </div>
        <div>
          <dt>Respuesta estimada</dt>
          <dd>{formatearFecha(solicitud.fechaEstimadaRespuesta)}</dd>
        </div>
        <div>
          <dt>Fecha de respuesta</dt>
          <dd>{solicitud.fechaResolucion ? formatearFecha(solicitud.fechaResolucion) : "Pendiente"}</dd>
        </div>
      </dl>

      <ol className="consulta-linea-tiempo" aria-label="Avance de la solicitud">
        {pasos.map((paso) => (
          <li
            key={paso.nombre}
            className={`consulta-paso consulta-paso-${paso.situacion} ${paso.vencido ? "consulta-paso-vencido" : ""}`}
            aria-current={paso.situacion === "actual" ? "step" : undefined}
          >
            <span className="consulta-paso-marca" aria-hidden="true">
              {paso.situacion === "completo" && <Icono nombre="check" tamano={13} />}
            </span>
            <div>
              <strong>{paso.nombre}</strong>
              <small>{detallePaso(paso, solicitud)}</small>
            </div>
          </li>
        ))}
      </ol>

      <div className="consulta-privacidad">
        <Icono nombre="personas" />
        <p>Por su seguridad, esta consulta no muestra sus datos personales ni el contenido de la solicitud.</p>
      </div>
    </section>
  );
}

/**
 * Consulta pública del estado de una PQR, sin iniciar sesión (RF-10, CU-07, SWR-08).
 * El radicado vive en la URL (?radicado=...): se puede compartir, recargar y volver atrás.
 */
export function ConsultarEstado() {
  const [parametros, setParametros] = useSearchParams();
  const radicadoConsultado = parametros.get("radicado") ?? "";
  const [radicado, setRadicado] = useState(radicadoConsultado);
  const [error, setError] = useState("");
  const consulta = useConsultaPqr(radicadoConsultado);

  const consultar = (evento) => {
    evento.preventDefault();
    const valor = normalizarRadicado(radicado);
    if (!FORMATO_RADICADO.test(valor)) {
      setError("Escriba el radicado completo, por ejemplo 202609-0001.");
      return;
    }
    setParametros({ radicado: valor });
  };

  return (
    <PaginaPortal
      ancho="estrecha"
      antetitulo="Seguimiento público"
      titulo="Consulte su solicitud"
      descripcion="Ingrese el número completo que aparece en la constancia de radicación."
    >
      <form className="consulta-busqueda" onSubmit={consultar} noValidate role="search">
        <Campo
          etiqueta="Número de radicado"
          className="cifra"
          placeholder="Ej. 202609-0001"
          autoComplete="off"
          inputMode="numeric"
          value={radicado}
          error={error}
          onChange={(evento) => {
            setRadicado(evento.target.value);
            setError("");
          }}
        />
        <Boton type="submit" icono="buscar" disabled={consulta.isFetching}>
          Consultar
        </Boton>
      </form>

      {consulta.isFetching && !consulta.data && <Cargando texto="Consultando su solicitud…" />}

      {consulta.error?.noEncontrado && (
        <div className="aviso aviso-error" role="alert">
          <p>
            No encontramos una solicitud con el radicado <strong className="cifra">{radicadoConsultado}</strong>.
            Verifique el número en su constancia de radicación.
          </p>
        </div>
      )}
      {consulta.error && !consulta.error.noEncontrado && <MensajeError error={consulta.error} />}

      {consulta.data && <ResultadoConsulta solicitud={consulta.data} />}
    </PaginaPortal>
  );
}