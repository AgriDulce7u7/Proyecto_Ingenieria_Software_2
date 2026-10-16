import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { Paginacion } from "../../../componentes/Paginacion";
import { Boton } from "../../../componentes/ui/Boton";
import { Icono } from "../../../componentes/ui/Icono";
import { useListaContratos } from "../../../hooks/useContratos";
import { useFiltrosUrl } from "../../../hooks/useFiltrosUrl";
import { useMunicipios } from "../../../hooks/useTerritorio";
import { paginar } from "../../../presentacion/paginacion";
import { buscarEn } from "../../../presentacion/texto";
import { ESTADOS_CONTRATO_CODIGOS, SERVICIOS, presentarEstadoContrato } from "../../../presentacion/usuarios";
import { TablaContratos } from "./TablaContratos";
import "../pqr/bandeja.css";
import "./contratos.css";

const TAMANO_PAGINA = 10;

/** Franja con la cantidad de contratos en cada estado y los candidatos a suspensión. */
function MetricasContratos({ contratos }) {
  const contar = (estado) => contratos.filter((contrato) => contrato.estado === estado).length;
  const indicadores = [
    { etiqueta: "Activos", valor: contar("ACTIVO"), detalle: "Se facturan cada mes" },
    { etiqueta: "Pendientes", valor: contar("PENDIENTE"), detalle: "Sin activar", tono: "plazo" },
    { etiqueta: "Suspendidos", valor: contar("SUSPENDIDO"), detalle: "Servicio suspendido", tono: "vencido" },
    {
      etiqueta: "Candidatos a suspensión",
      valor: contratos.filter((contrato) => contrato.candidatoSuspension).length,
      detalle: "Por mora en el pago",
      tono: "vencido",
    },
  ];
  return (
    <dl className="bandeja-metricas">
      {indicadores.map((indicador) => (
        <div key={indicador.etiqueta}>
          <dt>{indicador.etiqueta}</dt>
          <dd className={indicador.tono ? `bandeja-metrica-${indicador.tono}` : undefined}>{indicador.valor}</dd>
          <dd className="bandeja-metrica-detalle">{indicador.detalle}</dd>
        </div>
      ))}
    </dl>
  );
}

/**
 * Contratos de servicio (CU-02, RF-04, SWR-20) con filtros del backend (estado, servicio, municipio y
 * candidatos a suspensión) y búsqueda local por número o dirección. Los filtros quedan en la URL.
 * Un contrato nuevo se crea desde el perfil del usuario, donde se elige el predio.
 */
export function Contratos() {
  const { filtros, pagina, hayFiltros, cambiarFiltro, cambiarPagina, limpiar } = useFiltrosUrl([
    "q",
    "estado",
    "servicio",
    "municipio",
    "suspension",
  ]);
  const { data: municipios = [] } = useMunicipios();
  const todos = useListaContratos();
  const filtrados = useListaContratos({
    estado: filtros.estado,
    servicio: filtros.servicio,
    municipioId: filtros.municipio,
    candidatoSuspension: filtros.suspension ? true : undefined,
  });

  const resultado = buscarEn(filtrados.data ?? [], filtros.q, ["numeroContrato", "direccionServicio"]);
  const { visibles, paginaActual, totalPaginas, inicio } = paginar(resultado, pagina, TAMANO_PAGINA);
  const alCambiar = (nombre) => (evento) => cambiarFiltro(nombre, evento.target.value);

  return (
    <>
      <EncabezadoGestion
        antetitulo="Gestión comercial"
        titulo="Contratos de servicio"
        descripcion="Consulte contratos, su estado y los candidatos a suspensión. Para crear uno, abra el perfil del usuario."
        accion={
          <Boton a="/gestion/usuarios" variante="secundario" icono="personas">
            Buscar usuario
          </Boton>
        }
      />

      {todos.data && <MetricasContratos contratos={todos.data} />}

      <section className="bandeja-panel" aria-label="Contratos">
        <div className="bandeja-filtros contratos-filtros" role="search">
          <label className="bandeja-buscador">
            <Icono nombre="buscar" tamano={18} />
            <span className="solo-lectores">Buscar</span>
            <input
              type="search"
              placeholder="Buscar número de contrato o dirección..."
              value={filtros.q}
              onChange={alCambiar("q")}
            />
          </label>
          <select aria-label="Filtrar por estado" value={filtros.estado} onChange={alCambiar("estado")}>
            <option value="">Todos los estados</option>
            {ESTADOS_CONTRATO_CODIGOS.map((codigo) => (
              <option key={codigo} value={codigo}>
                {presentarEstadoContrato(codigo).etiqueta}
              </option>
            ))}
          </select>
          <select aria-label="Filtrar por servicio" value={filtros.servicio} onChange={alCambiar("servicio")}>
            <option value="">Todos los servicios</option>
            {SERVICIOS.map((servicio) => (
              <option key={servicio.codigo} value={servicio.codigo}>
                {servicio.nombre}
              </option>
            ))}
          </select>
          <select aria-label="Filtrar por municipio" value={filtros.municipio} onChange={alCambiar("municipio")}>
            <option value="">Todos los municipios</option>
            {municipios.map((municipio) => (
              <option key={municipio.id} value={municipio.id}>
                {municipio.nombre}
              </option>
            ))}
          </select>
          <label className="contratos-casilla">
            <input
              type="checkbox"
              checked={Boolean(filtros.suspension)}
              onChange={(evento) => cambiarFiltro("suspension", evento.target.checked ? "si" : "")}
            />
            Candidatos a suspensión
          </label>
          <Boton variante="discreto" onClick={limpiar} disabled={!hayFiltros}>
            Limpiar
          </Boton>
        </div>

        <div className="bandeja-resumen" aria-live="polite">
          <strong>
            {resultado.length} {resultado.length === 1 ? "contrato" : "contratos"}
          </strong>
          <small>{filtrados.isFetching ? "Actualizando…" : "Ordenados por número"}</small>
        </div>

        {filtrados.isPending && (
          <div className="bandeja-estado">
            <Cargando texto="Cargando contratos…" />
          </div>
        )}
        {filtrados.isError && (
          <div className="bandeja-estado">
            <MensajeError error={filtrados.error} />
          </div>
        )}
        {filtrados.isSuccess && resultado.length === 0 && (
          <div className="bandeja-estado bandeja-vacia">
            <p>{hayFiltros ? "Ningún contrato coincide con los filtros." : "Aún no hay contratos registrados."}</p>
            {hayFiltros && (
              <Boton variante="secundario" onClick={limpiar}>
                Limpiar filtros
              </Boton>
            )}
          </div>
        )}

        {visibles.length > 0 && <TablaContratos contratos={visibles} />}

        <Paginacion
          total={resultado.length}
          pagina={paginaActual}
          totalPaginas={totalPaginas}
          inicio={inicio}
          cantidadVisible={visibles.length}
          onCambiar={cambiarPagina}
        />
      </section>
    </>
  );
}