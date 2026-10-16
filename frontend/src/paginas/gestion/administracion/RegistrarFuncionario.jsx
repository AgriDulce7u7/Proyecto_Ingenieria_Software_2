import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { enfocarPrimerError } from "../../../componentes/enfocarPrimerError";
import { PieFormulario, SeccionFormulario } from "../../../componentes/SeccionFormulario";
import { Boton } from "../../../componentes/ui/Boton";
import { Campo } from "../../../componentes/ui/Campo";
import { EnlaceVolver } from "../../../componentes/ui/EnlaceVolver";
import { useCrearFuncionario, useRoles } from "../../../hooks/useAdministracion";
import { useMunicipios } from "../../../hooks/useTerritorio";
import { MENSAJE_USUARIO, PATRON_USUARIO } from "../../../presentacion/administracion";
import { PATRON_CORREO } from "../../../presentacion/datosPersonales";
import { MENSAJE_CONTRASENA, PATRON_CONTRASENA } from "../../../presentacion/portal";
import { CampoContrasena } from "./CampoContrasena";
import { PermisosRol } from "./PermisosRol";
import { SelectorGestor } from "./SelectorGestor";
import "./administracion.css";

const VACIO = {
  nombreCompleto: "",
  usuario: "",
  correo: "",
  municipioId: "",
  rol: "",
  gestorId: "",
  contrasenaTemporal: "",
};

function validar(datos, rolElegido) {
  const errores = {};
  if (!datos.nombreCompleto.trim()) errores.nombreCompleto = "Escriba el nombre completo.";
  if (!PATRON_USUARIO.test(datos.usuario.trim())) errores.usuario = MENSAJE_USUARIO;
  if (!PATRON_CORREO.test(datos.correo.trim())) errores.correo = "Escriba un correo válido.";
  if (!datos.rol) errores.rol = "Seleccione el rol.";
  if (rolElegido?.requiereGestorPqr && !datos.gestorId) errores.gestorId = "Seleccione el gestor de PQR.";
  if (!PATRON_CONTRASENA.test(datos.contrasenaTemporal)) errores.contrasenaTemporal = MENSAJE_CONTRASENA;
  return errores;
}

/** El 409 de un duplicado dice si el repetido es el usuario, el correo o el gestor: se muestra en su campo. */
function campoDelConflicto(error) {
  if (!error?.conflicto) return null;
  const detalle = error.detalle.toLowerCase();
  if (detalle.includes("usuario")) return "usuario";
  if (detalle.includes("correo")) return "correo";
  if (detalle.includes("gestor")) return "gestorId";
  return null;
}

/**
 * Creación de la cuenta de un funcionario (CU-10, RF-14). El rol define qué puede hacer en el sistema;
 * los roles de la Oficina de PQR se vinculan a un gestor. La contraseña es temporal y la operación queda
 * en la auditoría (RF-15).
 */
