import { useSearchParams } from "react-router-dom";
import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { Boton } from "../../../componentes/ui/Boton";
import { Icono } from "../../../componentes/ui/Icono";
import { useLotes } from "../../../hooks/useFacturacion";
import { partesPeriodo, presentarEstadoLote } from "../../../presentacion/facturacion";
import { TablaLotes } from "./TablaLotes";
import "./lotes.css";

const ESTADOS = ["COMPLETADO", "EN_PROCESO", "ERROR"];

/** Aviso del lote más reciente que tiene facturas sin sincronizar con el ERP (SWR-04). */
function AvisoErp({ lotes }) {
  const afectado = lotes.find((lote) => lote.facturasConErrorSincronizacion > 0);
  if (!afectado) return null;

  const cantidad = afectado.facturasConErrorSincronizacion;
  return (
    <div className="lotes-aviso" role="status">
      <div>
        <span className="lotes-aviso-icono" aria-hidden="true">
          <Icono nombre="alerta" />
        </span>
        <div>
          <h2>
            {cantidad === 1 ? "1 factura" : `${cantidad} facturas`} sin sincronizar en{" "}
            {partesPeriodo(afectado.periodo).texto}
          </h2>
          <p>El ERP rechazó el envío o no respondió. Puede reintentar la sincronización desde el lote.</p>
        </div>
      </div>
      <Boton a={`/gestion/facturacion/lotes/${afectado.periodo}`} variante="secundario">
        Revisar lote
      </Boton>
    </div>
  );
}

/** Historial de lotes de facturación (RF-05, CU-03, SWR-02, SWR-04). */
export function Lotes() {
  const lotes = useLotes();
  const [parametros, setParametros] = useSearchParams();
  const anio = parametros.get("anio") ?? "";
  const estado = parametros.get("estado") ?? "";

  const cambiarFiltro = (nombre) => (evento) =>
    setParametros((anteriores) => {
      const nuevos = new URLSearchParams(anteriores);
      if (evento.target.value) nuevos.set(nombre, evento.target.value);
      else nuevos.delete(nombre);
      return nuevos;
    });

  const todos = lotes.data ?? [];
  const anios = [...new Set(todos.map((lote) => lote.periodo.slice(0, 4)))];
  const visibles = todos.filter(
    (lote) => (!anio || lote.periodo.startsWith(anio)) && (!estado || lote.estado === estado),
  );

  return (
    <>
      <EncabezadoGestion
        antetitulo="Histórico"
        titulo="Lotes de facturación"
        descripcion="Revise ejecuciones, resultados e incidencias por periodo."
      />

      <div className="lotes-filtros">
        <select aria-label="Filtrar por año" value={anio} onChange={cambiarFiltro("anio")}>
          <option value="">Todos los años</option>
          {anios.map((valor) => (
            <option key={valor} value={valor}>
              {valor}
            </option>
          ))}
        </select>
        <select aria-label="Filtrar por estado" value={estado} onChange={cambiarFiltro("estado")}>
          <option value="">Todos los estados</option>
          {ESTADOS.map((valor) => (
            <option key={valor} value={valor}>
              {presentarEstadoLote(valor).etiqueta}
            </option>
          ))}
        </select>
      </div>

      <section className="lotes-panel" aria-label="Lotes">
        {lotes.isPending && (
          <div className="lotes-estado">
            <Cargando texto="Cargando lotes…" />
          </div>
        )}
        {lotes.isError && (
          <div className="lotes-estado">
            <MensajeError error={lotes.error} />
          </div>
        )}
        {lotes.isSuccess && visibles.length === 0 && (
          <div className="lotes-estado lotes-vacio">
            {todos.length === 0 ? (
              <>
                <p>Aún no se ha generado ningún lote.</p>
                <Boton a="/gestion/facturacion" variante="secundario">
                  Ir a facturación
                </Boton>
              </>
            ) : (
              <p>Ningún lote coincide con los filtros.</p>
            )}
          </div>
        )}
        {visibles.length > 0 && <TablaLotes lotes={visibles} />}
      </section>

      <AvisoErp lotes={todos} />
    </>
  );
}