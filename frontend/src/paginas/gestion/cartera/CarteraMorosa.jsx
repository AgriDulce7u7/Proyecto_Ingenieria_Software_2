import { useState } from "react";
import { exportarReporteCartera } from "../../../api/cartera";
import { BotonDescarga } from "../../../componentes/BotonDescarga";
import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { Paginacion } from "../../../componentes/Paginacion";
import { Boton } from "../../../componentes/ui/Boton";
import { Icono } from "../../../componentes/ui/Icono";
import { useReporteCartera } from "../../../hooks/useCartera";
import { useFiltrosUrl } from "../../../hooks/useFiltrosUrl";
import { useMunicipios } from "../../../hooks/useTerritorio";
import { RANGOS_MORA } from "../../../presentacion/cartera";
import { formatearMoneda } from "../../../presentacion/facturacion";
import { formatearFechaHora } from "../../../presentacion/fechas";
import { paginar } from "../../../presentacion/paginacion";
import { formatearRecaudo } from "../../../presentacion/pagos";
import { buscarEn } from "../../../presentacion/texto";
import { TablaMorosos } from "./TablaMorosos";
import "../pqr/bandeja.css";
import "./cartera.css";

const TAMANO_PAGINA = 10;

/** Opciones del filtro de periodos consecutivos en mora. */
const PERIODOS = [
  { valor: "", texto: "Cualquier antigüedad" },
  { valor: "2", texto: "2 o más periodos seguidos" },
  { valor: "3", texto: "3 o más periodos seguidos" },
  { valor: "6", texto: "6 o más periodos seguidos" },
];

