import { useQuery } from "@tanstack/react-query";
import { obtenerGestores } from "../api/pqr";
import { useSesion } from "../sesion/useSesion";

/**
 * Reemplaza al inicio de sesión mientras el backend no tenga autenticación:
 * define con qué gestor se ejecutan las acciones del back-office.
 */
export function SelectorGestor() {
  const { gestor, seleccionarGestor } = useSesion();
  const { data: gestores = [], isLoading, isError } = useQuery({
    queryKey: ["gestores"],
    queryFn: obtenerGestores,
    staleTime: 5 * 60 * 1000,
  });

  const alCambiar = (evento) => {
    const id = Number(evento.target.value);
    seleccionarGestor(gestores.find((g) => g.id === id) ?? null);
  };

  return (
    <label className="selector-gestor">
      <span className="solo-lectores-movil">Gestor</span>
      <select value={gestor?.id ?? ""} onChange={alCambiar} disabled={isLoading || isError}>
        <option value="">{isError ? "No disponible" : "Seleccionar…"}</option>
        {gestores.map((g) => (
          <option key={g.id} value={g.id}>
            {g.nombre}
          </option>
        ))}
      </select>
    </label>
  );
}
