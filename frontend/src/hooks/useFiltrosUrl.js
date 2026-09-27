import { useSearchParams } from "react-router-dom";

/**
 * Filtros de un listado guardados en la URL (?estado=...&q=...&pagina=2).
 * Se conservan al volver desde un detalle, al recargar y al compartir el enlace.
 *
 * @param {string[]} nombres filtros que maneja la pantalla
 * @param {string} textoLibre filtro que se escribe letra a letra; no llena el historial del navegador
 */
export function useFiltrosUrl(nombres, textoLibre = "q") {
  const [parametros, setParametros] = useSearchParams();

  const filtros = Object.fromEntries(nombres.map((nombre) => [nombre, parametros.get(nombre) ?? ""]));
  const pagina = Math.max(1, Number(parametros.get("pagina")) || 1);
  const hayFiltros = nombres.some((nombre) => parametros.has(nombre));

  const cambiarFiltro = (nombre, valor) =>
    setParametros(
      (anteriores) => {
        const nuevos = new URLSearchParams(anteriores);
        if (valor) nuevos.set(nombre, valor);
        else nuevos.delete(nombre);
        nuevos.delete("pagina"); // un filtro nuevo vuelve a la primera página
        return nuevos;
      },
      { replace: nombre === textoLibre },
    );

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