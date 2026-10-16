import { useRef, useState } from "react";
import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { Boton } from "../../../componentes/ui/Boton";
import { useDashboard } from "../../../hooks/useDashboard";
import { useFiltrosUrl } from "../../../hooks/useFiltrosUrl";
import { formatearValor, indicadoresDe, tieneTendencia } from "../../../presentacion/dashboard";
import { partesPeriodo } from "../../../presentacion/facturacion";
import { formatearFechaHora } from "../../../presentacion/fechas";
import { GraficaTendencia, TablaTendencia } from "./GraficaTendencia";
import { TarjetaIndicador, Variacion } from "./TarjetaIndicador";
import "./dashboard.css";

/** Indicador que se muestra en la gráfica principal si la URL no indica otro. */
const INDICADOR_INICIAL = "RECAUDO_MENSUAL";

/** Movimiento suave al subir a la gráfica, salvo que el usuario prefiera reducir las animaciones. */
const comportamientoDesplazamiento = () =>
  window.matchMedia("(prefers-reduced-motion: reduce)").matches ? "auto" : "smooth";

/** Gráfica principal con el indicador elegido: valor, variación, tendencia de 12 meses y vista en tabla. */
function Destacado({ indicador, mesReferencia }) {
  const [vista, setVista] = useState("grafica");
  const mesEnCurso = indicador.tendencia.at(-1)?.periodo === mesReferencia;

  return (
    <section className="panel dashboard-destacado" aria-labelledby="destacado-titulo">
      <div className="dashboard-destacado-cabecera">
        <div>
          <span className="antetitulo">{indicador.categoria}</span>
          <h2 id="destacado-titulo">{indicador.nombre}</h2>
          <div className="dashboard-destacado-valor">
            <strong className="cifra">{formatearValor(indicador.valorActual, indicador.unidad)}</strong>
            <Variacion indicador={indicador} />
          </div>
          <p>{indicador.descripcion}</p>
        </div>
        <div className="dashboard-vistas" role="group" aria-label="Vista de la tendencia">
          <button type="button" aria-pressed={vista === "grafica"} onClick={() => setVista("grafica")}>
            Gráfica
          </button>
          <button type="button" aria-pressed={vista === "tabla"} onClick={() => setVista("tabla")}>
            Tabla
          </button>
        </div>
      </div>

      {vista === "grafica" ? (
        <GraficaTendencia key={indicador.codigo} indicador={indicador} mesReferencia={mesReferencia} />
      ) : (
        <TablaTendencia indicador={indicador} mesReferencia={mesReferencia} />
      )}

      <p className="dashboard-nota">
        Últimos 12 meses.
        {mesEnCurso &&
          ` ${partesPeriodo(mesReferencia).mes} es el mes en curso: su valor va hasta hoy y puede cambiar antes del cierre. La variación lo compara con el mes anterior completo.`}
      </p>
    </section>
  );
}

/** Indicadores de una categoría (gestión comercial, facturación, cartera o PQR) como tarjetas. */
function Categoria({ categoria, seleccionado, onSeleccionar }) {
  const id = `categoria-${categoria.categoria.toLowerCase().replaceAll(" ", "-")}`;
  return (
    <section className="dashboard-categoria" aria-labelledby={id}>
      <h2 id={id}>{categoria.categoria}</h2>
      <ul className="dashboard-tarjetas">
        {categoria.indicadores.map((indicador) => (
          <TarjetaIndicador
            key={indicador.codigo}
            indicador={indicador}
            seleccionado={indicador.codigo === seleccionado}
            onSeleccionar={onSeleccionar}
          />
        ))}
      </ul>
    </section>
  );
}

/**
 * Dashboard gerencial (CU-09, RF-13): indicadores de gestión comercial, facturación, cartera y PQR con su
 * tendencia de 12 meses (SWR-35). Solo para Gerente y Directivo (SWR-33). El backend los calcula en cada
 * consulta sobre los datos operativos (SWR-34): la pantalla los vuelve a pedir cada 5 minutos o al
 * pulsar «Actualizar». El indicador de la gráfica principal queda en la URL (?indicador=...).
 */
export function Dashboard() {
  const dashboard = useDashboard();
  const { filtros, cambiarFiltro } = useFiltrosUrl(["indicador"], "indicador");
  const grafica = useRef(null);
  const datos = dashboard.data;

  const graficables = datos ? indicadoresDe(datos).filter(tieneTendencia) : [];
  const seleccionado =
    graficables.find((indicador) => indicador.codigo === filtros.indicador) ??
    graficables.find((indicador) => indicador.codigo === INDICADOR_INICIAL) ??
    graficables[0];

  const seleccionar = (codigo) => {
    cambiarFiltro("indicador", codigo);
    grafica.current?.scrollIntoView({ behavior: comportamientoDesplazamiento(), block: "start" });
  };

  return (
    <>
      <EncabezadoGestion
        antetitulo="Gerencia"
        titulo="Dashboard gerencial"
        descripcion="Indicadores de gestión comercial, facturación, cartera y PQR calculados en el momento con los datos del sistema."
        accion={
          datos && (
            <div className="dashboard-actualizacion">
              <Boton
                variante="secundario"
                icono="sincronizar"
                onClick={() => dashboard.refetch()}
                disabled={dashboard.isFetching}
              >
                {dashboard.isFetching ? "Actualizando…" : "Actualizar"}
              </Boton>
              <small aria-live="polite">Calculado el {formatearFechaHora(datos.generadoEn)}</small>
            </div>
          )
        }
      />

      {dashboard.isPending && <Cargando texto="Calculando los indicadores…" />}
      <MensajeError error={dashboard.error} />

      {datos && (
        <div className="dashboard">
          <div ref={grafica} className="dashboard-ancla">
            {seleccionado && <Destacado indicador={seleccionado} mesReferencia={datos.mesReferencia} />}
          </div>
          {(datos.categorias ?? []).map((categoria) => (
            <Categoria
              key={categoria.categoria}
              categoria={categoria}
              seleccionado={seleccionado?.codigo}
              onSeleccionar={seleccionar}
            />
          ))}
        </div>
      )}
    </>
  );
}