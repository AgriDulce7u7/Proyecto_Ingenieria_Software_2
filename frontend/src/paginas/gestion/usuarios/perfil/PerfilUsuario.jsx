import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { Cargando } from "../../../../componentes/Cargando";
import { MensajeError } from "../../../../componentes/MensajeError";
import { Boton } from "../../../../componentes/ui/Boton";
import { Dialogo } from "../../../../componentes/ui/Dialogo";
import { EnlaceVolver } from "../../../../componentes/ui/EnlaceVolver";
import { Etiqueta } from "../../../../componentes/ui/Etiqueta";
import { Icono } from "../../../../componentes/ui/Icono";
import { useRevalidarIdentidad, useUsuario } from "../../../../hooks/useUsuarios";
import { useTituloPagina } from "../../../../hooks/useTituloPagina";
import { formatearFecha, formatearFechaHora } from "../../../../presentacion/fechas";
import {
  nombreTipoPersona,
  nombreUsoPredio,
  presentarEstadoContrato,
  presentarValidacion,
  tieneContratosVigentes,
} from "../../../../presentacion/usuarios";
import { useSesion } from "../../../../sesion/useSesion";
import { DialogoEditarUsuario } from "./DialogoEditarUsuario";
import { DialogoEstadoUsuario } from "./DialogoEstadoUsuario";
import { DialogoPredio } from "./DialogoPredio";
import "../../pqr/detalle/detalle.css";
import "./perfil.css";

/** Lista de datos (término y valor), con el mismo estilo del detalle de PQR. */
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

