import { useState } from "react";
import { Link } from "react-router-dom";
import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { Paginacion } from "../../../componentes/Paginacion";
import { Boton } from "../../../componentes/ui/Boton";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import { useAuditoria, useCatalogoAuditoria, useFuncionarios } from "../../../hooks/useAdministracion";
import { useFiltrosUrl } from "../../../hooks/useFiltrosUrl";
import {
  accionesDelModulo,
  presentarAccion,
  presentarEntidad,
  presentarModulo,
  rutaEntidad,
} from "../../../presentacion/administracion";
import { formatearFechaCorta, formatearHora } from "../../../presentacion/fechas";
import "../pqr/bandeja.css";
import "./administracion.css";

const TAMANO_PAGINA = 25;

/**
 * Usuario exacto (el backend no busca por parte del nombre). Se aplica al pulsar Enter o al salir del
 * campo, no con cada letra, y sugiere los usuarios de los funcionarios.
 */
function FiltroUsuario({ inicial, onAplicar }) {
  const [valor, setValor] = useState(inicial);
  const funcionarios = useFuncionarios();
  const aplicar = () => {
    if (valor.trim() !== inicial) onAplicar(valor.trim());
  };

  return (
    <form
      className="bandeja-buscador admin-filtro-usuario"
      onSubmit={(evento) => {
        evento.preventDefault();
        aplicar();
      }}
    >
      <Icono nombre="buscar" tamano={18} />
      <label className="solo-lectores" htmlFor="auditoria-usuario">
        Usuario
      </label>
      <input
        id="auditoria-usuario"
        type="search"
        list="auditoria-usuarios"
        placeholder="Usuario exacto, por ejemplo asesor"
        autoComplete="off"
        autoCapitalize="none"
        spellCheck={false}
        value={valor}
        onChange={(evento) => setValor(evento.target.value)}
        onBlur={aplicar}
      />
      <datalist id="auditoria-usuarios">
        {funcionarios.data?.map((funcionario) => (
          <option key={funcionario.id} value={funcionario.usuario}>
            {funcionario.nombreCompleto}
          </option>
        ))}
      </datalist>
    </form>
  );
}

/** Entidad afectada con enlace a su pantalla cuando existe. */
function Registro({ registro }) {
  if (!registro.entidad) return <span className="admin-sin-dato">—</span>;
  const ruta = rutaEntidad(registro.entidad, registro.entidadId);
  return (
    <span className="bandeja-celda-doble">
      <span>{presentarEntidad(registro.entidad)}</span>
      {registro.entidadId &&
        (ruta ? (
          <Link to={ruta} className="admin-enlace cifra">
            {registro.entidadId}
          </Link>
        ) : (
          <small className="cifra">{registro.entidadId}</small>
        ))}
    </span>
  );
}

