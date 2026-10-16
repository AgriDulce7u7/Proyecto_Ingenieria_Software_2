import { useState } from "react";
import { MensajeError } from "../../../componentes/MensajeError";
import { enfocarPrimerError } from "../../../componentes/enfocarPrimerError";
import { Boton } from "../../../componentes/ui/Boton";
import { Campo } from "../../../componentes/ui/Campo";
import {
  useActualizarFuncionario,
  useCambiarEstadoFuncionario,
  useCambiarRol,
  useRestablecerContrasena,
  useRoles,
} from "../../../hooks/useAdministracion";
import { useMunicipios } from "../../../hooks/useTerritorio";
import { PATRON_CORREO } from "../../../presentacion/datosPersonales";
import { MENSAJE_CONTRASENA, PATRON_CONTRASENA } from "../../../presentacion/portal";
import { CampoContrasena } from "./CampoContrasena";
import { PermisosRol } from "./PermisosRol";
import { SelectorGestor } from "./SelectorGestor";

const MAXIMO_MOTIVO = 300;

/** Error del backend que no corresponde a un campo: va al final del diálogo. */
const errorGeneral = (error) => (error && !error.esValidacion ? error : null);

/** Botones del pie del diálogo. */
function Acciones({ onCancelar, pendiente, texto, textoPendiente = "Guardando…", variante = "principal" }) {
  return (
    <div className="dialogo-acciones">
      <Boton variante="discreto" onClick={onCancelar}>
        Cancelar
      </Boton>
      <Boton type="submit" variante={variante} disabled={pendiente}>
        {pendiente ? textoPendiente : texto}
      </Boton>
    </div>
  );
}

/** Motivo obligatorio del cambio, que queda en la auditoría con el usuario del administrador (RF-15). */
function CampoMotivo({ valor, error, onCambiar }) {
  return (
    <Campo
      etiqueta="Motivo"
      como="textarea"
      obligatorio
      rows={3}
      maxLength={MAXIMO_MOTIVO}
      value={valor}
      error={error}
      ayuda="Queda en la auditoría."
      contador={`${valor.length} / ${MAXIMO_MOTIVO}`}
      onChange={(evento) => onCambiar(evento.target.value)}
    />
  );
}

/** Nombre, correo y municipio. El usuario de acceso no se modifica. */
export function DialogoEditarFuncionario({ funcionario, onListo, onCancelar }) {
  const municipios = useMunicipios();
  const actualizacion = useActualizarFuncionario(funcionario.id);
  const [datos, setDatos] = useState({
    nombreCompleto: funcionario.nombreCompleto,
    correo: funcionario.correo,
    municipioId: funcionario.municipioId ? String(funcionario.municipioId) : "",
  });
  const [errores, setErrores] = useState({});
  const conflicto = actualizacion.error?.conflicto ? actualizacion.error.detalle : null;

  const propsCampo = (campo) => ({
    value: datos[campo],
    error: errores[campo] ?? actualizacion.error?.errores?.[campo] ?? (campo === "correo" ? conflicto : null),
    onChange: (evento) => {
      setDatos((anteriores) => ({ ...anteriores, [campo]: evento.target.value }));
      setErrores((anteriores) => ({ ...anteriores, [campo]: null }));
    },
  });

  const guardar = (evento) => {
    evento.preventDefault();
    const erroresCliente = {};
    if (!datos.nombreCompleto.trim()) erroresCliente.nombreCompleto = "Escriba el nombre completo.";
    if (!PATRON_CORREO.test(datos.correo.trim())) erroresCliente.correo = "Escriba un correo válido.";
    setErrores(erroresCliente);
    if (Object.keys(erroresCliente).length > 0) {
      enfocarPrimerError(evento.currentTarget);
      return;
    }
    actualizacion.mutate(
      {
        nombreCompleto: datos.nombreCompleto.trim(),
        correo: datos.correo.trim(),
        municipioId: datos.municipioId ? Number(datos.municipioId) : null,
      },
      { onSuccess: () => onListo("Se actualizaron los datos de la cuenta.") },
    );
  };

  return (
    <form className="dialogo-formulario" onSubmit={guardar} noValidate>
      <p className="admin-dialogo-nota">
        Usuario de acceso <strong className="cifra">{funcionario.usuario}</strong>: no se puede modificar.
      </p>
      <Campo etiqueta="Nombre completo" obligatorio maxLength={150} {...propsCampo("nombreCompleto")} />
      <Campo etiqueta="Correo institucional" obligatorio type="email" maxLength={150} {...propsCampo("correo")} />
      <Campo etiqueta="Municipio de la sede" como="select" disabled={municipios.isPending} {...propsCampo("municipioId")}>
        <option value="">Sin municipio</option>
        {municipios.data?.map((municipio) => (
          <option key={municipio.id} value={municipio.id}>
            {municipio.nombre}
          </option>
        ))}
      </Campo>
      <MensajeError error={conflicto ? null : errorGeneral(actualizacion.error)} />
      <Acciones onCancelar={onCancelar} pendiente={actualizacion.isPending} texto="Guardar cambios" />
    </form>
  );
}

