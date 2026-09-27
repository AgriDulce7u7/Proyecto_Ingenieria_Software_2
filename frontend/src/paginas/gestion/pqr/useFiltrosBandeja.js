import { useSearchParams } from "react-router-dom";

const FILTROS = ["q", "estado", "tipo", "gestor"];

/**
 * Filtros y página de la bandeja guardados en la URL (?estado=RADICADO&gestor=2&pagina=2).
 * Se conservan al volver desde el detalle de una PQR y se pueden compartir.
 */
export function useFiltrosBandeja() {
  const [parametros, setParametros] = useSearchParams();

  const filtros = Object.fromEntries(FILTROS.map((nombre) => [nombre, parametros.get(nombre) ?? ""]));
  const pagina = Math.max(1, Number(parametros.get("pagina")) || 1);
  const hayFiltros = FILTROS.some((nombre) => filtros[nombre]);

  const cambiarFiltro = (nombre, valor) => {
    setParametros(
      (anteriores) => {
        const nuevos = new URLSearchParams(anteriores);
        if (valor) nuevos.set(nombre, valor);
        else nuevos.delete(nombre);
        nuevos.delete("pagina"); // un filtro nuevo vuelve a la primera página
        return nuevos;
      },
      // Escribir en el buscador no debe llenar el historial del navegador
      { replace: nombre === "q" },
    );
  };

  const cambiarPagina = (numero) =>
    setParametros((anteriores) => {
      const nuevos = new URLSearchParams(anteriores);
      if (numero > 1) nuevos.set("pagina", String(numero));
      else nuevos.delete("pagina");
      return nuevos;
    });

  const limpiar = () => setParametros({});

  return { filtros, pagina, hayFiltros, cambiarFiltro, cambiarPagina, limpiar };
}