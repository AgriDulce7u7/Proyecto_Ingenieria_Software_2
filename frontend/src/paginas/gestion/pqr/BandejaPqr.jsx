import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { Boton } from "../../../componentes/ui/Boton";
import { useBandejaPqr } from "../../../hooks/usePqr";
import { TAMANO_PAGINA, aFiltrosApi, buscar } from "./bandeja";
import { FiltrosBandeja } from "./FiltrosBandeja";
import { MetricasBandeja } from "./MetricasBandeja";
import { TablaBandeja } from "./TablaBandeja";
import { useFiltrosBandeja } from "./useFiltrosBandeja";
import "./bandeja.css";

/**
 * Bandeja de PQR del back-office (RF-09, CU-07): indicadores, filtros y listado ordenado
 * por fecha límite (el más urgente primero, como lo entrega el backend).
 */
export function BandejaPqr() {
  const { filtros, pagina, hayFiltros, cambiarFiltro, cambiarPagina, limpiar } = useFiltrosBandeja();
  const todas = useBandejaPqr();
  const filtradas = useBandejaPqr(aFiltrosApi(filtros));

  const resultado = buscar(filtradas.data ?? [], filtros.q);
  const totalPaginas = Math.max(1, Math.ceil(resultado.length / TAMANO_PAGINA));
  const paginaActual = Math.min(pagina, totalPaginas);
  const inicio = (paginaActual - 1) * TAMANO_PAGINA;
  const visibles = resultado.slice(inicio, inicio + TAMANO_PAGINA);

  return (
    <>
      <EncabezadoGestion
        antetitulo="Operación diaria"
        titulo="Bandeja de PQR"
        descripcion="Gestione solicitudes, responsables y fechas límite."
      />

      {todas.data && <MetricasBandeja pqrs={todas.data} />}

      <section className="bandeja-panel" aria-label="Solicitudes">
        <FiltrosBandeja filtros={filtros} hayFiltros={hayFiltros} cambiarFiltro={cambiarFiltro} limpiar={limpiar} />

        <div className="bandeja-resumen" aria-live="polite">
          <strong>
            {resultado.length} {resultado.length === 1 ? "solicitud" : "solicitudes"}
          </strong>
          <small>{filtradas.isFetching ? "Actualizando…" : "Ordenadas por fecha límite"}</small>
        </div>

        {filtradas.isPending && (
          <div className="bandeja-estado">
            <Cargando texto="Cargando solicitudes…" />
          </div>
        )}
        {filtradas.isError && (
          <div className="bandeja-estado">
            <MensajeError error={filtradas.error} />
          </div>
        )}

        {filtradas.isSuccess && resultado.length === 0 && (
          <div className="bandeja-estado bandeja-vacia">
            <p>{hayFiltros ? "Ninguna solicitud coincide con los filtros." : "No hay solicitudes registradas."}</p>
            {hayFiltros && (
              <Boton variante="secundario" onClick={limpiar}>
                Limpiar filtros
              </Boton>
            )}
          </div>
        )}

        {visibles.length > 0 && <TablaBandeja pqrs={visibles} />}

        {resultado.length > TAMANO_PAGINA && (
          <nav className="bandeja-paginacion" aria-label="Paginación">
            <span>
              Mostrando {inicio + 1}–{inicio + visibles.length} de {resultado.length}
            </span>
            <div>
              <Boton variante="discreto" disabled={paginaActual === 1} onClick={() => cambiarPagina(paginaActual - 1)}>
                Anterior
              </Boton>
              <Boton
                variante="secundario"
                disabled={paginaActual === totalPaginas}
                onClick={() => cambiarPagina(paginaActual + 1)}
              >
                Siguiente
              </Boton>
            </div>
          </nav>
        )}
      </section>
    </>
  );
}