export function Cargando({ texto = "Cargando…" }) {
  return (
    <p className="texto-secundario" role="status" aria-live="polite">
      {texto}
    </p>
  );
}
