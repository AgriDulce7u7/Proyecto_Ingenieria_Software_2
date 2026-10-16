import { useState } from "react";
import { exportarCumplimiento } from "../../../api/pqr";
import { MensajeError } from "../../../componentes/MensajeError";
import { Boton } from "../../../componentes/ui/Boton";

/** Descarga del reporte de incumplimientos del periodo en PDF o Excel (SyR-02f, reporte SSPD). */
export function ExportarCumplimiento({ desde, hasta }) {
  const [enCurso, setEnCurso] = useState(null);
  const [error, setError] = useState(null);

  const exportar = async (formato) => {
    setEnCurso(formato);
    setError(null);
    try {
      await exportarCumplimiento({ formato, desde, hasta });
    } catch (falla) {
      setError(falla);
    } finally {
      setEnCurso(null);
    }
  };

  return (
    <div className="cumplimiento-exportar">
      <div>
        <Boton variante="secundario" icono="descargar" onClick={() => exportar("pdf")} disabled={Boolean(enCurso)}>
          {enCurso === "pdf" ? "Generando…" : "PDF"}
        </Boton>
        <Boton variante="secundario" icono="descargar" onClick={() => exportar("excel")} disabled={Boolean(enCurso)}>
          {enCurso === "excel" ? "Generando…" : "Excel"}
        </Boton>
      </div>
      <MensajeError error={error} />
    </div>
  );
}