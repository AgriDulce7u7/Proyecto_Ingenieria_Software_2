import "./formulario-gestion.css";

/**
 * Formulario largo del back-office dividido en secciones numeradas (registro de usuarios, contratos).
 * Sigue el mismo diseño del formulario de radicación del portal.
 *
 * - El <form> lo pone la pantalla, con la clase `formulario-gestion` y su onSubmit.
 * - `SeccionFormulario`: bloque numerado con título, descripción y la rejilla de campos.
 * - `PieFormulario`: franja final con el mensaje de error general y los botones.
 */
export function SeccionFormulario({ numero, titulo, descripcion, children }) {
  return (
    <section className="formulario-seccion" aria-labelledby={`formulario-seccion-${numero}`}>
      <span className="formulario-seccion-numero" aria-hidden="true">
        {numero}
      </span>
      <div>
        <h2 id={`formulario-seccion-${numero}`}>{titulo}</h2>
        {descripcion && <p>{descripcion}</p>}
        <div className="formulario-campos">{children}</div>
      </div>
    </section>
  );
}

export function PieFormulario({ children }) {
  return <div className="formulario-pie">{children}</div>;
}