import { Cargando } from "../../../componentes/Cargando";
import { MensajeError } from "../../../componentes/MensajeError";
import { useFiltrosUrl } from "../../../hooks/useFiltrosUrl";
import { useContratosPortal, useFacturasPortal } from "../../../hooks/usePortal";
import { useTituloPagina } from "../../../hooks/useTituloPagina";
import { estaPorPagar } from "../../../presentacion/portal";
import { ListaFacturas } from "./ListaFacturas";

const FILTROS_COBRO = [
  { codigo: "", texto: "Todas" },
  { codigo: "pendientes", texto: "Por pagar" },
  { codigo: "pagadas", texto: "Pagadas" },
];

const filtrarPorCobro = (facturas, cobro) => {
  if (cobro === "pendientes") return facturas.filter(estaPorPagar);
  if (cobro === "pagadas") return facturas.filter((factura) => factura.estadoCobro === "PAGADA");
  return facturas;
};

/** Facturas del ciudadano (SWR-25, SWR-27), filtrables por contrato y por estado de cobro. Los filtros van en la URL. */
export function FacturasCiudadano() {
  useTituloPagina("Mis facturas");
  const { filtros, cambiarFiltro } = useFiltrosUrl(["contrato", "cobro"]);
  const contratos = useContratosPortal();
  const facturas = useFacturasPortal(filtros.contrato || undefined);
  const visibles = filtrarPorCobro(facturas.data ?? [], filtros.cobro);

  return (
    <section className="cuenta-seccion" aria-labelledby="facturas-titulo">
      <div className="cuenta-seccion-titulo">
        <h2 id="facturas-titulo">Mis facturas</h2>
        {(contratos.data?.length ?? 0) > 1 && (
          <select
            aria-label="Filtrar por contrato"
            className="cuenta-selector"
            value={filtros.contrato}
            onChange={(evento) => cambiarFiltro("contrato", evento.target.value)}
          >
            <option value="">Todos los servicios</option>
            {contratos.data.map((contrato) => (
              <option key={contrato.numeroContrato} value={contrato.numeroContrato}>
                {contrato.servicio} · {contrato.numeroContrato}
              </option>
            ))}
          </select>
        )}
      </div>

      <div className="cuenta-filtros" role="group" aria-label="Estado de cobro">
        {FILTROS_COBRO.map((filtro) => (
          <button
            key={filtro.codigo || "todas"}
            type="button"
            className="cuenta-filtro"
            aria-pressed={filtros.cobro === filtro.codigo}
            onClick={() => cambiarFiltro("cobro", filtro.codigo)}
          >
            {filtro.texto}
          </button>
        ))}
      </div>

      {facturas.isPending && <Cargando texto="Cargando sus facturas…" />}
      <MensajeError error={facturas.error} />
      {facturas.isSuccess && visibles.length === 0 && (
        <p className="cuenta-vacio">
          {filtros.cobro === "pendientes" ? "No tiene facturas por pagar." : "No hay facturas para mostrar."}
        </p>
      )}
      {visibles.length > 0 && <ListaFacturas facturas={visibles} mostrarServicio={!filtros.contrato} />}
    </section>
  );
}