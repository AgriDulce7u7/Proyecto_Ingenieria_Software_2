import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { Paginacion } from "../../../componentes/Paginacion";
import { Boton } from "../../../componentes/ui/Boton";
import { Icono } from "../../../componentes/ui/Icono";
import { useFacturas, useLotes, useSincronizarFactura } from "../../../hooks/useFacturacion";
import { useFiltrosUrl } from "../../../hooks/useFiltrosUrl";
import {
  ESTADOS_COBRO_CODIGOS,
  ESTADOS_FACTURA_CODIGOS,
  formatearMoneda,
  formatearNumero,
  partesPeriodo,
  presentarEstadoCobro,
  presentarEstadoFactura,
} from "../../../presentacion/facturacion";
import { paginar } from "../../../presentacion/paginacion";
import { buscarEn } from "../../../presentacion/texto";
import { useSesion } from "../../../sesion/useSesion";
import { TablaFacturas } from "./TablaFacturas";
import "./facturas.css";

const TAMANO_PAGINA = 10;
const TODOS_LOS_PERIODOS = "todos";

/** Resultado del reintento de sincronización: aceptada por el ERP o motivo del rechazo. */
function ResultadoSincronizacion({ resultado }) {
  return (
    <div className={`aviso ${resultado.exitosa ? "aviso-exito" : "aviso-error"}`} role="status">
      <p>
        {resultado.exitosa
          ? `La factura ${resultado.numeroFactura} se sincronizó con el ERP.`
          : `El ERP no aceptó la factura ${resultado.numeroFactura} tras ${resultado.intentos} intentos: ${resultado.mensaje}`}
      </p>
    </div>
  );
}

/**
 * Facturas generadas, su estado de cobro (DE-01) y su sincronización con el ERP (RF-05, SWR-03, SWR-04).
 * Por defecto muestra el periodo del lote más reciente. Las anuladas no suman al total.
 */
export function Facturas() {
  const { filtros, pagina, hayFiltros, cambiarFiltro, cambiarPagina, limpiar } = useFiltrosUrl([
    "q",
    "periodo",
    "estado",
    "cobro",
  ]);
  const lotes = useLotes();
  const sincronizacion = useSincronizarFactura();
  const puedeOperar = useSesion().puede("FACTURACION_OPERAR");

  const periodoReciente = lotes.data?.[0]?.periodo ?? "";
  const periodoElegido = filtros.periodo || periodoReciente || TODOS_LOS_PERIODOS;
  const periodo = periodoElegido === TODOS_LOS_PERIODOS ? "" : periodoElegido;

  // Espera a conocer el lote más reciente para no pedir primero todas las facturas
  const facturas = useFacturas(
     { periodo, estado: filtros.estado, estadoCobro: filtros.cobro },
    { habilitada: Boolean(filtros.periodo) || !lotes.isPending },
  );

  const resultado = buscarEn(facturas.data ?? [], filtros.q, ["numeroFactura", "numeroContrato", "cliente"]);
  const hayAnuladas = resultado.some((factura) => factura.estadoCobro === "ANULADA");
  const total = resultado
    .filter((factura) => factura.estadoCobro !== "ANULADA")
    .reduce((suma, factura) => suma + Number(factura.total), 0);
  const { visibles, paginaActual, totalPaginas, inicio } = paginar(resultado, pagina, TAMANO_PAGINA);

  return (
    <>
      <EncabezadoGestion
        antetitulo={periodo ? partesPeriodo(periodo).texto : "Todos los periodos"}
        titulo="Facturas"
        descripcion="Consulte liquidaciones, su estado de cobro y su sincronización con el ERP."
      />

      {sincronizacion.data && <ResultadoSincronizacion resultado={sincronizacion.data} />}
      <MensajeError error={sincronizacion.error} />

      <section className="facturas-panel" aria-label="Facturas">
        <div className="facturas-filtros" role="search">
          <label className="facturas-buscador">
            <Icono nombre="buscar" tamano={18} />
            <span className="solo-lectores">Buscar</span>
            <input
              type="search"
              placeholder="Buscar factura, contrato o cliente..."
              value={filtros.q}
              onChange={(evento) => cambiarFiltro("q", evento.target.value)}
            />
          </label>
              <select
            aria-label="Filtrar por estado de cobro"
            value={filtros.cobro}
            onChange={(evento) => cambiarFiltro("cobro", evento.target.value)}
          >
            <option value="">Todo el cobro</option>
            {ESTADOS_COBRO_CODIGOS.map((codigo) => (
              <option key={codigo} value={codigo}>
                {presentarEstadoCobro(codigo).etiqueta}
              </option>
            ))}
          </select>
          <select
            aria-label="Filtrar por estado en el ERP"
            value={filtros.estado}
            onChange={(evento) => cambiarFiltro("estado", evento.target.value)}
          >
            <option value="">Todo el ERP</option>
            {ESTADOS_FACTURA_CODIGOS.map((codigo) => (
              <option key={codigo} value={codigo}>
                {presentarEstadoFactura(codigo).etiqueta}
              </option>
            ))}
          </select>
          <select
            aria-label="Filtrar por estado"
            value={filtros.estado}
            onChange={(evento) => cambiarFiltro("estado", evento.target.value)}
          >
            <option value="">Todos los estados</option>
            {ESTADOS_FACTURA_CODIGOS.map((codigo) => (
              <option key={codigo} value={codigo}>
                {presentarEstadoFactura(codigo).etiqueta}
              </option>
            ))}
          </select>
          <Boton variante="discreto" onClick={limpiar} disabled={!hayFiltros}>
            Limpiar
          </Boton>
        </div>

        <div className="facturas-resumen" aria-live="polite">
          <strong>
            {formatearNumero(resultado.length)} {resultado.length === 1 ? "factura" : "facturas"}
          </strong>
           <small>
            {filtros.q || filtros.estado || filtros.cobro ? "Total mostrado" : periodo ? "Total del periodo" : "Total"}
            {hayAnuladas && " sin anuladas"}:{" "}
            <span className="cifra">{formatearMoneda(total)}</span>
          </small>
        </div>

        {(facturas.isPending || lotes.isPending) && (
          <div className="facturas-estado">
            <Cargando texto="Cargando facturas…" />
          </div>
        )}
        {facturas.isError && (
          <div className="facturas-estado">
            <MensajeError error={facturas.error} />
          </div>
        )}
        {facturas.isSuccess && resultado.length === 0 && (
          <div className="facturas-estado">
            <p>{hayFiltros ? "Ninguna factura coincide con los filtros." : "Aún no hay facturas generadas."}</p>
          </div>
        )}

        {visibles.length > 0 && (
          <TablaFacturas
            facturas={visibles}
            onReintentar={puedeOperar ? (numero) => sincronizacion.mutate(numero) : undefined}
            reintentando={sincronizacion.isPending ? sincronizacion.variables : null}
          />
        )}

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