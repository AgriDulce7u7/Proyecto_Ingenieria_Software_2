package co.edu.uniquindio.epq.facturacion.servicio.calculo;

import co.edu.uniquindio.epq.facturacion.dominio.Contrato;
import co.edu.uniquindio.epq.facturacion.dominio.DetalleLiquidacion;
import co.edu.uniquindio.epq.facturacion.dominio.LecturaMedidor;
import co.edu.uniquindio.epq.facturacion.dominio.Tarifa;

/**
 * Patrón <b>Strategy</b>: algoritmo de liquidación de una factura.
 *
 * <p>Permite incorporar nuevas fórmulas tarifarias (por ejemplo por estrato o por rangos de
 * consumo, RN-08) agregando una nueva implementación sin modificar el proceso de facturación (OCP).</p>
 */
public interface EstrategiaCalculoFactura {

    /** Indica si la estrategia aplica para el contrato (por ejemplo, según su servicio). */
    boolean aplicaA(Contrato contrato);

    /** Menor valor = mayor prioridad cuando varias estrategias aplican. */
    default int prioridad() {
        return 100;
    }

    DetalleLiquidacion calcular(LecturaMedidor lectura, Tarifa tarifa);
}
