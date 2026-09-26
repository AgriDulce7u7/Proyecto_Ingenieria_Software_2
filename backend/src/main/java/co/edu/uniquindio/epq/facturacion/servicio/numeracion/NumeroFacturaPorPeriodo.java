package co.edu.uniquindio.epq.facturacion.servicio.numeracion;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.facturacion.dominio.Contrato;

/**
 * Numeración determinista {@code FAC-yyyyMM-NNNNNN} (NNNNNN = id del contrato).
 *
 * <p>Al depender solo del periodo y del contrato es única (un contrato tiene una factura por
 * periodo) y no requiere secuencias ni bloqueos, lo que permite procesar el lote en paralelo.</p>
 */
@Component
public class NumeroFacturaPorPeriodo implements GeneradorNumeroFactura {

    private static final DateTimeFormatter FORMATO_PERIODO = DateTimeFormatter.ofPattern("yyyyMM");

    @Override
    public String generar(YearMonth periodo, Contrato contrato) {
        return "FAC-%s-%06d".formatted(periodo.format(FORMATO_PERIODO), contrato.getId());
    }
}
