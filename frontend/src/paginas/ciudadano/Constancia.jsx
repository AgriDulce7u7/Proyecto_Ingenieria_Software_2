import { useState } from "react";
import { useLocation } from "react-router-dom";
import { Boton } from "../../componentes/ui/Boton";
import { Etiqueta } from "../../componentes/ui/Etiqueta";
import { Icono } from "../../componentes/ui/Icono";
import { useTituloPagina } from "../../hooks/useTituloPagina";
import { tonoEstado } from "../../presentacion/estadosPqr";
import { formatearFecha, formatearFechaHora } from "../../presentacion/fechas";
import "./constancia.css";

/** Botón para copiar el radicado, con confirmación accesible. */
function CopiarRadicado({ radicado }) {
  const [copiado, setCopiado] = useState(false);

  const copiar = async () => {
    try {
      await navigator.clipboard.writeText(radicado);
      setCopiado(true);
      setTimeout(() => setCopiado(false), 2500);
    } catch {
      // Sin permiso para el portapapeles: el número sigue visible para copiarlo a mano.
    }
  };

  return (
    <>
      <Boton variante="discreto" icono={copiado ? "check" : "documento"} onClick={copiar}>
        {copiado ? "Copiado" : "Copiar número"}
      </Boton>
      <span className="solo-lectores" role="status">
        {copiado ? "Número de radicado copiado" : ""}
      </span>
    </>
  );
}

/** Se muestra si se entra a la constancia sin haber radicado en esta visita. */
function SinRadicacion() {
  return (
    <div className="constancia constancia-vacia">
      <h1>No hay una radicación reciente</h1>
      <p>
        La constancia se muestra justo después de radicar. Si ya tiene su número, consulte el estado de su
        solicitud.
      </p>
      <div className="constancia-acciones">
        <Boton a="/consultar" icono="buscar">
          Consultar estado
        </Boton>
        <Boton a="/radicar" variante="discreto">
          Radicar una PQR
        </Boton>
      </div>
    </div>
  );
}

/**
 * Constancia de radicación (RF-08, RN-04): número de radicado y plazos calculados por el backend.
 * Recibe la respuesta de POST /api/pqr por el estado de la navegación, que se conserva al recargar.
 */
export function Constancia() {
  useTituloPagina("Constancia de radicación");
  const radicacion = useLocation().state?.radicacion;

  if (!radicacion) return <SinRadicacion />;

  const datos = [
    { termino: "Tipo de solicitud", valor: radicacion.tipoSolicitud },
    {
      termino: "Estado inicial",
      valor: <Etiqueta tono={tonoEstado(radicacion.estado)}>{radicacion.estado}</Etiqueta>,
    },
    { termino: "Fecha de recepción", valor: formatearFechaHora(radicacion.fechaRecepcion) },
    { termino: "Fecha límite de respuesta", valor: formatearFecha(radicacion.fechaLimiteRespuesta) },
    { termino: "Respuesta estimada", valor: formatearFecha(radicacion.fechaEstimadaRespuesta) },
  ];

  return (
    <div className="constancia">
      <span className="constancia-exito" aria-hidden="true">
        <Icono nombre="check" tamano={32} />
      </span>
      <span className="antetitulo">Solicitud recibida</span>
      <h1>Su PQR fue radicada correctamente</h1>
      <p className="constancia-intro">
        Conserve el número de radicado para consultar el avance de su solicitud. También enviamos la constancia al
        correo registrado.
      </p>

      <div className="constancia-radicado">
        <span>Número de radicado</span>
        <strong className="cifra">{radicacion.radicado}</strong>
        <small>Este es el número que debe usar para hacer seguimiento.</small>
        <CopiarRadicado radicado={radicacion.radicado} />
      </div>

      <dl className="constancia-datos">
        {datos.map((dato) => (
          <div key={dato.termino}>
            <dt>{dato.termino}</dt>
            <dd>{dato.valor}</dd>
          </div>
        ))}
      </dl>

      <div className="constancia-nota">
        <Icono nombre="documento" tamano={18} />
        <p>
          Podrá consultar el estado en cualquier momento. No necesita crear una cuenta ni volver a ingresar sus datos
          personales.
        </p>
      </div>

      <div className="constancia-acciones">
        <Boton a={`/consultar?radicado=${radicacion.radicado}`} icono="buscar">
          Consultar estado de la PQR
        </Boton>
        <Boton a="/" variante="discreto">
          Volver al inicio
        </Boton>
      </div>
    </div>
  );
}