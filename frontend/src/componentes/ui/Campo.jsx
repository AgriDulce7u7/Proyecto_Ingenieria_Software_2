import { useId } from "react";
import { Icono } from "./Icono";
import "./ui.css";

/**
 * Campo de texto con etiqueta y mensaje de error asociado (aria-invalid y aria-describedby).
 * La validación la hace el formulario: el asterisco indica obligatoriedad sin activar la del navegador.
 */
export function Campo({ etiqueta, obligatorio = false, error, className = "", ...propsInput }) {
  const id = useId();
  const idError = `${id}-error`;

  return (
    <div className={`campo ${className}`.trim()}>
      <label htmlFor={id}>
        {etiqueta}
        {obligatorio && <b aria-hidden="true"> *</b>}
      </label>
      <input
        id={id}
        aria-required={obligatorio || undefined}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? idError : undefined}
        {...propsInput}
      />
      {error && (
        <small id={idError} className="campo-error">
          <Icono nombre="alerta" tamano={14} />
          {error}
        </small>
      )}
    </div>
  );
}