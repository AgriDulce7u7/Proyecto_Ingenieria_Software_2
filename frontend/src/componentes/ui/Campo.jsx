import { useId } from "react";
import { Icono } from "./Icono";
import "./ui.css";

/**
 * Campo de formulario con etiqueta, ayuda, contador y mensaje de error asociados al control
 * (aria-invalid y aria-describedby).
 *
 * - `como`: "input" (por defecto), "select" o "textarea". Las opciones del select van como children.
 * - La validación la hace el formulario: el asterisco indica obligatoriedad sin activar la del navegador.
 * - `accesorio`: botón dentro del control, a la derecha (por ejemplo, mostrar la contraseña).
 */
export function Campo({
  etiqueta,
  como: Control = "input",
  obligatorio = false,
  error,
  ayuda,
  contador,
  className = "",
  accesorio,
  children,
  ...propsControl
}) {
  const id = useId();
  const idAyuda = `${id}-ayuda`;
  const idError = `${id}-error`;
  const descripciones = [ayuda && idAyuda, error && idError].filter(Boolean).join(" ") || undefined;
  const control = (
    <Control
      id={id}
      aria-required={obligatorio || undefined}
      aria-invalid={error ? true : undefined}
      aria-describedby={descripciones}
      {...propsControl}
    >
      {children}
    </Control>
  );

  return (
    <div className={`campo ${className}`.trim()}>
      <label htmlFor={id}>
        {etiqueta}
        {obligatorio && <b aria-hidden="true"> *</b>}
      </label>
      {accesorio ? (
        <div className="campo-con-accesorio">
          {control}
          {accesorio}
        </div>
      ) : (
        control
      )}
      {error && (
        <small id={idError} className="campo-error">
          <Icono nombre="alerta" tamano={14} />
          {error}
        </small>
      )}
      {(ayuda || contador) && (
        <div className="campo-pie">
          {ayuda && <small id={idAyuda}>{ayuda}</small>}
          {contador && <small className="campo-contador">{contador}</small>}
        </div>
      )}
    </div>
  );
}