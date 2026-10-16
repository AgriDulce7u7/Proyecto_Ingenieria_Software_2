import { useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { iniciarSesionCiudadano } from "../../../api/autenticacion";
import { enfocarPrimerError } from "../../../componentes/enfocarPrimerError";
import { Boton } from "../../../componentes/ui/Boton";
import { Campo } from "../../../componentes/ui/Campo";
import { Icono } from "../../../componentes/ui/Icono";
import { useTituloPagina } from "../../../hooks/useTituloPagina";
import { limpiarDocumento } from "../../../presentacion/datosPersonales";
import { useSesion } from "../../../sesion/useSesion";
import "../../acceso/acceso.css";
import "./cuenta.css";

/** Mensaje claro para cada fallo del ingreso, sin revelar si el documento existe. */
function mensajeDeError(error) {
  if (error.cuentaBloqueada) return error.detalle;
  if (error.noAutenticado) {
    return "Número de identificación o contraseña incorrectos. Tras 3 intentos fallidos la cuenta se bloquea 15 minutos.";
  }
  return error.detalle ?? error.message;
}

/**
 * Ingreso al portal de autogestión (RF-07, CU-05) con el número de identificación y la contraseña que
 * el ciudadano creó al activar su cuenta. Tras ingresar vuelve a la página pedida o a «Mi cuenta».
 */
export function IngresarCiudadano() {
  useTituloPagina("Ingresar a mi cuenta");
  const navegar = useNavigate();
  const { state } = useLocation();
  const { autenticado, esCiudadano, esFuncionario, motivoCierre, iniciarSesion } = useSesion();
  const [datos, setDatos] = useState({ numeroDocumento: state?.documento ?? "", contrasena: "" });
  const [errores, setErrores] = useState({});
  const [errorGeneral, setErrorGeneral] = useState(null);
  const [verContrasena, setVerContrasena] = useState(false);
  const [enviando, setEnviando] = useState(false);

  const destino = state?.desde ?? "/mi-cuenta";

  if (autenticado && esCiudadano) return <Navigate to={destino} replace />;

  const actualizar = (campo) => (evento) => {
    setDatos((anteriores) => ({ ...anteriores, [campo]: evento.target.value }));
    setErrores((anteriores) => ({ ...anteriores, [campo]: null }));
  };

  const ingresar = async (evento) => {
    evento.preventDefault();
    const formulario = evento.currentTarget;
    const documento = limpiarDocumento(datos.numeroDocumento);
    const faltantes = {
      numeroDocumento: documento ? null : "Ingrese su número de identificación.",
      contrasena: datos.contrasena ? null : "Ingrese su contraseña.",
    };
    setErrores(faltantes);
    if (faltantes.numeroDocumento || faltantes.contrasena) {
      enfocarPrimerError(formulario);
      return;
    }

    setEnviando(true);
    setErrorGeneral(null);
    try {
      const sesion = await iniciarSesionCiudadano({ numeroDocumento: documento, contrasena: datos.contrasena });
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
        <span className="cuenta-acceso-icono" aria-hidden="true">
          <Icono nombre="recibo" tamano={24} />
        </span>
        <div className="acceso-titulo">
          <span className="antetitulo">Portal de autogestión</span>
          <h1>Ingresar a mi cuenta</h1>
          <p>Consulte sus facturas, su historial de consumo y pague en línea.</p>
        </div>

        {state?.activada && !errorGeneral && (
          <div className="aviso aviso-exito" role="status">
            <p>Su cuenta quedó activa. Ingrese con su número de identificación y la contraseña que creó.</p>
          </div>
        )}
        {motivoCierre === "vencida" && !errorGeneral && !state?.activada && (
          <div className="aviso aviso-info" role="status">
            <p>Su sesión venció. Ingrese de nuevo para continuar.</p>
          </div>
        )}
        {esFuncionario && (
          <div className="aviso aviso-info" role="status">
            <p>Tiene abierta una sesión de funcionario. Si ingresa como ciudadano, esa sesión se cierra.</p>
          </div>
        )}
        {errorGeneral && (
          <div className="aviso aviso-error" role="alert">
            <p>{mensajeDeError(errorGeneral)}</p>
          </div>
        )}

        <Campo
          etiqueta="Número de identificación"
          obligatorio
          className="cifra"
          inputMode="numeric"
          autoComplete="username"
          maxLength={20}
          value={datos.numeroDocumento}
          error={errores.numeroDocumento}
          onChange={actualizar("numeroDocumento")}
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

        <div className="cuenta-acceso-activar">
          <strong>¿Es la primera vez?</strong>
          <p>Si ya es usuario de EPQ, cree su contraseña con su número de identificación y el correo registrado.</p>
          <Boton a="/activar-cuenta" variante="secundario">
            Activar mi cuenta
          </Boton>
        </div>

        <p className="acceso-nota">
          ¿Es funcionario de EPQ? <Link to="/acceso">Ingrese al back-office</Link>.
        </p>
      </form>
    </div>
  );
}