package co.edu.uniquindio.epq.facturacion.servicio.numeracion;

import java.time.YearMonth;

import co.edu.uniquindio.epq.facturacion.dominio.Contrato;

/**
 * Abstracción de la política de numeración de facturas (DIP / OCP).
 */
public interface GeneradorNumeroFactura {

    String generar(YearMonth periodo, Contrato contrato);
}