function MetricasCartera({ reporte }) {
  const indicadores = [
    {
      etiqueta: "Deuda en mora",
      valor: formatearRecaudo(reporte.deudaTotal),
      detalle: "Facturas vencidas sin acuerdo",
      clase: "cartera-metrica-moneda",
    },
    { etiqueta: "Contratos en mora", valor: reporte.contratosEnMora, detalle: "Con al menos una factura vencida" },
    { etiqueta: "Usuarios en mora", valor: reporte.usuariosEnMora, detalle: "Titulares distintos" },
    {
      etiqueta: "Candidatos a suspensión",
      valor: reporte.candidatosSuspension,
      detalle: "Mora seguida sin acuerdo",
      clase: "bandeja-metrica-vencido",
    },
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

/** Deuda por antigüedad de la mora: barras horizontales con la proporción de cada rango sobre el total. */
function DeudaPorRango({ porRango, total }) {
  const totalNumero = Number(total);
  return (
    <section className="panel cartera-rangos" aria-labelledby="rangos-titulo">
      <div className="panel-titulo">
        <h2 id="rangos-titulo">Deuda por antigüedad</h2>
        <span className="panel-nota">Desde la factura vencida más antigua</span>
      </div>
      <ul>
        {RANGOS_MORA.map((rango) => {
          const valor = Number(porRango?.[rango.codigo] ?? 0);
          const porcentaje = totalNumero > 0 ? (valor / totalNumero) * 100 : 0;
          return (
            <li key={rango.codigo}>
              <div className="cartera-rango-cabecera">
                <strong>{rango.codigo}</strong>
                <span className="cifra">{formatearMoneda(valor)}</span>
              </div>
              <div
                className="cartera-rango-barra"
                role="img"
                aria-label={`${rango.codigo}: ${Math.round(porcentaje)} % de la deuda`}
              >
                <span data-tono={rango.tono} style={{ width: `${porcentaje}%` }} />
              </div>
            </li>
          );
        })}
      </ul>
    </section>
  );
}

/** Botones de exportación del reporte con los filtros aplicados (SWR-29). */
function ExportarCartera({ filtros }) {
  const [error, setError] = useState(null);
  return (
    <div className="cartera-exportar">
      <div>
        <BotonDescarga descargar={() => exportarReporteCartera("pdf", filtros)} onError={setError}>
          PDF
        </BotonDescarga>
        <BotonDescarga
          descargar={() => exportarReporteCartera("excel", filtros)}
          textoCargando="Generando Excel…"
          onError={setError}
        >
          Excel
        </BotonDescarga>
      </div>
      <MensajeError error={error} />
    </div>
  );
}

/**
 * Cartera morosa (CU-08, RF-11, SWR-29, SWR-32): contratos con facturas vencidas que no están en un acuerdo,
 * su deuda, antigüedad y si son candidatos a suspensión. Los filtros del backend (municipio, periodos
 * seguidos y solo candidatos) también se aplican a la exportación; la búsqueda por texto es local.
 */
export function CarteraMorosa() {
  const { filtros, pagina, hayFiltros, cambiarFiltro, cambiarPagina, limpiar } = useFiltrosUrl([
    "q",
    "municipio",
    "periodos",
    "candidatos",
  ]);
  const { data: municipios = [] } = useMunicipios();
  const filtrosBackend = {
    municipioId: filtros.municipio || undefined,
    minPeriodos: filtros.periodos || undefined,
    soloCandidatos: filtros.candidatos ? true : undefined,
  };
  const reporte = useReporteCartera(filtrosBackend);
  const datos = reporte.data;

  const resultado = buscarEn(datos?.morosos ?? [], filtros.q, ["numeroContrato", "nombreUsuario", "documentoUsuario"]);
  const { visibles, paginaActual, totalPaginas, inicio } = paginar(resultado, pagina, TAMANO_PAGINA);

  return (
    <>
      <EncabezadoGestion
        antetitulo="Cobro persuasivo"
        titulo="Cartera morosa"
        descripcion="Usuarios con facturas vencidas que no están en un acuerdo de pago, con su deuda y la antigüedad de la mora."
        accion={datos && <ExportarCartera filtros={filtrosBackend} />}
      />

      {datos && <MetricasCartera reporte={datos} />}
      {datos && <DeudaPorRango porRango={datos.deudaPorRango} total={datos.deudaTotal} />}

      <section className="bandeja-panel" aria-label="Contratos en mora">
        <div className="bandeja-filtros cartera-filtros" role="search">
          <label className="bandeja-buscador">
            <Icono nombre="buscar" tamano={18} />
            <span className="solo-lectores">Buscar</span>
            <input
              type="search"
              placeholder="Buscar contrato, usuario o documento..."
              value={filtros.q}
              onChange={(evento) => cambiarFiltro("q", evento.target.value)}
            />
          </label>
          <select
            aria-label="Filtrar por municipio"
            value={filtros.municipio}
            onChange={(evento) => cambiarFiltro("municipio", evento.target.value)}
          >
            <option value="">Todos los municipios</option>
            {municipios.map((municipio) => (
              <option key={municipio.id} value={municipio.id}>
                {municipio.nombre}
              </option>
            ))}
          </select>
          <select
            aria-label="Filtrar por periodos seguidos en mora"
            value={filtros.periodos}
            onChange={(evento) => cambiarFiltro("periodos", evento.target.value)}
          >
            {PERIODOS.map((opcion) => (
              <option key={opcion.valor} value={opcion.valor}>
                {opcion.texto}
              </option>
            ))}
          </select>
          <label className="cartera-casilla">
            <input
              type="checkbox"
              checked={Boolean(filtros.candidatos)}
              onChange={(evento) => cambiarFiltro("candidatos", evento.target.checked ? "si" : "")}
            />
            Solo candidatos a suspensión
          </label>
          <Boton variante="discreto" onClick={limpiar} disabled={!hayFiltros}>
            Limpiar
          </Boton>
        </div>

        <div className="bandeja-resumen" aria-live="polite">
          <strong>
            {resultado.length} {resultado.length === 1 ? "contrato en mora" : "contratos en mora"}
          </strong>
          <small>
            {reporte.isFetching ? "Actualizando…" : datos && `Generado el ${formatearFechaHora(datos.generadoEn)}`}
          </small>
        </div>

        {reporte.isPending && (
          <div className="bandeja-estado">
            <Cargando texto="Calculando la cartera…" />
          </div>
        )}
        {reporte.isError && (
          <div className="bandeja-estado">
            <MensajeError error={reporte.error} />
          </div>
        )}
        {reporte.isSuccess && resultado.length === 0 && (
          <div className="bandeja-estado bandeja-vacia">
            <p>
              {hayFiltros
                ? "Ningún contrato en mora coincide con los filtros."
                : "No hay cartera morosa: todos están al día."}
            </p>
            {hayFiltros && (
              <Boton variante="secundario" onClick={limpiar}>
                Limpiar filtros
              </Boton>
            )}
          </div>
        )}

        {visibles.length > 0 && <TablaMorosos morosos={visibles} />}

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