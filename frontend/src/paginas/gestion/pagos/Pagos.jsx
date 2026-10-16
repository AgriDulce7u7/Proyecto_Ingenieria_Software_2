import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { Paginacion } from "../../../componentes/Paginacion";
import { Boton } from "../../../componentes/ui/Boton";
import { Icono } from "../../../componentes/ui/Icono";
import { useFiltrosUrl } from "../../../hooks/useFiltrosUrl";
import { usePagos } from "../../../hooks/usePagos";
import { CANALES_CODIGOS, formatearRecaudo, presentarCanal, resumirPagos } from "../../../presentacion/pagos";
import { paginar } from "../../../presentacion/paginacion";
import { buscarEn } from "../../../presentacion/texto";
import { useSesion } from "../../../sesion/useSesion";
import { TablaPagos } from "./TablaPagos";
import "../pqr/bandeja.css";
import "./pagos.css";

const TAMANO_PAGINA = 10;

/** Franja con el recaudo y la cantidad de pagos por canal de lo que se está mostrando. */
function MetricasPagos({ pagos }) {
  const { total, cantidad, porCanal } = resumirPagos(pagos);
  const indicadores = [
    { etiqueta: "Recaudado", valor: formatearRecaudo(total), detalle: "De los pagos mostrados", clase: "pagos-metrica-moneda" },
    { etiqueta: "Pagos", valor: cantidad, detalle: "Un pago por factura" },
    { etiqueta: "En línea", valor: porCanal.EN_LINEA, detalle: "Desde el portal ciudadano" },
    {
      etiqueta: "Caja y consignación",
      valor: porCanal.PRESENCIAL + porCanal.BANCO,
      detalle: "Registrados por un funcionario",
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

/**
 * Pagos recibidos (CU-04, RF-06, SWR-24) con filtros del backend (canal y rango de fechas) y búsqueda
 * local por pago, factura, contrato o usuario. Los filtros quedan en la URL.
 */
export function Pagos() {
  const { filtros, pagina, hayFiltros, cambiarFiltro, cambiarPagina, limpiar } = useFiltrosUrl([
    "q",
    "canal",
    "desde",
    "hasta",
  ]);
  const { puede } = useSesion();
  const puedeRegistrar = puede("FACTURACION_OPERAR");
  const puedePagarCuota = puede("CARTERA_PAGAR_CUOTA");
  const rangoInvalido = Boolean(filtros.desde && filtros.hasta && filtros.desde > filtros.hasta);
  const pagos = usePagos(
    { canal: filtros.canal, desde: filtros.desde, hasta: filtros.hasta },
    { habilitada: !rangoInvalido },
  );

  const resultado = buscarEn(pagos.data ?? [], filtros.q, ["codigoPago", "numeroFactura", "numeroContrato", "usuario"]);
  const { visibles, paginaActual, totalPaginas, inicio } = paginar(resultado, pagina, TAMANO_PAGINA);
  const alCambiar = (nombre) => (evento) => cambiarFiltro(nombre, evento.target.value);

  return (
    <>
      <EncabezadoGestion
        antetitulo="Recaudo"
        titulo="Pagos"
        descripcion="Pagos en caja, por consignación y en línea, con su comprobante. Cada factura se paga completa una sola vez."
        accion={
          (puedeRegistrar || puedePagarCuota) && (
            <div className="pagos-acciones">
              {puedePagarCuota && (
                <Boton a="/gestion/pagos/cuota" variante="secundario" icono="acuerdo">
                  Pago de cuota
                </Boton>
              )}
              {puedeRegistrar && (
                <Boton a="/gestion/pagos/nuevo" icono="mas">
                  Registrar pago
                </Boton>
              )}
            </div>
          )
        }
      />

      {pagos.data && <MetricasPagos pagos={resultado} />}

      <section className="bandeja-panel" aria-label="Pagos">
        <div className="bandeja-filtros pagos-filtros" role="search">
          <label className="bandeja-buscador">
            <Icono nombre="buscar" tamano={18} />
            <span className="solo-lectores">Buscar</span>
            <input
              type="search"
              placeholder="Buscar pago, factura, contrato o usuario..."
              value={filtros.q}
              onChange={alCambiar("q")}
            />
          </label>
          <select aria-label="Filtrar por canal" value={filtros.canal} onChange={alCambiar("canal")}>
            <option value="">Todos los canales</option>
            {CANALES_CODIGOS.map((codigo) => (
              <option key={codigo} value={codigo}>
                {presentarCanal(codigo).etiqueta}
              </option>
            ))}
          </select>
          <label className="pagos-fecha">
            <span>Desde</span>
            <input
              type="date"
              value={filtros.desde}
              max={filtros.hasta || undefined}
              aria-invalid={rangoInvalido || undefined}
              onChange={alCambiar("desde")}
            />
          </label>
          <label className="pagos-fecha">
            <span>Hasta</span>
            <input
              type="date"
              value={filtros.hasta}
              min={filtros.desde || undefined}
              aria-invalid={rangoInvalido || undefined}
              onChange={alCambiar("hasta")}
            />
          </label>
          <Boton variante="discreto" onClick={limpiar} disabled={!hayFiltros}>
            Limpiar
          </Boton>
        </div>

        <div className="bandeja-resumen" aria-live="polite">
          <strong>
            {resultado.length} {resultado.length === 1 ? "pago" : "pagos"}
          </strong>
          <small>{pagos.isFetching ? "Actualizando…" : "Del más reciente al más antiguo"}</small>
        </div>

        {rangoInvalido && (
          <div className="bandeja-estado">
            <p className="campo-error" role="alert">
              La fecha inicial no puede ser posterior a la final.
            </p>
          </div>
        )}
        {pagos.isPending && !rangoInvalido && (
          <div className="bandeja-estado">
            <Cargando texto="Cargando pagos…" />
          </div>
        )}
        {pagos.isError && (
          <div className="bandeja-estado">
            <MensajeError error={pagos.error} />
          </div>
        )}
        {pagos.isSuccess && resultado.length === 0 && (
          <div className="bandeja-estado bandeja-vacia">
            <p>{hayFiltros ? "Ningún pago coincide con los filtros." : "Aún no hay pagos registrados."}</p>
            {hayFiltros && (
              <Boton variante="secundario" onClick={limpiar}>
                Limpiar filtros
              </Boton>
            )}
          </div>
        )}

        {visibles.length > 0 && <TablaPagos pagos={visibles} />}

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