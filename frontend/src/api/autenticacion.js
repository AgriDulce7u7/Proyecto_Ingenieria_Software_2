import { solicitar } from "./cliente";

/** Endpoints de autenticación (IS-SIS-01). */

export const iniciarSesionFuncionario = ({ usuario, contrasena }) =>
  solicitar("/auth/login", { metodo: "POST", cuerpo: { usuario, contrasena } });

export const iniciarSesionCiudadano = ({ numeroDocumento, contrasena }) =>
  solicitar("/auth/ciudadano/login", { metodo: "POST", cuerpo: { numeroDocumento, contrasena } });

export const activarCuentaCiudadano = (datos) =>
  solicitar("/auth/ciudadano/activacion", { metodo: "POST", cuerpo: datos });

/** Registra el cierre de sesión en el log de auditoría (RF-15). */
export const cerrarSesionServidor = () => solicitar("/auth/logout", { metodo: "POST" });