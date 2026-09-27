import { Boton } from "../../../componentes/ui/Boton";
import { Icono } from "../../../componentes/ui/Icono";
import { useEstadosPqr, useGestores, useTiposSolicitud } from "../../../hooks/usePqr";

/** Barra de búsqueda y filtros de la bandeja (estado, tipo y gestor). */
export function FiltrosBandeja({ filtros, hayFiltros, cambiarFiltro, limpiar }) {
  const { data: estados = [] } = useEstadosPqr();
  const { data: tipos = [] } = useTiposSolicitud();
  const { data: gestores = [] } = useGestores();

  const alCambiar = (nombre) => (evento) => cambiarFiltro(nombre, evento.target.value);

  return (
    <div className="bandeja-filtros" role="search">
      <label className="bandeja-buscador">
        <Icono nombre="buscar" tamano={18} />
        <span className="solo-lectores">Buscar</span>
        <input
          type="search"
          placeholder="Buscar radicado, asunto o ciudadano..."
          value={filtros.q}
          onChange={alCambiar("q")}
        />
      </label>

      <select aria-label="Filtrar por estado" value={filtros.estado} onChange={alCambiar("estado")}>
        <option value="">Todos los estados</option>
        {estados.map((estado) => (
          <option key={estado.codigo} value={estado.codigo}>
            {estado.nombre}
          </option>
        ))}
      </select>

      <select aria-label="Filtrar por tipo" value={filtros.tipo} onChange={alCambiar("tipo")}>
        <option value="">Todos los tipos</option>
        {tipos.map((tipo) => (
          <option key={tipo.codigo} value={tipo.codigo}>
            {tipo.nombre}
          </option>
        ))}
      </select>

      <select aria-label="Filtrar por gestor" value={filtros.gestor} onChange={alCambiar("gestor")}>
        <option value="">Todos los gestores</option>
        {gestores.map((gestor) => (
          <option key={gestor.id} value={gestor.id}>
            {gestor.nombre}
          </option>
        ))}
      </select>

      <Boton variante="discreto" onClick={limpiar} disabled={!hayFiltros}>
        Limpiar
      </Boton>
    </div>
  );
}