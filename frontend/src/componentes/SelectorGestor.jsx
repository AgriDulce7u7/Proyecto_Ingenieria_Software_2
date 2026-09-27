import { useGestores } from "../hooks/usePqr";
import { useSesion } from "../sesion/useSesion";

/**
 * Reemplaza al inicio de sesión mientras el backend no tenga autenticación:
 * define con qué gestor se ejecutan las acciones del back-office.
 */
export function SelectorGestor() {
  const { gestor, seleccionarGestor } = useSesion();
  const { data: gestores = [], isPending, isError } = useGestores();

  const alCambiar = (evento) => {
    const id = Number(evento.target.value);
    seleccionarGestor(gestores.find((g) => g.id === id) ?? null);
  };

  return (
    <label className="selector-gestor">
      <span className="selector-gestor-etiqueta">
        Gestor
        <br />
        actual
      </span>
      <select
        aria-label="Gestor actual"
        value={gestor?.id ?? ""}
        onChange={alCambiar}
        disabled={isPending || isError}
      >
        <option value="">{isError ? "No disponible" : "Seleccionar gestor"}</option>
        {gestores.map((g) => (
          <option key={g.id} value={g.id}>
            {g.nombre}
          </option>
        ))}
      </select>
    </label>
  );
}