/** Predios del usuario; el asesor puede registrar otro. */
function Predios({ usuario, puedeGestionar, onAgregar }) {
  return (
    <section className="panel" aria-labelledby="predios-titulo">
      <div className="panel-titulo">
        <h2 id="predios-titulo">Predios</h2>
        {puedeGestionar && usuario.activo && (
          <Boton variante="discreto" icono="mas" onClick={onAgregar}>
            Agregar predio
          </Boton>
        )}
      </div>
      {usuario.predios.length === 0 ? (
        <p className="perfil-vacio">El usuario no tiene predios registrados.</p>
      ) : (
        <ul className="perfil-lista">
          {usuario.predios.map((predio) => (
            <li key={predio.id}>
              <Icono nombre="inicio" />
              <div>
                <strong>{predio.direccion}</strong>
                <small>
                  <span className="cifra">{predio.codigoPredio}</span> · {predio.municipio} · Estrato {predio.estrato} ·{" "}
                  {nombreUsoPredio(predio.uso)}
                </small>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

/**
 * Contratos del usuario con su estado (SWR-20). Cada uno lleva a su detalle; el asesor crea uno nuevo
 * desde aquí cuando el usuario está activo y tiene predios (F-04).
 */
function Contratos({ usuario, puedeGestionar }) {
  const puedeCrear = puedeGestionar && usuario.activo && usuario.predios.length > 0;
  return (
    <section className="panel" aria-labelledby="contratos-titulo">
      <div className="panel-titulo">
        <h2 id="contratos-titulo">Contratos</h2>
        {puedeCrear ? (
          <Boton a={`/gestion/contratos/nuevo?usuario=${usuario.codigoInterno}`} variante="discreto" icono="mas">
            Nuevo contrato
          </Boton>
        ) : (
          <span className="panel-nota">{usuario.contratos.length} registrados</span>
        )}
      </div>
      {usuario.contratos.length === 0 ? (
        <p className="perfil-vacio">El usuario aún no tiene contratos de servicio.</p>
      ) : (
        <ul className="perfil-lista">
          {usuario.contratos.map((contrato) => {
            const estado = presentarEstadoContrato(contrato.estado);
            return (
              <li key={contrato.numeroContrato}>
                <Icono nombre="documento" />
                <div>
                  <Link to={`/gestion/contratos/${contrato.numeroContrato}`} className="perfil-enlace cifra">
                    {contrato.numeroContrato}
                  </Link>
                  <small>
                    {contrato.servicio} · {contrato.direccionServicio}
                    {contrato.municipio && `, ${contrato.municipio}`}
                  </small>
                </div>
                <span className="perfil-lista-estado">
                  <Etiqueta tono={estado.tono}>{estado.etiqueta}</Etiqueta>
                  {contrato.candidatoSuspension && <small className="perfil-alerta">Candidato a suspensión</small>}
                </span>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}

/** Resultado de la validación de identidad contra la RNEC (IS-03, SWR-14), con reintento. */
function Identidad({ usuario, puedeGestionar, onResultado }) {
  const validacion = presentarValidacion(usuario.estadoValidacion);
  const revalidacion = useRevalidarIdentidad(usuario.codigoInterno);
  const validada = usuario.estadoValidacion === "VALIDADO";

  const revalidar = () =>
    revalidacion.mutate(undefined, {
      onSuccess: (perfil) =>
        onResultado(
          perfil.estadoValidacion === "VALIDADO"
            ? { tipo: "exito", mensaje: "La Registraduría confirmó la identidad del usuario." }
            : { tipo: "aviso", mensaje: `La identidad sigue sin validar: ${perfil.detalleValidacion}` },
        ),
      onError: (error) => onResultado({ tipo: "error", error }),
    });

  return (
    <section className={`panel perfil-identidad perfil-identidad-${validacion.tono}`} aria-labelledby="identidad-titulo">
      <h2 id="identidad-titulo">Validación de identidad</h2>
      <Etiqueta tono={validacion.tono}>{validacion.etiqueta}</Etiqueta>
      {usuario.detalleValidacion && <p>{usuario.detalleValidacion}</p>}
      {usuario.fechaValidacion && <small>Última consulta: {formatearFechaHora(usuario.fechaValidacion)}</small>}
      {puedeGestionar && !validada && (
        <Boton variante="secundario" icono="sincronizar" onClick={revalidar} disabled={revalidacion.isPending}>
          {revalidacion.isPending ? "Consultando la RNEC…" : "Validar de nuevo"}
        </Boton>
      )}
    </section>
  );
}

function ContenidoPerfil({ usuario }) {
  const { puede } = useSesion();
  const puedeGestionar = puede("COMERCIAL_GESTIONAR");
  const [dialogo, setDialogo] = useState(null);
  const [resultado, setResultado] = useState(null);
  const validacion = presentarValidacion(usuario.estadoValidacion);
  const vigentes = tieneContratosVigentes(usuario);

  const cerrar = () => setDialogo(null);
  const terminar = (mensaje) => {
    cerrar();
    setResultado({ tipo: "exito", mensaje });
  };

  return (
    <>
      <div className="detalle-encabezado">
        <div>
          <span className="antetitulo cifra">{usuario.codigoInterno}</span>
          <h1>{usuario.nombreCompleto}</h1>
          <div className="detalle-etiquetas">
            <Etiqueta tono={usuario.activo ? "exito" : "neutro"}>{usuario.activo ? "Activo" : "Inactivo"}</Etiqueta>
            <Etiqueta tono={validacion.tono}>{validacion.corta}</Etiqueta>
            <span className="cifra">
              {usuario.tipoDocumento} {usuario.numeroDocumento}
            </span>
            <span>{nombreTipoPersona(usuario.tipoPersona)}</span>
          </div>
        </div>
        {puedeGestionar && (
          <div className="detalle-acciones">
            {usuario.activo ? (
              <>
                <Boton variante="secundario" onClick={() => setDialogo("editar")}>
                  Editar datos
                </Boton>
                <Boton variante="peligro" onClick={() => setDialogo("desactivar")}>
                  Desactivar
                </Boton>
              </>
            ) : (
              <Boton onClick={() => setDialogo("reactivar")}>Reactivar usuario</Boton>
            )}
          </div>
        )}
      </div>

      {resultado?.tipo === "exito" && (
        <div className="aviso aviso-exito detalle-resultado" role="status">
          <p>{resultado.mensaje}</p>
        </div>
      )}
      {resultado?.tipo === "aviso" && (
        <div className="aviso aviso-error detalle-resultado" role="status">
          <p>{resultado.mensaje}</p>
        </div>
      )}
      {resultado?.tipo === "error" && (
        <div className="detalle-resultado">
          <MensajeError error={resultado.error} />
        </div>
      )}
      {!usuario.activo && (
        <div className="aviso aviso-info detalle-resultado">
          <p>El usuario está inactivo: se conserva su historial, pero no se le pueden registrar predios ni contratos.</p>
        </div>
      )}

      <div className="detalle-rejilla">
        <div className="detalle-principal">
          <section className="panel" aria-labelledby="contacto-titulo">
            <h2 id="contacto-titulo">Datos de contacto</h2>
            <Datos
              filas={[
                { termino: "Teléfono", valor: usuario.telefono },
                { termino: "Correo electrónico", valor: usuario.correo },
                { termino: "Dirección de notificación", valor: usuario.direccionNotificacion },
                { termino: "Municipio", valor: usuario.municipio },
              ]}
            />
          </section>
          <Predios usuario={usuario} puedeGestionar={puedeGestionar} onAgregar={() => setDialogo("predio")} />
          <Contratos usuario={usuario} puedeGestionar={puedeGestionar} />
        </div>

        <aside className="detalle-lateral">
          <Identidad usuario={usuario} puedeGestionar={puedeGestionar && usuario.activo} onResultado={setResultado} />
          <section className="panel" aria-labelledby="registro-titulo">
            <h2 id="registro-titulo">Registro</h2>
            <Datos
              filas={[
                { termino: "Registrado", valor: formatearFecha(usuario.fechaRegistro) },
                { termino: "Registrado por", valor: usuario.registradoPor },
                { termino: "Última actualización", valor: formatearFechaHora(usuario.fechaActualizacion) },
                { termino: "Portal ciudadano", valor: usuario.cuentaPortalActiva ? "Cuenta activa" : "Sin cuenta" },
              ]}
            />
          </section>
        </aside>
      </div>

      <Dialogo abierto={dialogo === "editar"} onCerrar={cerrar} antetitulo={usuario.codigoInterno} titulo="Editar datos">
        <DialogoEditarUsuario usuario={usuario} onListo={terminar} onCancelar={cerrar} />
      </Dialogo>
      <Dialogo
        abierto={dialogo === "desactivar" || dialogo === "reactivar"}
        onCerrar={cerrar}
        antetitulo={usuario.codigoInterno}
        titulo={dialogo === "reactivar" ? "Reactivar usuario" : "Desactivar usuario"}
      >
        <DialogoEstadoUsuario
          usuario={usuario}
          accion={dialogo}
          bloqueado={dialogo === "desactivar" && vigentes}
          onListo={terminar}
          onCancelar={cerrar}
        />
      </Dialogo>
      <Dialogo abierto={dialogo === "predio"} onCerrar={cerrar} antetitulo={usuario.nombreCompleto} titulo="Agregar predio">
        <DialogoPredio usuario={usuario} onListo={terminar} onCancelar={cerrar} />
      </Dialogo>
    </>
  );
}

/** Perfil del usuario del servicio (CU-01, RF-02, RF-03): datos, identidad, predios y contratos. */
export function PerfilUsuario() {
  const { codigo } = useParams();
  useTituloPagina(`Usuario ${codigo}`);
  const usuario = useUsuario(codigo);

  return (
    <>
      <div className="detalle-volver">
        <EnlaceVolver a="/gestion/usuarios" historial>
          Volver a usuarios
        </EnlaceVolver>
      </div>

      {usuario.isPending && <Cargando texto="Cargando el perfil…" />}
      {usuario.error?.noEncontrado && (
        <div className="aviso aviso-error" role="alert">
          <p>
            No existe un usuario con el código <strong className="cifra">{codigo}</strong>.
          </p>
        </div>
      )}
      {usuario.error && !usuario.error.noEncontrado && <MensajeError error={usuario.error} />}
      {usuario.data && <ContenidoPerfil key={codigo} usuario={usuario.data} />}
    </>
  );
}