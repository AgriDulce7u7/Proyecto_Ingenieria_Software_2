import { Link, useNavigate } from "react-router-dom";
import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { Paginacion } from "../../../componentes/Paginacion";
import { Boton } from "../../../componentes/ui/Boton";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import { useFuncionarios, useRoles } from "../../../hooks/useAdministracion";
import { useFiltrosUrl } from "../../../hooks/useFiltrosUrl";
import { ESTADOS_CUENTA, presentarEstadoFuncionario } from "../../../presentacion/administracion";
import { formatearTiempoRelativo } from "../../../presentacion/fechas";
import { paginar } from "../../../presentacion/paginacion";
import { buscarEn } from "../../../presentacion/texto";
import "../pqr/bandeja.css";
import "./administracion.css";

const TAMANO_PAGINA = 15;

/** El backend filtra por activo; el bloqueo por intentos fallidos se filtra aquí. */
const filtroActivo = (estado) => (estado === "inactivas" ? false : estado ? true : undefined);

function MetricasFuncionarios({ funcionarios }) {
  const estados = funcionarios.map((funcionario) => presentarEstadoFuncionario(funcionario).codigo);
  const contar = (codigo) => estados.filter((estado) => estado === codigo).length;
  const indicadores = [
    { etiqueta: "Cuentas", valor: funcionarios.length, detalle: "Funcionarios registrados" },
    { etiqueta: "Activas", valor: contar("ACTIVO"), detalle: "Pueden iniciar sesión" },
    {
      etiqueta: "Bloqueadas",
      valor: contar("BLOQUEADO"),
      detalle: "Por intentos fallidos",
      clase: contar("BLOQUEADO") > 0 ? "bandeja-metrica-vencido" : undefined,
    },
    { etiqueta: "Inactivas", valor: contar("INACTIVO"), detalle: "Sin acceso al sistema" },
  ];
  return (
    <dl className="bandeja-metricas">
      {indicadores.map((indicador) => (
        <div key={indicador.etiqueta}>
          <dt>{indicador.etiqueta}</dt>
          <dd className={indicador.clase}>{indicador.valor}</dd>
          <dd className="bandeja-metrica-detalle">{indicador.detalle}</dd>
        </div>
      ))}
    </dl>
  );
}

