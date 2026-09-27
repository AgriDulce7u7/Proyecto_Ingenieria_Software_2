import { createContext } from "react";

/**
 * Sesión del back-office.
 *
 * El prototipo no tiene autenticación (limitación documentada en backend/README.md): el gestor
 * se elige en un selector. Cuando exista login, este contexto recibirá el usuario autenticado
 * y el resto de la aplicación no cambia.
 */
export const SesionContext = createContext(null);
