import { useState } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { iniciarSesionFuncionario } from "../../api/autenticacion";
import { Boton } from "../../componentes/ui/Boton";
import { Campo } from "../../componentes/ui/Campo";
import { Icono } from "../../componentes/ui/Icono";
import { Logo } from "../../componentes/ui/Logo";
import { useTituloPagina } from "../../hooks/useTituloPagina";
import { useSesion } from "../../sesion/useSesion";
import "./acceso.css";

/** Mensaje claro para cada fallo del login, sin revelar si el usuario existe. */
function mensajeDeError(error) {
  if (error.cuentaBloqueada) return error.detalle;
  if (error.noAutenticado) return "Usuario o contraseña incorrectos. Tras 3 intentos fallidos la cuenta se bloquea 15 minutos.";
  return error.detalle ?? error.message;
}

/**
 * Inicio de sesión de funcionarios (IS-SIS-01, RF-14). Tras ingresar vuelve a la página que se
 * intentaba abrir, o al inicio del back-office.
 */
export function IniciarSesion() {
  useTituloPagina("Acceso funcionarios");
  const navegar = useNavigate();
  const { state } = useLocation();
  const { autenticado, esFuncionario, motivoCierre, iniciarSesion } = useSesion();
  const [datos, setDatos] = useState({ usuario: "", contrasena: "" });
  const [errores, setErrores] = useState({});
  const [errorGeneral, setErrorGeneral] = useState(null);
  const [verContrasena, setVerContrasena] = useState(false);
  const [enviando, setEnviando] = useState(false);

  const destino = state?.desde ?? "/gestion";

  if (autenticado && esFuncionario) return <Navigate to={destino} replace />;

  const actualizar = (campo) => (evento) => {
    setDatos((anteriores) => ({ ...anteriores, [campo]: evento.target.value }));
    setErrores((anteriores) => ({ ...anteriores, [campo]: null }));
  };

  const ingresar = async (evento) => {
    evento.preventDefault();
    const faltantes = {
      usuario: datos.usuario.trim() ? null : "Ingrese su usuario.",
      contrasena: datos.contrasena ? null : "Ingrese su contraseña.",
    };
    setErrores(faltantes);
    if (faltantes.usuario || faltantes.contrasena) {
      evento.currentTarget.querySelector('[aria-invalid="true"]')?.focus();
      return;
    }

    setEnviando(true);
    setErrorGeneral(null);
    try {
      const sesion = await iniciarSesionFuncionario({ usuario: datos.usuario.trim(), contrasena: datos.contrasena });
      iniciarSesion(sesion);
      navegar(destino, { replace: true });
    } catch (error) {
      setErrorGeneral(error);
      setDatos((anteriores) => ({ ...anteriores, contrasena: "" }));
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="acceso">
      <form className="acceso-tarjeta" onSubmit={ingresar} noValidate>
        <Logo />
        <div className="acceso-titulo">
          <span className="antetitulo">Back-office</span>
          <h1>Acceso funcionarios</h1>
          <p>Ingrese con el usuario y la contraseña asignados por el administrador.</p>
        </div>

        {motivoCierre === "vencida" && !errorGeneral && (
          <div className="aviso aviso-info" role="status">
            <p>Su sesión venció. Ingrese de nuevo para continuar.</p>
          </div>
        )}
        {errorGeneral && (
          <div className="aviso aviso-error" role="alert">
            <p>{mensajeDeError(errorGeneral)}</p>
          </div>
        )}

        <Campo
          etiqueta="Usuario"
          obligatorio
          autoComplete="username"
          autoCapitalize="none"
          spellCheck={false}
          maxLength={60}
          value={datos.usuario}
          error={errores.usuario}
          onChange={actualizar("usuario")}
        />
        <Campo
          etiqueta="Contraseña"
          obligatorio
          type={verContrasena ? "text" : "password"}
          autoComplete="current-password"
          maxLength={100}
          value={datos.contrasena}
          error={errores.contrasena}
          onChange={actualizar("contrasena")}
          accesorio={
            <button
              type="button"
              onClick={() => setVerContrasena((visible) => !visible)}
              aria-label={verContrasena ? "Ocultar contraseña" : "Mostrar contraseña"}
              aria-pressed={verContrasena}
            >
              <Icono nombre="ojo" tamano={19} />
            </button>
          }
        />

        <Boton type="submit" icono="candado" disabled={enviando}>
          {enviando ? "Ingresando…" : "Ingresar"}
        </Boton>

        <p className="acceso-nota">
          ¿Es ciudadano? Radique o consulte su PQR desde el <a href="/">portal</a>, sin crear una cuenta.
        </p>
      </form>
    </div>
  );
}