/**
 * Cambio de rol (RF-14, RF-15). Si el nuevo rol es de la Oficina de PQR se elige el gestor vinculado.
 * El backend impide retirar el rol al único administrador activo.
 */
export function DialogoRol({ funcionario, onListo, onCancelar }) {
  const roles = useRoles();
  const cambio = useCambiarRol(funcionario.id);
  const [datos, setDatos] = useState({
    rol: funcionario.rol,
    gestorId: funcionario.gestorId ? String(funcionario.gestorId) : "",
    motivo: "",
  });
  const [errores, setErrores] = useState({});
  const internos = (roles.data ?? []).filter((rol) => rol.interno);
  const rolElegido = internos.find((rol) => rol.rol === datos.rol);
  const sinCambio = datos.rol === funcionario.rol && Number(datos.gestorId || 0) === Number(funcionario.gestorId || 0);

  const cambiar = (campo, valor) => {
    setDatos((anteriores) => ({ ...anteriores, [campo]: valor }));
    setErrores((anteriores) => ({ ...anteriores, [campo]: null }));
  };

  const guardar = (evento) => {
    evento.preventDefault();
    const erroresCliente = {};
    if (sinCambio) erroresCliente.rol = "Elija un rol distinto al actual.";
    if (rolElegido?.requiereGestorPqr && !datos.gestorId) erroresCliente.gestorId = "Seleccione el gestor de PQR.";
    if (!datos.motivo.trim()) erroresCliente.motivo = "Indique el motivo del cambio.";
    setErrores(erroresCliente);
    if (Object.keys(erroresCliente).length > 0) {
      enfocarPrimerError(evento.currentTarget);
      return;
    }
    cambio.mutate(
      {
        rol: datos.rol,
        gestorId: rolElegido?.requiereGestorPqr ? Number(datos.gestorId) : null,
        motivo: datos.motivo.trim(),
      },
      { onSuccess: (actualizado) => onListo(`El rol cambió a ${actualizado.descripcionRol}.`) },
    );
  };

  return (
    <form className="dialogo-formulario" onSubmit={guardar} noValidate>
      <p className="admin-dialogo-nota">
        Rol actual: <strong>{funcionario.descripcionRol}</strong>. El cambio aplica desde su próximo inicio de sesión.
      </p>
      <Campo
        etiqueta="Nuevo rol"
        como="select"
        obligatorio
        disabled={roles.isPending}
        value={datos.rol}
        error={errores.rol ?? cambio.error?.errores?.rol}
        onChange={(evento) => {
          cambiar("rol", evento.target.value);
          cambiar("gestorId", evento.target.value === funcionario.rol && funcionario.gestorId ? String(funcionario.gestorId) : "");
        }}
      >
        {internos.map((rol) => (
          <option key={rol.rol} value={rol.rol}>
            {rol.descripcion}
          </option>
        ))}
      </Campo>
      {rolElegido && !sinCambio && <PermisosRol rol={rolElegido} />}
      {rolElegido?.requiereGestorPqr && (
        <SelectorGestor
          valor={datos.gestorId}
          error={errores.gestorId}
          funcionarioId={funcionario.id}
          onCambiar={(valor) => cambiar("gestorId", valor)}
        />
      )}
      <CampoMotivo
        valor={datos.motivo}
        error={errores.motivo ?? cambio.error?.errores?.motivo}
        onCambiar={(valor) => cambiar("motivo", valor)}
      />
      <MensajeError error={errorGeneral(cambio.error)} />
      <Acciones onCancelar={onCancelar} pendiente={cambio.isPending} texto="Cambiar rol" />
    </form>
  );
}

