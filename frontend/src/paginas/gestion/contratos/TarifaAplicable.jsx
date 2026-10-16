import { Icono } from "../../../componentes/ui/Icono";
import { useTarifaAplicable } from "../../../hooks/useContratos";
import { formatearMoneda } from "../../../presentacion/facturacion";

/**
 * Tarifa que el backend asignará al contrato (SWR-18, RN-08) según el servicio y el municipio y estrato
 * del predio. Si no hay tarifa vigente, el contrato no se puede registrar y se avisa antes de enviar.
 */
export function TarifaAplicable({ servicio, predio }) {
  const tarifa = useTarifaAplicable({ servicio, municipioId: predio?.municipioId, estrato: predio?.estrato });

  if (!servicio || !predio) {
    return (
      <div className="contrato-tarifa contrato-tarifa-vacia formulario-campo-completo">
        <Icono nombre="recibo" />
        <p>Elija el predio y el servicio para ver la tarifa que se aplicará.</p>
      </div>
    );
  }
  if (tarifa.isPending) {
    return (
      <div className="contrato-tarifa contrato-tarifa-vacia formulario-campo-completo">
        <p>Consultando la tarifa…</p>
      </div>
    );
  }
  if (tarifa.isError) {
    return (
      <div className="aviso aviso-error formulario-campo-completo" role="alert">
        <p>
          <strong>Sin tarifa vigente.</strong> {tarifa.error.detalle}
        </p>
      </div>
    );
  }

  const { data } = tarifa;
  return (
    <div className="contrato-tarifa formulario-campo-completo" aria-live="polite">
      <Icono nombre="recibo" />
      <div>
        <span className="antetitulo">Tarifa que se aplicará</span>
        <strong>{data.nombre}</strong>
        {data.criterio && <small>{data.criterio}</small>}
      </div>
      <dl>
        <div>
          <dt>Valor por m³</dt>
          <dd className="cifra">{formatearMoneda(data.valorPorUnidad)}</dd>
        </div>
        <div>
          <dt>Cargo fijo</dt>
          <dd className="cifra">{formatearMoneda(data.cargoFijo)}</dd>
        </div>
      </dl>
    </div>
  );
}