export function RegistrarFuncionario() {
  const navegar = useNavigate();
  const roles = useRoles();
  const municipios = useMunicipios();
  const creacion = useCrearFuncionario();
  const [datos, setDatos] = useState(VACIO);
  const [errores, setErrores] = useState({});

  const internos = (roles.data ?? []).filter((rol) => rol.interno);
  const rolElegido = internos.find((rol) => rol.rol === datos.rol);
  const campoConflicto = campoDelConflicto(creacion.error);

  const cambiar = (campo, valor) => {
    setDatos((anteriores) => ({ ...anteriores, [campo]: valor }));
    setErrores((anteriores) => ({ ...anteriores, [campo]: null }));
  };
  const propsCampo = (campo) => ({
    value: datos[campo],
    error: errores[campo] ?? creacion.error?.errores?.[campo] ?? (campoConflicto === campo ? creacion.error.detalle : null),
    onChange: (evento) => cambiar(campo, evento.target.value),
  });

  const crear = (evento) => {
    evento.preventDefault();
    const erroresCliente = validar(datos, rolElegido);
    setErrores(erroresCliente);
    if (Object.keys(erroresCliente).length > 0) {
      enfocarPrimerError(evento.currentTarget);
      return;
    }
    creacion.mutate(
      {
        nombreCompleto: datos.nombreCompleto.trim(),
        usuario: datos.usuario.trim(),
        correo: datos.correo.trim(),
        municipioId: datos.municipioId ? Number(datos.municipioId) : null,
        rol: datos.rol,
        gestorId: rolElegido?.requiereGestorPqr ? Number(datos.gestorId) : null,
        contrasenaTemporal: datos.contrasenaTemporal,
      },
      {
        onSuccess: (funcionario) =>
          navegar(`/gestion/administracion/funcionarios/${funcionario.id}`, { replace: true, state: { creado: true } }),
      },
    );
  };

  const errorGeneral = creacion.error && !creacion.error.esValidacion && !campoConflicto ? creacion.error : null;

  return (
    <>
      <div className="admin-volver">
        <EnlaceVolver a="/gestion/administracion/funcionarios" historial>
          Volver
        </EnlaceVolver>
      </div>
      <EncabezadoGestion
        antetitulo="Administración"
        titulo="Nuevo funcionario"
        descripcion="Cree la cuenta con la que el funcionario entra al back-office. Lo que puede hacer depende del rol."
      />

      <form className="formulario-gestion" onSubmit={crear} noValidate>
        <SeccionFormulario numero={1} titulo="Datos del funcionario">
          <Campo
            etiqueta="Nombre completo"
            obligatorio
            className="formulario-campo-completo"
            maxLength={150}
            autoComplete="off"
            {...propsCampo("nombreCompleto")}
          />
          <Campo
            etiqueta="Usuario de acceso"
            obligatorio
            className="cifra"
            maxLength={60}
            autoComplete="off"
            autoCapitalize="none"
            spellCheck={false}
            placeholder="laura.giraldo"
            ayuda="No se puede cambiar después."
            {...propsCampo("usuario")}
          />
          <Campo
            etiqueta="Correo institucional"
            obligatorio
            type="email"
            maxLength={150}
            autoComplete="off"
            {...propsCampo("correo")}
          />
          <Campo
            etiqueta="Municipio de la sede"
            como="select"
            disabled={municipios.isPending}
            ayuda="Opcional."
            {...propsCampo("municipioId")}
          >
            <option value="">Sin municipio</option>
            {municipios.data?.map((municipio) => (
              <option key={municipio.id} value={municipio.id}>
                {municipio.nombre}
              </option>
            ))}
          </Campo>
        </SeccionFormulario>

        <SeccionFormulario
          numero={2}
          titulo="Rol en el sistema"
          descripcion="Cada funcionario tiene un solo rol. Se puede cambiar después; el cambio queda en la auditoría."
        >
          <Campo
            etiqueta="Rol"
            como="select"
            obligatorio
            className="formulario-campo-completo"
            disabled={roles.isPending}
            {...propsCampo("rol")}
            onChange={(evento) => {
              cambiar("rol", evento.target.value);
              cambiar("gestorId", "");
            }}
          >
            <option value="">Seleccione el rol</option>
            {internos.map((rol) => (
              <option key={rol.rol} value={rol.rol}>
                {rol.descripcion}
              </option>
            ))}
          </Campo>
          {rolElegido && <PermisosRol rol={rolElegido} className="formulario-campo-completo" />}
          {rolElegido?.requiereGestorPqr && (
            <SelectorGestor
              valor={datos.gestorId}
              error={propsCampo("gestorId").error}
              onCambiar={(valor) => cambiar("gestorId", valor)}
            />
          )}
        </SeccionFormulario>

        <SeccionFormulario
          numero={3}
          titulo="Contraseña temporal"
          descripcion="Si el funcionario la olvida o se bloquea su cuenta, puede restablecerla desde su detalle."
        >
          <CampoContrasena
            etiqueta="Contraseña temporal"
            valor={datos.contrasenaTemporal}
            error={propsCampo("contrasenaTemporal").error}
            onCambiar={(valor) => cambiar("contrasenaTemporal", valor)}
          />
        </SeccionFormulario>

        <PieFormulario>
          <MensajeError error={errorGeneral} />
          <div className="formulario-pie-acciones">
            <Boton a="/gestion/administracion/funcionarios" variante="discreto">
              Cancelar
            </Boton>
            <Boton type="submit" icono="check" disabled={creacion.isPending}>
              {creacion.isPending ? "Creando…" : "Crear cuenta"}
            </Boton>
          </div>
        </PieFormulario>
      </form>
    </>
  );
}