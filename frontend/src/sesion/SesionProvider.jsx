import { useQueryClient } from "@tanstack/react-query";
import { useCallback, useEffect, useMemo, useState } from "react";
import { cerrarSesionServidor } from "../api/autenticacion";
import { guardarSesion, obtenerSesion, suscribirse } from "./almacenSesion";
import { rolPuede } from "./capacidades";
import { SesionContext } from "./SesionContext";

/**
 * Expone la sesión a la interfaz y la mantiene sincronizada con el almacén:
 * - Al iniciar o cerrar sesión se vacía la caché de consultas, para que un usuario nunca
 *   vea datos cargados por el anterior en el mismo navegador.
 * - Cierra la sesión sola cuando el token vence (expiraEn), sin esperar a un 401.
 */
export function SesionProvider({ children }) {
  const clienteConsultas = useQueryClient();
  const [sesion, setSesion] = useState(obtenerSesion);
  const [motivoCierre, setMotivoCierre] = useState(null);

  useEffect(
    () =>
      suscribirse((nueva, motivo) => {
        clienteConsultas.clear();
        setSesion(nueva);
        setMotivoCierre(nueva ? null : motivo);
      }),
    [clienteConsultas],
  );

  // Cierre automático al vencer el token
  useEffect(() => {
    if (!sesion) return undefined;
    const restante = new Date(sesion.expiraEn).getTime() - Date.now();
    const temporizador = setTimeout(() => guardarSesion(null, "vencida"), Math.max(restante, 0));
    return () => clearTimeout(temporizador);
  }, [sesion]);

  const cerrarSesion = useCallback(async () => {
    try {
      await cerrarSesionServidor();
    } catch {
      // El token se descarta igual: el cierre en el servidor solo deja constancia en la auditoría.
    }
    guardarSesion(null, "manual");
  }, []);

  const valor = useMemo(() => {
    const usuario = sesion?.usuario ?? null;
    return {
      usuario,
      autenticado: Boolean(usuario),
      esFuncionario: usuario?.tipo === "FUNCIONARIO",
      esCiudadano: usuario?.tipo === "CIUDADANO",
      /** ¿El usuario puede usar esta capacidad? (ver capacidades.js) */
      puede: (capacidad) => rolPuede(usuario?.rol, capacidad),
      /** Motivo del último cierre: "vencida" o "manual" (para avisar en el login) */
      motivoCierre,
      iniciarSesion: (respuestaLogin) => guardarSesion(respuestaLogin),
      cerrarSesion,
    };
  }, [sesion, motivoCierre, cerrarSesion]);

  return <SesionContext.Provider value={valor}>{children}</SesionContext.Provider>;
}