/**
 * Activación o desactivación de la cuenta con motivo. Un administrador no puede desactivar su propia
 * cuenta ni dejar el sistema sin administradores activos: el backend lo rechaza y el motivo se muestra aquí.
 */
export function DialogoEstado({ funcionario, onListo, onCancelar }) {
  const cambio = useCambiarEstadoFuncionario(funcionario.id);
  const [motivo, setMotivo] = useState("");
  const [error, setError] = useState("");
  const activar = !funcionario.activo;

  const guardar = (evento) => {
    evento.preventDefault();
    if (!motivo.trim()) {
      setError("Indique el motivo.");
      enfocarPrimerError(evento.currentTarget);
      return;
    }
    cambio.mutate(
      { activo: activar, motivo: motivo.trim() },
      { onSuccess: () => onListo(activar ? "La cuenta quedó activa." : "La cuenta quedó inactiva.") },
    );
  };

  return (
    <form className="dialogo-formulario" onSubmit={guardar} noValidate>
      <p>
        {activar
          ? "El funcionario podrá volver a iniciar sesión con su contraseña."
          : "El funcionario no podrá iniciar sesión. Su historial y lo que registró se conservan."}
      </p>
      <CampoMotivo
        valor={motivo}
        error={error || cambio.error?.errores?.motivo}
        onCambiar={(valor) => {
          setMotivo(valor);
          setError("");
        }}
      />
      <MensajeError error={errorGeneral(cambio.error)} />
      <Acciones
        onCancelar={onCancelar}
        pendiente={cambio.isPending}
        texto={activar ? "Activar cuenta" : "Desactivar cuenta"}
        variante={activar ? "principal" : "peligro"}
      />
    </form>
  );
}

/** Restablecimiento de la contraseña; también quita el bloqueo por intentos fallidos. */
export function DialogoContrasena({ funcionario, onListo, onCancelar }) {
  const restablecimiento = useRestablecerContrasena(funcionario.id);
  const [contrasena, setContrasena] = useState("");
  const [error, setError] = useState("");

  const guardar = (evento) => {
    evento.preventDefault();
    if (!PATRON_CONTRASENA.test(contrasena)) {
      setError(MENSAJE_CONTRASENA);
      enfocarPrimerError(evento.currentTarget);
      return;
    }
    restablecimiento.mutate(contrasena, {
      onSuccess: () =>
        onListo(
          funcionario.bloqueado
            ? "Se restableció la contraseña y se desbloqueó la cuenta."
            : "Se restableció la contraseña.",
        ),
    });
  };

  return (
    <form className="dialogo-formulario" onSubmit={guardar} noValidate>
      {funcionario.bloqueado && (
        <div className="aviso aviso-info">
          <p>La cuenta está bloqueada por intentos fallidos: al restablecer la contraseña queda desbloqueada.</p>
        </div>
      )}
      <CampoContrasena
        etiqueta="Nueva contraseña"
        valor={contrasena}
        error={error || restablecimiento.error?.errores?.nuevaContrasena}
        onCambiar={(valor) => {
          setContrasena(valor);
          setError("");
        }}
      />
      <MensajeError error={errorGeneral(restablecimiento.error)} />
      <Acciones onCancelar={onCancelar} pendiente={restablecimiento.isPending} texto="Restablecer contraseña" />
    </form>
  );
}