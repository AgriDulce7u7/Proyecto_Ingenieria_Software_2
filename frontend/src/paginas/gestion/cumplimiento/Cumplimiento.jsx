import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { useCumplimientoPqr } from "../../../hooks/usePqr";
import { useFiltrosUrl } from "../../../hooks/useFiltrosUrl";
import { BarrasCumplimiento } from "./BarrasCumplimiento";
import { ExportarCumplimiento } from "./ExportarCumplimiento";
import { FiltroPeriodo } from "./FiltroPeriodo";
import { ResumenCumplimiento } from "./ResumenCumplimiento";
import { TablaIncumplimientos } from "./TablaIncumplimientos";
import "./cumplimiento.css";

/**
 * Indicador de cumplimiento del plazo normativo de PQR (SyR-02f, RN-04) para el reporte a la SSPD:
 * porcentaje respondido dentro del plazo, desglose por tipo y municipio, PQR que incumplieron y
 * exportación a PDF o Excel. El periodo queda en la URL (?desde=…&hasta=…).
 */
export function Cumplimiento() {
  const { filtros, cambiarFiltros } = useFiltrosUrl(["desde", "hasta"]);
  const periodo = { desde: filtros.desde || undefined, hasta: filtros.hasta || undefined };
  const cumplimiento = useCumplimientoPqr(periodo);
  const datos = cumplimiento.data;

  // Sin fechas en la URL se muestran las del periodo por defecto que devolvió el backend
  const desde = filtros.desde || datos?.desde || "";
  const hasta = filtros.hasta || datos?.hasta || "";

  return (
    <>
      <EncabezadoGestion
        antetitulo="Indicadores de PQR"
        titulo="Cumplimiento de plazos"
        descripcion="Solicitudes respondidas dentro del plazo legal (10 días hábiles para peticiones, 15 para quejas y reclamos)."
        accion={datos && <ExportarCumplimiento desde={datos.desde} hasta={datos.hasta} />}
      />

      <FiltroPeriodo
        key={`${desde}-${hasta}`}
        desde={desde}
        hasta={hasta}
        consultando={cumplimiento.isFetching}
        onAplicar={(rango) => cambiarFiltros({ desde: rango.desde, hasta: rango.hasta })}
      />

      {cumplimiento.isPending && <Cargando texto="Calculando el indicador…" />}
      <MensajeError error={cumplimiento.error} />

      {datos && (
        <div className="cumplimiento-contenido">
          <ResumenCumplimiento datos={datos} />
          <div className="cumplimiento-grupos">
            <BarrasCumplimiento id="por-tipo" titulo="Por tipo de solicitud" grupos={datos.porTipo} />
            <BarrasCumplimiento id="por-municipio" titulo="Por municipio" grupos={datos.porMunicipio} />
          </div>
          <TablaIncumplimientos key={`${datos.desde}-${datos.hasta}`} incumplimientos={datos.incumplimientos} />
        </div>
      )}
    </>
  );
}