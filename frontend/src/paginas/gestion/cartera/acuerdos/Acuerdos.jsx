import { Link, useNavigate } from "react-router-dom";
import { Cargando } from "../../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../../componentes/MensajeError";
import { Paginacion } from "../../../../componentes/Paginacion";
import { Boton } from "../../../../componentes/ui/Boton";
import { Etiqueta } from "../../../../componentes/ui/Etiqueta";
import { Icono } from "../../../../componentes/ui/Icono";
import { useAcuerdos } from "../../../../hooks/useCartera";
import { useFiltrosUrl } from "../../../../hooks/useFiltrosUrl";
import {
  ESTADOS_ACUERDO_CODIGOS,
  contarPorEstado,
  porcentajePagado,
  presentarEstadoAcuerdo,
  siguienteCuota,
} from "../../../../presentacion/cartera";
import { formatearMoneda } from "../../../../presentacion/facturacion";
import { formatearFecha, formatearFechaCorta } from "../../../../presentacion/fechas";
import { paginar } from "../../../../presentacion/paginacion";
import { formatearRecaudo } from "../../../../presentacion/pagos";
import { buscarEn } from "../../../../presentacion/texto";
import "../../pqr/bandeja.css";
import "../cartera.css";

const TAMANO_PAGINA = 10;

