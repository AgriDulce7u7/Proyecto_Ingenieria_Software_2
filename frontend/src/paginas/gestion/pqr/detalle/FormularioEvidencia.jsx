import { useId, useRef, useState } from "react";
import { MensajeError } from "../../../../componentes/MensajeError";
import { Boton } from "../../../../componentes/ui/Boton";
import { Campo } from "../../../../componentes/ui/Campo";
import { Icono } from "../../../../componentes/ui/Icono";
import { useAdjuntarEvidencia } from "../../../../hooks/usePqr";
import { ACEPTA_EVIDENCIA, EVIDENCIA, formatearTamano, validarEvidencia } from "../../../../presentacion/archivos";

/**
 * Adjunta una evidencia documental a la PQR (RN-06, RF-09). Valida tipo y tamaño antes de subir
 * y, si el backend rechaza el archivo, muestra su mensaje. Se usa en el panel de evidencias
 * y dentro del diálogo de respuesta.
 */
export function FormularioEvidencia({ radicado, onAdjuntada }) {
  const id = useId();
  const selector = useRef(null);
  const [archivo, setArchivo] = useState(null);
  const [descripcion, setDescripcion] = useState("");
  const [error, setError] = useState("");
  const adjuntar = useAdjuntarEvidencia(radicado);

  const elegir = (evento) => {
    const elegido = evento.target.files?.[0] ?? null;
    adjuntar.reset();
    setArchivo(elegido);
    setError(elegido ? validarEvidencia(elegido) ?? "" : "");
  };

  const limpiar = () => {
    setArchivo(null);
    setDescripcion("");
    setError("");
    if (selector.current) selector.current.value = "";
  };

  const subir = () => {
    const mensaje = validarEvidencia(archivo);
    if (mensaje) {
      setError(mensaje);
      selector.current?.focus();
      return;
    }
    adjuntar.mutate(
      { archivo, descripcion: descripcion.trim() || undefined },
      {
        onSuccess: (evidencia) => {
          limpiar();
          onAdjuntada?.(evidencia);
        },
      },
    );
  };

  return (
    // No es un <form>: también se usa dentro del formulario del diálogo de respuesta
    <div className="evidencia-formulario">
      <div className="evidencia-selector">
        <input
          ref={selector}
          id={id}
          type="file"
          accept={ACEPTA_EVIDENCIA}
          className="evidencia-selector-control"
          aria-invalid={error ? true : undefined}
          aria-describedby={`${id}-ayuda${error ? ` ${id}-error` : ""}`}
          onChange={elegir}
        />
        <label htmlFor={id} className="evidencia-selector-zona">
          <Icono nombre="adjunto" />
          <span>
            <strong>{archivo ? archivo.name : "Seleccionar archivo"}</strong>
            <small id={`${id}-ayuda`}>
              {archivo ? formatearTamano(archivo.size) : "PDF, JPG, PNG o Word. Máximo 5 MB."}
            </small>
          </span>
        </label>
        {error && (
          <small id={`${id}-error`} className="campo-error">
            <Icono nombre="alerta" tamano={14} />
            {error}
          </small>
        )}
      </div>

      {archivo && !error && (
        <>
          <Campo
            etiqueta="Descripción"
            placeholder="Por ejemplo: acta de visita técnica (opcional)"
            maxLength={EVIDENCIA.descripcionMaxima}
            value={descripcion}
            onChange={(evento) => setDescripcion(evento.target.value)}
          />
          <MensajeError error={adjuntar.error} />
          <div className="evidencia-formulario-acciones">
            <Boton variante="discreto" onClick={limpiar} disabled={adjuntar.isPending}>
              Quitar
            </Boton>
            <Boton variante="secundario" icono="adjunto" onClick={subir} disabled={adjuntar.isPending}>
              {adjuntar.isPending ? "Adjuntando…" : "Adjuntar evidencia"}
            </Boton>
          </div>
        </>
      )}
    </div>
  );
}