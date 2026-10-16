import { useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import { Cargando } from "../../../componentes/Cargando";
import { MensajeError } from "../../../componentes/MensajeError";
import { Boton } from "../../../componentes/ui/Boton";
import { Dialogo } from "../../../componentes/ui/Dialogo";
import { EnlaceVolver } from "../../../componentes/ui/EnlaceVolver";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { useAuditoria, useFuncionario, useRoles } from "../../../hooks/useAdministracion";
import { useGestores } from "../../../hooks/usePqr";
import { useTituloPagina } from "../../../hooks/useTituloPagina";
import { presentarAccion, presentarEstadoFuncionario } from "../../../presentacion/administracion";
import { formatearFechaHora } from "../../../presentacion/fechas";
import { useSesion } from "../../../sesion/useSesion";
import { DialogoContrasena, DialogoEditarFuncionario, DialogoEstado, DialogoRol } from "./DialogosFuncionario";
import { PermisosRol } from "./PermisosRol";
import "../pqr/detalle/detalle.css";
import "./administracion.css";

const CAMBIOS_VISIBLES = 8;

const TITULOS_DIALOGO = {
  editar: "Editar datos",
  rol: "Cambiar rol",
  estado: { activar: "Activar cuenta", desactivar: "Desactivar cuenta" },
  contrasena: "Restablecer contraseña",
};

/** Últimas operaciones del administrador sobre esta cuenta (creación, datos, rol, estado, contraseña). */
function CambiosCuenta({ funcionario }) {
  const auditoria = useAuditoria({
    entidad: "funcionario",
    entidadId: String(funcionario.id),
    tamano: CAMBIOS_VISIBLES,
  });
  const registros = auditoria.data?.contenido ?? [];
  const total = auditoria.data?.totalElementos ?? 0;

  return (
    <section className="panel" aria-labelledby="cambios-titulo">
      <div className="panel-titulo">
        <h2 id="cambios-titulo">Cambios en la cuenta</h2>
        {total > CAMBIOS_VISIBLES && (
          <Link
            to={`/gestion/administracion/auditoria?entidad=funcionario&entidadId=${funcionario.id}`}
            className="admin-enlace"
          >
            Ver los {total}
          </Link>
        )}
      </div>
      {auditoria.isPending && <Cargando texto="Cargando cambios…" />}
      <MensajeError error={auditoria.error} />
      {auditoria.isSuccess && registros.length === 0 && <p className="admin-vacio">Aún no hay cambios registrados.</p>}
      {registros.length > 0 && (
        <ol className="admin-cambios">
          {registros.map((registro) => (
            <li key={registro.id}>
              <div>
                <strong>{presentarAccion(registro.accion)}</strong>
                <small>
                  {formatearFechaHora(registro.fecha)} · <span className="cifra">{registro.usuario}</span>
                </small>
              </div>
              {registro.detalle && <p>{registro.detalle}</p>}
            </li>
          ))}
        </ol>
      )}
    </section>
  );
}

/** Rol de la cuenta con sus permisos y, en los roles de PQR, el gestor vinculado. */
function RolCuenta({ funcionario }) {
  const roles = useRoles();
  const gestores = useGestores();
  const rol = roles.data?.find((elemento) => elemento.rol === funcionario.rol);
  const gestor = gestores.data?.find((elemento) => elemento.id === funcionario.gestorId);

  return (
    <section className="panel" aria-labelledby="rol-titulo">
      <div className="panel-titulo">
        <h2 id="rol-titulo">{funcionario.descripcionRol}</h2>
        <span className="panel-nota cifra">{funcionario.rol}</span>
      </div>
      {rol ? <PermisosRol rol={rol} /> : roles.isPending && <Cargando texto="Cargando permisos…" />}
      {funcionario.gestorId && (
        <p className="admin-gestor">
          Gestor de PQR vinculado: <strong>{gestor ? gestor.nombre : `#${funcionario.gestorId}`}</strong>
        </p>
      )}
    </section>
  );
}

function ContenidoFuncionario({ funcionario, creado }) {
  const { usuario } = useSesion();
  const [dialogo, setDialogo] = useState(null);
  const [resultado, setResultado] = useState(null);
  const estado = presentarEstadoFuncionario(funcionario);
  const esPropia = usuario?.id === funcionario.id;
  const accionEstado = funcionario.activo ? "desactivar" : "activar";

  const cerrar = () => setDialogo(null);
  const terminar = (mensaje) => {
    cerrar();
    setResultado(mensaje);
  };
  const titulo = dialogo === "estado" ? TITULOS_DIALOGO.estado[accionEstado] : TITULOS_DIALOGO[dialogo];
  const propsDialogo = { funcionario, onListo: terminar, onCancelar: cerrar };

  return (
    <>
      <div className="detalle-encabezado">
        <div>
          <span className="antetitulo cifra">{funcionario.usuario}</span>
          <h1>{funcionario.nombreCompleto}</h1>
          <div className="detalle-etiquetas">
            <Etiqueta tono={estado.tono}>{estado.etiqueta}</Etiqueta>
            <span>{funcionario.descripcionRol}</span>
            {esPropia && <span>Su cuenta</span>}
          </div>
        </div>
        <div className="detalle-acciones">
          <Boton variante="secundario" onClick={() => setDialogo("editar")}>
            Editar datos
          </Boton>
          <Boton variante="secundario" onClick={() => setDialogo("rol")}>
            Cambiar rol
          </Boton>
          <Boton variante="secundario" icono="llave" onClick={() => setDialogo("contrasena")}>
            {funcionario.bloqueado ? "Desbloquear" : "Restablecer contraseña"}
          </Boton>
          {!esPropia && (
            <Boton variante={funcionario.activo ? "peligro" : "principal"} onClick={() => setDialogo("estado")}>
              {funcionario.activo ? "Desactivar" : "Activar"}
            </Boton>
          )}
        </div>
      </div>

      {creado && !resultado && (
        <div className="aviso aviso-exito detalle-resultado" role="status">
          <p>
            Se creó la cuenta <strong className="cifra">{funcionario.usuario}</strong>. Entregue la contraseña temporal
            al funcionario por un canal seguro.
          </p>
        </div>
      )}
      {resultado && (
        <div className="aviso aviso-exito detalle-resultado" role="status">
          <p>{resultado}</p>
        </div>
      )}
      {funcionario.activo && funcionario.bloqueado && (
        <div className="aviso aviso-error detalle-resultado">
          <p>
            La cuenta está bloqueada por intentos fallidos de inicio de sesión. Restablezca la contraseña para
            desbloquearla.
          </p>
        </div>
      )}
      {!funcionario.activo && (
        <div className="aviso aviso-info detalle-resultado">
          <p>La cuenta está inactiva: el funcionario no puede iniciar sesión. Su historial se conserva.</p>
        </div>
      )}
      {esPropia && (
        <div className="aviso aviso-info detalle-resultado">
          <p>Es su propia cuenta: no puede desactivarla. Si cambia su rol, el cambio aplica al volver a ingresar.</p>
        </div>
      )}

      <div className="detalle-rejilla admin-rejilla">
        <div className="detalle-principal">
          <RolCuenta funcionario={funcionario} />
          <CambiosCuenta funcionario={funcionario} />
        </div>
        <aside className="detalle-lateral">
          <section className="panel" aria-labelledby="cuenta-titulo">
            <h2 id="cuenta-titulo">Datos de la cuenta</h2>
            <dl className="detalle-datos">
              <div>
                <dt>Usuario de acceso</dt>
                <dd className="cifra">{funcionario.usuario}</dd>
              </div>
              <div>
                <dt>Correo</dt>
                <dd className="admin-correo">{funcionario.correo}</dd>
              </div>
              <div>
                <dt>Municipio de la sede</dt>
                <dd>{funcionario.municipio ?? "Sin municipio"}</dd>
              </div>
              <div>
                <dt>Último acceso</dt>
                <dd>{funcionario.ultimoAcceso ? formatearFechaHora(funcionario.ultimoAcceso) : "Nunca ha ingresado"}</dd>
              </div>
              <div>
                <dt>Cuenta creada</dt>
                <dd>{formatearFechaHora(funcionario.fechaCreacion)}</dd>
              </div>
            </dl>
            <Boton
              a={`/gestion/administracion/auditoria?usuario=${encodeURIComponent(funcionario.usuario)}`}
              variante="secundario"
              icono="historial"
              className="admin-boton-ancho"
            >
              Ver lo que ha hecho
            </Boton>
          </section>
        </aside>
      </div>

      <Dialogo abierto={dialogo !== null} onCerrar={cerrar} antetitulo={funcionario.usuario} titulo={titulo ?? ""}>
        {dialogo === "editar" && <DialogoEditarFuncionario {...propsDialogo} />}
        {dialogo === "rol" && <DialogoRol {...propsDialogo} />}
        {dialogo === "estado" && <DialogoEstado {...propsDialogo} />}
        {dialogo === "contrasena" && <DialogoContrasena {...propsDialogo} />}
      </Dialogo>
    </>
  );
}

/** Detalle de la cuenta de un funcionario (CU-10, RF-14): datos, rol, estado, contraseña y cambios auditados. */
export function DetalleFuncionario() {
  const { id } = useParams();
  const { state } = useLocation();
  const funcionario = useFuncionario(id);
  useTituloPagina(funcionario.data?.nombreCompleto ?? "Funcionario");

  return (
    <>
      <div className="detalle-volver">
        <EnlaceVolver a="/gestion/administracion/funcionarios" historial>
          Volver
        </EnlaceVolver>
      </div>
      {funcionario.isPending && <Cargando texto="Cargando la cuenta…" />}
      {funcionario.error?.noEncontrado && (
        <div className="aviso aviso-error" role="alert">
          <p>No existe el funcionario solicitado.</p>
        </div>
      )}
      {funcionario.error && !funcionario.error.noEncontrado && <MensajeError error={funcionario.error} />}
      {funcionario.data && (
        <ContenidoFuncionario key={id} funcionario={funcionario.data} creado={Boolean(state?.creado)} />
      )}
    </>
  );
}