function TablaFuncionarios({ funcionarios }) {
  const navegar = useNavigate();
  const abrirDesdeFila = (id) => (evento) => {
    if (!evento.target.closest("a")) navegar(`/gestion/administracion/funcionarios/${id}`);
  };

  return (
    <div className="bandeja-tabla-contenedor">
      <table className="bandeja-tabla admin-tabla">
        <thead>
          <tr>
            <th scope="col">Funcionario</th>
            <th scope="col">Rol</th>
            <th scope="col">Correo</th>
            <th scope="col">Último acceso</th>
            <th scope="col">Estado</th>
            <th scope="col">
              <span className="solo-lectores">Acciones</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {funcionarios.map((funcionario) => {
            const estado = presentarEstadoFuncionario(funcionario);
            return (
              <tr
                key={funcionario.id}
                className={estado.codigo === "BLOQUEADO" ? "bandeja-fila-vencido" : undefined}
                onClick={abrirDesdeFila(funcionario.id)}
              >
                <td>
                  <span className="bandeja-celda-doble">
                    <Link to={`/gestion/administracion/funcionarios/${funcionario.id}`} className="admin-nombre">
                      {funcionario.nombreCompleto}
                    </Link>
                    <small className="cifra">{funcionario.usuario}</small>
                  </span>
                </td>
                <td>
                  <span className="bandeja-celda-doble">
                    <span>{funcionario.descripcionRol}</span>
                    {funcionario.municipio && <small>{funcionario.municipio}</small>}
                  </span>
                </td>
                <td className="admin-correo">{funcionario.correo}</td>
                <td>{funcionario.ultimoAcceso ? formatearTiempoRelativo(funcionario.ultimoAcceso) : "Nunca"}</td>
                <td>
                  <Etiqueta tono={estado.tono}>{estado.etiqueta}</Etiqueta>
                </td>
                <td>
                  <Link
                    to={`/gestion/administracion/funcionarios/${funcionario.id}`}
                    className="bandeja-ver"
                    aria-label={`Ver la cuenta de ${funcionario.nombreCompleto}`}
                  >
                    <Icono nombre="chevron" tamano={18} />
                  </Link>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

/**
 * Cuentas de funcionarios (CU-10, RF-14) filtrables por rol y estado, con búsqueda por nombre, usuario o
 * correo. Los filtros quedan en la URL.
 */
export function Funcionarios() {
  const { filtros, pagina, hayFiltros, cambiarFiltro, cambiarPagina, limpiar } = useFiltrosUrl(["q", "rol", "estado"]);
  const roles = useRoles();
  const todos = useFuncionarios();
  const filtrados = useFuncionarios({ rol: filtros.rol || undefined, activo: filtroActivo(filtros.estado) });

  const porEstado = (filtrados.data ?? []).filter((funcionario) => {
    const codigo = presentarEstadoFuncionario(funcionario).codigo;
    if (filtros.estado === "bloqueadas") return codigo === "BLOQUEADO";
    if (filtros.estado === "activas") return codigo === "ACTIVO";
    return true;
  });
  const resultado = buscarEn(porEstado, filtros.q, ["nombreCompleto", "usuario", "correo"]);
  const { visibles, paginaActual, totalPaginas, inicio } = paginar(resultado, pagina, TAMANO_PAGINA);

  return (
    <>
      <EncabezadoGestion
        antetitulo="Administración"
        titulo="Funcionarios"
        descripcion="Cuentas de acceso al back-office. El rol de cada cuenta define qué puede consultar y hacer."
        accion={
          <Boton a="/gestion/administracion/funcionarios/nuevo" icono="mas">
            Nuevo funcionario
          </Boton>
        }
      />

      {todos.data && <MetricasFuncionarios funcionarios={todos.data} />}

      <section className="bandeja-panel" aria-label="Funcionarios">
        <div className="bandeja-filtros" role="search">
          <label className="bandeja-buscador">
            <Icono nombre="buscar" tamano={18} />
            <span className="solo-lectores">Buscar</span>
            <input
              type="search"
              placeholder="Buscar nombre, usuario o correo..."
              value={filtros.q}
              onChange={(evento) => cambiarFiltro("q", evento.target.value)}
            />
          </label>
          <select
            aria-label="Filtrar por rol"
            value={filtros.rol}
            onChange={(evento) => cambiarFiltro("rol", evento.target.value)}
          >
            <option value="">Todos los roles</option>
            {(roles.data ?? [])
              .filter((rol) => rol.interno)
              .map((rol) => (
                <option key={rol.rol} value={rol.rol}>
                  {rol.descripcion}
                </option>
              ))}
          </select>
          <select
            aria-label="Filtrar por estado"
            value={filtros.estado}
            onChange={(evento) => cambiarFiltro("estado", evento.target.value)}
          >
            <option value="">Todos los estados</option>
            {ESTADOS_CUENTA.map((estado) => (
              <option key={estado.valor} value={estado.valor}>
                {estado.texto}
              </option>
            ))}
          </select>
          <Boton variante="discreto" onClick={limpiar} disabled={!hayFiltros}>
            Limpiar
          </Boton>
        </div>

        <div className="bandeja-resumen" aria-live="polite">
          <strong>
            {resultado.length} {resultado.length === 1 ? "funcionario" : "funcionarios"}
          </strong>
          <small>{filtrados.isFetching ? "Actualizando…" : "Por nombre"}</small>
        </div>

        {filtrados.isPending && (
          <div className="bandeja-estado">
            <Cargando texto="Cargando funcionarios…" />
          </div>
        )}
        {filtrados.isError && (
          <div className="bandeja-estado">
            <MensajeError error={filtrados.error} />
          </div>
        )}
        {filtrados.isSuccess && resultado.length === 0 && (
          <div className="bandeja-estado bandeja-vacia">
            <p>{hayFiltros ? "Ningún funcionario coincide con los filtros." : "Aún no hay funcionarios registrados."}</p>
            {hayFiltros && (
              <Boton variante="secundario" onClick={limpiar}>
                Limpiar filtros
              </Boton>
            )}
          </div>
        )}

        {visibles.length > 0 && <TablaFuncionarios funcionarios={visibles} />}

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