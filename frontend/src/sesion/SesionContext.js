import { createContext } from "react";

/**
 * Sesión autenticada con JWT (RF-14, RNF-05): usuario, rol y las acciones para iniciar y
 * cerrar sesión. La provee SesionProvider; se consume con useSesion().
 */
export const SesionContext = createContext(null);