function TablaAuditoria({ registros }) {
  return (
    <div className="bandeja-tabla-contenedor">
      <table className="bandeja-tabla auditoria-tabla">
        <thead>
          <tr>
            <th scope="col">Fecha</th>
            <th scope="col">Usuario</th>
            <th scope="col">Operación</th>
            <th scope="col">Registro</th>
            <th scope="col">Detalle</th>
            <th scope="col">Resultado</th>
          </tr>
        </thead>
        <tbody>
          {registros.map((registro) => (
            <tr key={registro.id} className={registro.exitoso ? undefined : "bandeja-fila-vencido"}>
              <td>
                <span className="bandeja-celda-doble">
                  <span className="auditoria-fecha">{formatearFechaCorta(registro.fecha)}</span>
                  <small>{formatearHora(registro.fecha)}</small>
                </span>
              </td>
              <td>
                <span className="bandeja-celda-doble">
                  <strong className="cifra">{registro.usuario ?? "Sistema"}</strong>
                  <small>
                    {registro.rol ?? "Proceso automático"}
                    {registro.ipOrigen && ` · ${registro.ipOrigen}`}
                  </small>
                </span>
              </td>
              <td>
                <span className="bandeja-celda-doble">
                  <span className="auditoria-accion">{presentarAccion(registro.accion)}</span>
                  <small>{presentarModulo(registro.modulo)}</small>
                </span>
              </td>
              <td>
                <Registro registro={registro} />
              </td>
              <td className="auditoria-detalle">{registro.detalle ?? "—"}</td>
              <td>
                <Etiqueta tono={registro.exitoso ? "exito" : "peligro"}>{registro.exitoso ? "Exitosa" : "Fallida"}</Etiqueta>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

/**
 * Log de auditoría (CU-11, RF-15): quién hizo qué operación crítica, cuándo, sobre qué registro y con
 * qué resultado. Es de solo lectura. Los filtros y la página quedan en la URL, así que el detalle de un
 * funcionario puede enlazar a lo que hizo (?usuario=...) o a los cambios de su cuenta (?entidad=...).
 */
export function Auditoria() {
  const { filtros, pagina, hayFiltros, cambiarFiltro, cambiarFiltros, cambiarPagina, limpiar } = useFiltrosUrl([
    "usuario",
    "modulo",
    "accion",
    "desde",
    "hasta",
    "fallidos",
    "entidad",
    "entidadId",
  ]);
  const catalogo = useCatalogoAuditoria();
  const rangoInvalido = Boolean(filtros.desde && filtros.hasta && filtros.desde > filtros.hasta);
  const auditoria = useAuditoria(
    {
      usuario: filtros.usuario || undefined,
      modulo: filtros.modulo || undefined,
      accion: filtros.accion || undefined,
      entidad: filtros.entidad || undefined,
      entidadId: filtros.entidadId || undefined,
      desde: filtros.desde || undefined,
      hasta: filtros.hasta || undefined,
      soloFallidos: filtros.fallidos ? true : undefined,
      pagina: pagina - 1,
      tamano: TAMANO_PAGINA,
    },
    { habilitada: !rangoInvalido },
  );
  const datos = auditoria.data;
  const acciones = accionesDelModulo(catalogo.data?.acciones ?? [], filtros.modulo);
  const alCambiar = (nombre) => (evento) => cambiarFiltro(nombre, evento.target.value);

  return (
    <>
      <EncabezadoGestion
        antetitulo="Administración"
        titulo="Auditoría"
        descripcion="Registro de las operaciones críticas del sistema: quién, qué, cuándo y con qué resultado. No se puede modificar."
      />

      <section className="bandeja-panel" aria-label="Registros de auditoría">
        <div className="bandeja-filtros auditoria-filtros" role="search">
          <FiltroUsuario key={filtros.usuario} inicial={filtros.usuario} onAplicar={(valor) => cambiarFiltro("usuario", valor)} />
          <select
            aria-label="Filtrar por módulo"
            value={filtros.modulo}
            onChange={(evento) => cambiarFiltros({ modulo: evento.target.value, accion: "" })}
          >
            <option value="">Todos los módulos</option>
            {catalogo.data?.modulos.map((codigo) => (
              <option key={codigo} value={codigo}>
                {presentarModulo(codigo)}
              </option>
            ))}
          </select>
          <select aria-label="Filtrar por operación" value={filtros.accion} onChange={alCambiar("accion")}>
            <option value="">Todas las operaciones</option>
            {acciones.map((codigo) => (
              <option key={codigo} value={codigo}>
                {presentarAccion(codigo)}
              </option>
            ))}
          </select>
          <label className="auditoria-fecha-filtro">
            <span>Desde</span>
            <input
              type="date"
              value={filtros.desde}
              max={filtros.hasta || undefined}
              aria-invalid={rangoInvalido || undefined}
              onChange={alCambiar("desde")}
            />
          </label>
          <label className="auditoria-fecha-filtro">
            <span>Hasta</span>
            <input
              type="date"
              value={filtros.hasta}
              min={filtros.desde || undefined}
              aria-invalid={rangoInvalido || undefined}
              onChange={alCambiar("hasta")}
            />
          </label>
          <label className="admin-casilla">
            <input
              type="checkbox"
              checked={Boolean(filtros.fallidos)}
              onChange={(evento) => cambiarFiltro("fallidos", evento.target.checked ? "si" : "")}
            />
            Solo fallidas
          </label>
          <Boton variante="discreto" onClick={limpiar} disabled={!hayFiltros}>
            Limpiar
          </Boton>
        </div>

        {filtros.entidad && (
          <div className="auditoria-entidad">
            <span>
              Registro: <strong>{presentarEntidad(filtros.entidad)}</strong>
              {filtros.entidadId && <span className="cifra"> {filtros.entidadId}</span>}
            </span>
            <button
              type="button"
              aria-label="Quitar el filtro de registro"
              onClick={() => cambiarFiltros({ entidad: "", entidadId: "" })}
            >
              <Icono nombre="cerrar" tamano={14} />
            </button>
          </div>
        )}

        <div className="bandeja-resumen" aria-live="polite">
          <strong>
            {datos ? datos.totalElementos.toLocaleString("es-CO") : 0}{" "}
            {datos?.totalElementos === 1 ? "registro" : "registros"}
          </strong>
          <small>{auditoria.isFetching ? "Actualizando…" : "Del más reciente al más antiguo"}</small>
        </div>

        {rangoInvalido && (
          <div className="bandeja-estado">
            <p className="campo-error" role="alert">
              La fecha inicial no puede ser posterior a la final.
            </p>
          </div>
        )}
        {auditoria.isPending && !rangoInvalido && (
          <div className="bandeja-estado">
            <Cargando texto="Cargando la auditoría…" />
          </div>
        )}
        {auditoria.isError && (
          <div className="bandeja-estado">
            <MensajeError error={auditoria.error} />
          </div>
        )}
        {auditoria.isSuccess && datos.contenido.length === 0 && (
          <div className="bandeja-estado bandeja-vacia">
            <p>{hayFiltros ? "Ningún registro coincide con los filtros." : "Aún no hay registros de auditoría."}</p>
            {hayFiltros && (
              <Boton variante="secundario" onClick={limpiar}>
                Limpiar filtros
              </Boton>
            )}
          </div>
        )}

        {datos?.contenido.length > 0 && <TablaAuditoria registros={datos.contenido} />}

        {datos && (
          <Paginacion
            total={datos.totalElementos}
            pagina={pagina}
            totalPaginas={datos.totalPaginas}
            inicio={(pagina - 1) * TAMANO_PAGINA}
            cantidadVisible={datos.contenido.length}
            onCambiar={cambiarPagina}
          />
        )}
      </section>
    </>
  );
}