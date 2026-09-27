/** Recorta una lista a la página pedida. La página se ajusta si quedó fuera de rango al filtrar. */
export function paginar(lista, pagina, tamano) {
  const totalPaginas = Math.max(1, Math.ceil(lista.length / tamano));
  const paginaActual = Math.min(Math.max(1, pagina), totalPaginas);
  const inicio = (paginaActual - 1) * tamano;
  return { visibles: lista.slice(inicio, inicio + tamano), paginaActual, totalPaginas, inicio };
}