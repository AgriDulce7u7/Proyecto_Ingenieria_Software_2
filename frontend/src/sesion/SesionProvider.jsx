import { useMemo, useState } from "react";
import { SesionContext } from "./SesionContext";

const CLAVE = "sigca-epq:gestor";

function leerGestorGuardado() {
  try {
    const guardado = localStorage.getItem(CLAVE);
    return guardado ? JSON.parse(guardado) : null;
  } catch {
    return null;
  }
}

export function SesionProvider({ children }) {
  const [gestor, setGestor] = useState(leerGestorGuardado);

  const valor = useMemo(() => {
    const seleccionarGestor = (nuevo) => {
      setGestor(nuevo);
      try {
        if (nuevo) {
          localStorage.setItem(CLAVE, JSON.stringify(nuevo));
        } else {
          localStorage.removeItem(CLAVE);
        }
      } catch {
        // Sin almacenamiento disponible: la selección dura solo mientras la pestaña esté abierta.
      }
    };
    return {
      gestor,
      seleccionarGestor,
      // Identificador que se envía como "usuario" en las acciones auditadas (SWR-09)
      usuario: gestor?.correo ?? null,
    };
  }, [gestor]);

  return <SesionContext.Provider value={valor}>{children}</SesionContext.Provider>;
}
