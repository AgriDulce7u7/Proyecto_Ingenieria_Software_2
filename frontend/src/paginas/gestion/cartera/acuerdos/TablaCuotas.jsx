import { Etiqueta } from "../../../../componentes/ui/Etiqueta";
import { formatearMoneda } from "../../../../presentacion/facturacion";
import { formatearFechaCorta, formatearFechaHora } from "../../../../presentacion/fechas";
import { presentarEstadoCuota } from "../../../../presentacion/cartera";
import { presentarCanal } from "../../../../presentacion/pagos";

/**
 * Plan de cuotas: número, vencimiento, valor y estado. Sirve para la simulación (sin estado ni pago)
 * y para el detalle del acuerdo, donde la siguiente cuota por pagar queda resaltada.
 */
export function TablaCuotas({ cuotas, siguiente = null, simulacion = false }) {
  return (
    <div className="cuotas-contenedor">
      <table className="cuotas-tabla">
        <thead>
          <tr>
            <th scope="col">Cuota</th>
            <th scope="col">Vence</th>
            <th scope="col">Valor</th>
            {!simulacion && <th scope="col">Estado</th>}
            {!simulacion && <th scope="col">Pago</th>}
          </tr>
        </thead>
        <tbody>
          {cuotas.map((cuota) => {
            const estado = presentarEstadoCuota(cuota.estado);
            return (
              <tr key={cuota.numeroCuota} data-siguiente={siguiente === cuota.numeroCuota || undefined}>
                <th scope="row" className="cifra">
                  {cuota.numeroCuota}
                </th>
                <td>{formatearFechaCorta(cuota.fechaVencimiento)}</td>
                <td className="cifra">{formatearMoneda(cuota.valor)}</td>
                {!simulacion && (
                  <td>
                    <Etiqueta tono={estado.tono}>{estado.etiqueta}</Etiqueta>
                  </td>
                )}
                {!simulacion && (
                  <td className="cuotas-pago">
                    {cuota.fechaPago ? (
                      <>
                        {formatearFechaHora(cuota.fechaPago)}
                        <small>
                          {presentarCanal(cuota.canalPago).etiqueta}
                          {cuota.referenciaPago && ` · ${cuota.referenciaPago}`}
                        </small>
                      </>
                    ) : (
                      "—"
                    )}
                  </td>
                )}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}