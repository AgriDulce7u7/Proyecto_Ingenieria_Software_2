import { Campo } from "../../../componentes/ui/Campo";
import { useFuncionarios } from "../../../hooks/useAdministracion";
import { useGestores } from "../../../hooks/usePqr";

/**
 * Registro de gestor de PQR que se vincula a la cuenta. Lo exigen los roles Gestor y Jefe de PQR para que
 * la cuenta actúe sobre las PQR de ese gestor. Solo se ofrecen los gestores que no tienen otra cuenta.
 */
export function SelectorGestor({ valor, error, funcionarioId = null, onCambiar }) {
  const gestores = useGestores();
  const funcionarios = useFuncionarios();
  const ocupados = new Set(
    (funcionarios.data ?? [])
      .filter((funcionario) => funcionario.gestorId && funcionario.id !== funcionarioId)
      .map((funcionario) => funcionario.gestorId),
  );
  const libres = (gestores.data ?? []).filter((gestor) => !ocupados.has(gestor.id));
  const cargando = gestores.isPending || funcionarios.isPending;

  return (
    <Campo
      etiqueta="Gestor de PQR vinculado"
      como="select"
      obligatorio
      className="formulario-campo-completo"
      disabled={cargando}
      value={valor}
      error={error}
      ayuda={
        !cargando && libres.length === 0
          ? "Todos los gestores de PQR ya tienen cuenta. Registre el gestor en la Oficina de PQR antes de continuar."
          : "La cuenta atenderá las PQR asignadas a este gestor."
      }
      onChange={(evento) => onCambiar(evento.target.value)}
    >
      <option value="">{cargando ? "Cargando gestores…" : "Seleccione el gestor"}</option>
      {libres.map((gestor) => (
        <option key={gestor.id} value={gestor.id}>
          {gestor.nombre}
          {gestor.area ? ` · ${gestor.area}` : ""}
          {gestor.esJefe ? " (jefe)" : ""}
        </option>
      ))}
    </Campo>
  );
}