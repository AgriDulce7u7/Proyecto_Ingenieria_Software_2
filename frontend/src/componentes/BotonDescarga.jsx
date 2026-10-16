import { useState } from "react";
import { Boton } from "./ui/Boton";

/**
 * Botón que descarga un archivo del backend (PDF de comprobantes y facturas). Mientras se genera
 * muestra `textoCargando`; si falla, entrega el error a `onError` para que la pantalla lo muestre.
 */
export function BotonDescarga({ descargar, children, textoCargando = "Generando PDF…", onError, variante = "secundario" }) {
  const [descargando, setDescargando] = useState(false);

  const alPulsar = async () => {
    setDescargando(true);
    onError?.(null);
    try {
      await descargar();
    } catch (error) {
      onError?.(error);
    } finally {
      setDescargando(false);
    }
  };

  return (
    <Boton variante={variante} icono="descargar" onClick={alPulsar} disabled={descargando}>
      {descargando ? textoCargando : children}
    </Boton>
  );
}