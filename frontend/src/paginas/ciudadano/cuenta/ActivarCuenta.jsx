import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { activarCuentaCiudadano } from "../../../api/autenticacion";
import { enfocarPrimerError } from "../../../componentes/enfocarPrimerError";
import { Boton } from "../../../componentes/ui/Boton";
import { Campo } from "../../../componentes/ui/Campo";
import { Icono } from "../../../componentes/ui/Icono";
import { useTituloPagina } from "../../../hooks/useTituloPagina";
import { PATRON_CORREO, limpiarDocumento } from "../../../presentacion/datosPersonales";
import { MENSAJE_CONTRASENA, PATRON_CONTRASENA, reglasContrasena } from "../../../presentacion/portal";
import "../../acceso/acceso.css";
import "./cuenta.css";

const VACIO = { numeroDocumento: "", correo: "", contrasena: "", confirmacion: "" };

function validar(datos) {
  const errores = {};
  if (!limpiarDocumento(datos.numeroDocumento)) errores.numeroDocumento = "Ingrese su número de identificación.";
  if (!PATRON_CORREO.test(datos.correo.trim())) errores.correo = "Escriba el correo que registró en EPQ.";
  if (!PATRON_CONTRASENA.test(datos.contrasena)) errores.contrasena = MENSAJE_CONTRASENA;
  if (!errores.contrasena && datos.confirmacion !== datos.contrasena) {
    errores.confirmacion = "Las contraseñas no coinciden.";
  }
  return errores;
}

/** Lista de reglas de la contraseña que se marcan a medida que se cumplen. */
function ReglasContrasena({ contrasena }) {
  return (
    <ul className="cuenta-reglas" aria-label="Requisitos de la contraseña">
      {reglasContrasena(contrasena).map((regla) => (
        <li key={regla.texto} data-cumple={regla.cumple}>
          <Icono nombre={regla.cumple ? "check" : "cerrar"} tamano={14} />
          {regla.texto}
        </li>
      ))}
    </ul>
  );
}

/**
 * Activación de la cuenta del portal (autorregistro, CU-01): el usuario del servicio que EPQ ya registró
 * crea su contraseña comprobando su número de identificación y el correo que dejó en el registro.
 */
export function ActivarCuenta() {
  useTituloPagina("Activar mi cuenta");
  const navegar = useNavigate();
  const [datos, setDatos] = useState(VACIO);
  const [errores, setErrores] = useState({});
  const [errorGeneral, setErrorGeneral] = useState(null);
  const [verContrasena, setVerContrasena] = useState(false);
  const [enviando, setEnviando] = useState(false);

  const actualizar = (campo) => (evento) => {
    setDatos((anteriores) => ({ ...anteriores, [campo]: evento.target.value }));
    setErrores((anteriores) => ({ ...anteriores, [campo]: null }));
  };

  const activar = async (evento) => {
    evento.preventDefault();
    const formulario = evento.currentTarget;
    const erroresCliente = validar(datos);
    setErrores(erroresCliente);
    if (Object.keys(erroresCliente).length > 0) {
      enfocarPrimerError(formulario);
      return;
    }

    setEnviando(true);
    setErrorGeneral(null);
    const documento = limpiarDocumento(datos.numeroDocumento);
    try {
      await activarCuentaCiudadano({ numeroDocumento: documento, correo: datos.correo.trim(), contrasena: datos.contrasena });
      navegar("/ingresar", { replace: true, state: { activada: true, documento } });
    } catch (error) {
      if (error.esValidacion) setErrores(error.errores);
      else setErrorGeneral(error);
      setDatos((anteriores) => ({ ...anteriores, contrasena: "", confirmacion: "" }));
    } finally {
      setEnviando(false);
    }
  };

  const ojo = (
    <button
      type="button"
      onClick={() => setVerContrasena((visible) => !visible)}
      aria-label={verContrasena ? "Ocultar contraseña" : "Mostrar contraseña"}
      aria-pressed={verContrasena}
    >
      <Icono nombre="ojo" tamano={19} />
    </button>
  );

  return (
    <div className="acceso">
      <form className="acceso-tarjeta" onSubmit={activar} noValidate>
        <span className="cuenta-acceso-icono" aria-hidden="true">
          <Icono nombre="candado" tamano={24} />
        </span>
        <div className="acceso-titulo">
          <span className="antetitulo">Portal de autogestión</span>
          <h1>Activar mi cuenta</h1>
          <p>
            Es para usuarios que ya tienen servicios con EPQ. Si aún no está registrado, acuda a una oficina de
            atención.
          </p>
        </div>

        {errorGeneral?.conflicto && (
          <div className="aviso aviso-info" role="alert">
            <p>
              Ya tiene una cuenta en el portal. <Link to="/ingresar">Ingrese con su contraseña</Link>.
            </p>
          </div>
        )}
        {errorGeneral && !errorGeneral.conflicto && (
          <div className="aviso aviso-error" role="alert">
            <p>{errorGeneral.detalle ?? errorGeneral.message}</p>
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
          etiqueta="Correo registrado"
          obligatorio
          type="email"
          autoComplete="email"
          maxLength={150}
          ayuda="El que dejó al registrarse como usuario de EPQ."
          value={datos.correo}
          error={errores.correo}
          onChange={actualizar("correo")}
        />
        <div className="cuenta-contrasena">
          <Campo
            etiqueta="Nueva contraseña"
            obligatorio
            type={verContrasena ? "text" : "password"}
            autoComplete="new-password"
            maxLength={64}
            value={datos.contrasena}
            error={errores.contrasena}
            onChange={actualizar("contrasena")}
            accesorio={ojo}
          />
          <ReglasContrasena contrasena={datos.contrasena} />
        </div>
        <Campo
          etiqueta="Confirme la contraseña"
          obligatorio
          type={verContrasena ? "text" : "password"}
          autoComplete="new-password"
          maxLength={64}
          value={datos.confirmacion}
          error={errores.confirmacion}
          onChange={actualizar("confirmacion")}
        />

        <Boton type="submit" icono="check" disabled={enviando}>
          {enviando ? "Activando…" : "Activar cuenta"}
        </Boton>

        <p className="acceso-nota">
          ¿Ya activó su cuenta? <Link to="/ingresar">Ingrese aquí</Link>.
        </p>
      </form>
    </div>
  );
}