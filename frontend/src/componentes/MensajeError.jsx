/**
 * Muestra un ErrorApi (o cualquier Error) con su título y detalle.
 * Los errores por campo de las validaciones se muestran en cada campo del formulario, no aquí.
 */
export function MensajeError({ error, children }) {
  if (!error) {
    return null;
  }
  return (
    <div className="aviso aviso-error" role="alert">
      {error.titulo && <p><strong>{error.titulo}.</strong></p>}
      <p>{error.detalle ?? error.message}</p>
      {children}
    </div>
  );
}