function MetricasAcuerdos({ acuerdos }) {
  const conteo = contarPorEstado(acuerdos);
  const saldo = acuerdos
    .filter((acuerdo) => acuerdo.estado === "VIGENTE")
    .reduce((suma, acuerdo) => suma + Number(acuerdo.saldoPendiente), 0);
  const indicadores = [
    { etiqueta: "Vigentes", valor: conteo.VIGENTE, detalle: "Cobro coercitivo suspendido" },
    {
      etiqueta: "Por recaudar",
      valor: formatearRecaudo(saldo),
      detalle: "Saldo de los vigentes",
      clase: "cartera-metrica-moneda",
    },
    { etiqueta: "Incumplidos", valor: conteo.INCUMPLIDO, detalle: "Cuota vencida sin pago", clase: "bandeja-metrica-vencido" },
    { etiqueta: "Cumplidos", valor: conteo.CUMPLIDO, detalle: "Facturas saldadas" },
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

/** Barra de avance del pago de un acuerdo. */
function Avance({ acuerdo }) {
  const porcentaje = porcentajePagado(acuerdo);
  return (
    <span className="acuerdos-avance">
      <span className="acuerdos-avance-barra" aria-hidden="true">
        <span style={{ width: `${porcentaje}%` }} />
      </span>
      <small>
        {acuerdo.cuotas.filter((cuota) => cuota.estado === "PAGADA").length} de {acuerdo.numeroCuotas} cuotas
      </small>
    </span>
  );
}

function TablaAcuerdos({ acuerdos }) {
  const navegar = useNavigate();
  const abrirDesdeFila = (numero) => (evento) => {
    if (!evento.target.closest("a")) navegar(`/gestion/cartera/acuerdos/${numero}`);
  };

  return (
    <div className="bandeja-tabla-contenedor">
      <table className="bandeja-tabla">
        <thead>
          <tr>
            <th scope="col">Acuerdo</th>
            <th scope="col">Usuario y contrato</th>
            <th scope="col">Deuda</th>
            <th scope="col">Avance</th>
            <th scope="col">Próxima cuota</th>
            <th scope="col">Estado</th>
            <th scope="col">
              <span className="solo-lectores">Acciones</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {acuerdos.map((acuerdo) => {
            const estado = presentarEstadoAcuerdo(acuerdo.estado);
            const proxima = acuerdo.estado === "VIGENTE" ? siguienteCuota(acuerdo) : null;
            return (
              <tr
                key={acuerdo.numeroAcuerdo}
                className={acuerdo.estado === "INCUMPLIDO" ? "bandeja-fila-vencido" : undefined}
                onClick={abrirDesdeFila(acuerdo.numeroAcuerdo)}
              >
                <td>
                  <span className="bandeja-celda-doble">
                    <Link to={`/gestion/cartera/acuerdos/${acuerdo.numeroAcuerdo}`} className="bandeja-radicado cifra">
                      {acuerdo.numeroAcuerdo}
                    </Link>
                    <small>Desde el {formatearFechaCorta(acuerdo.fechaAcuerdo)}</small>
                  </span>
                </td>
                <td>
                  <span className="bandeja-celda-doble">
                    <strong>{acuerdo.usuario}</strong>
                    <small>
                      <span className="cifra">{acuerdo.numeroContrato}</span> · {acuerdo.servicio}
                    </small>
                  </span>
                </td>
                <td>
                  <span className="bandeja-celda-doble">
                    <strong className="cifra">{formatearMoneda(acuerdo.totalDeuda)}</strong>
                    <small>Saldo {formatearMoneda(acuerdo.saldoPendiente)}</small>
                  </span>
                </td>
                <td>
                  <Avance acuerdo={acuerdo} />
                </td>
                <td>
                  {proxima ? (
                    <span className="bandeja-celda-doble">
                      <span className="cifra">{formatearMoneda(proxima.valor)}</span>
                      <small>
                        Cuota {proxima.numeroCuota} · {formatearFecha(proxima.fechaVencimiento)}
                      </small>
                    </span>
                  ) : (
                    "—"
                  )}
                </td>
                <td>
                  <Etiqueta tono={estado.tono}>{estado.etiqueta}</Etiqueta>
                </td>
                <td>
                  <Link
                    to={`/gestion/cartera/acuerdos/${acuerdo.numeroAcuerdo}`}
                    className="bandeja-ver"
                    aria-label={`Ver acuerdo ${acuerdo.numeroAcuerdo}`}
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

/** Acuerdos de pago (CU-08, RF-12, RN-07) filtrables por estado, con búsqueda por número, contrato o usuario. */
export function Acuerdos() {
  const { filtros, pagina, hayFiltros, cambiarFiltro, cambiarPagina, limpiar } = useFiltrosUrl(["q", "estado"]);
  const todos = useAcuerdos();
  const filtrados = useAcuerdos({ estado: filtros.estado || undefined });

  const resultado = buscarEn(filtrados.data ?? [], filtros.q, ["numeroAcuerdo", "numeroContrato", "usuario"]);
  const { visibles, paginaActual, totalPaginas, inicio } = paginar(resultado, pagina, TAMANO_PAGINA);

  return (
    <>
      <EncabezadoGestion
        antetitulo="Cobro persuasivo"
        titulo="Acuerdos de pago"
        descripcion="Deudas vencidas pactadas en cuotas. Mientras el acuerdo esté vigente, el cobro coercitivo queda suspendido."
        accion={
          <Boton a="/gestion/cartera/acuerdos/nuevo" icono="mas">
            Nuevo acuerdo
          </Boton>
        }
      />

      {todos.data && <MetricasAcuerdos acuerdos={todos.data} />}

      <section className="bandeja-panel" aria-label="Acuerdos de pago">
        <div className="bandeja-filtros" role="search">
          <label className="bandeja-buscador">
            <Icono nombre="buscar" tamano={18} />
            <span className="solo-lectores">Buscar</span>
            <input
              type="search"
              placeholder="Buscar acuerdo, contrato o usuario..."
              value={filtros.q}
              onChange={(evento) => cambiarFiltro("q", evento.target.value)}
            />
          </label>
          <select
            aria-label="Filtrar por estado"
            value={filtros.estado}
            onChange={(evento) => cambiarFiltro("estado", evento.target.value)}
          >
            <option value="">Todos los estados</option>
            {ESTADOS_ACUERDO_CODIGOS.map((codigo) => (
              <option key={codigo} value={codigo}>
                {presentarEstadoAcuerdo(codigo).etiqueta}
              </option>
            ))}
          </select>
          <Boton variante="discreto" onClick={limpiar} disabled={!hayFiltros}>
            Limpiar
          </Boton>
        </div>

        <div className="bandeja-resumen" aria-live="polite">
          <strong>
            {resultado.length} {resultado.length === 1 ? "acuerdo" : "acuerdos"}
          </strong>
          <small>{filtrados.isFetching ? "Actualizando…" : "Del más reciente al más antiguo"}</small>
        </div>

        {filtrados.isPending && (
          <div className="bandeja-estado">
            <Cargando texto="Cargando acuerdos…" />
          </div>
        )}
        {filtrados.isError && (
          <div className="bandeja-estado">
            <MensajeError error={filtrados.error} />
          </div>
        )}
        {filtrados.isSuccess && resultado.length === 0 && (
          <div className="bandeja-estado bandeja-vacia">
            <p>{hayFiltros ? "Ningún acuerdo coincide con los filtros." : "Aún no hay acuerdos de pago registrados."}</p>
          </div>
        )}

        {visibles.length > 0 && <TablaAcuerdos acuerdos={visibles} />}

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