package co.edu.uniquindio.epq.facturacion.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import co.edu.uniquindio.epq.facturacion.ErpProperties;

@DisplayName("ErpFinancieroSimuladoAdapter - ERP simulado del prototipo")
class ErpFinancieroSimuladoAdapterTest {

    private static final FacturaErp FACTURA = new FacturaErp("FAC-202608-000001", "2026-08", "CT-ACU-0001",
            "1094950001", "Acueducto", new BigDecimal("21.50"), new BigDecimal("81783.25"),
            new BigDecimal("81783.25"), LocalDate.of(2026, 9, 16));

    private static ErpFinancieroSimuladoAdapter conTasaDeFallo(double tasa) {
        return new ErpFinancieroSimuladoAdapter(new ErpProperties("simulado", "http://localhost:9090/api/erp", 3, 0,
                new ErpProperties.Simulado(tasa)));
    }

    @RepeatedTest(20)
    @DisplayName("Tasa de fallo 0: siempre acepta y devuelve una referencia ERP-XXXXXXXX")
    void siempreAcepta() {
        RespuestaErp respuesta = conTasaDeFallo(0.0).enviarFactura(FACTURA);

        assertTrue(respuesta.exitosa());
        assertTrue(respuesta.referencia().matches("ERP-[0-9A-F]{8}"), respuesta.referencia());
        assertEquals("Factura registrada en el ERP simulado", respuesta.mensaje());
    }

    @RepeatedTest(20)
    @DisplayName("Tasa de fallo 1: siempre rechaza, sin referencia")
    void siempreRechaza() {
        RespuestaErp respuesta = conTasaDeFallo(1.0).enviarFactura(FACTURA);

        assertFalse(respuesta.exitosa());
        assertNull(respuesta.referencia());
    }

    @ParameterizedTest(name = "Tasa {0} → se limita a 1.0 y siempre rechaza")
    @ValueSource(doubles = {1.5, 100.0})
    @DisplayName("Tasas mayores a 1 se limitan a 1")
    void tasaMayorAUno(double tasa) {
        assertFalse(conTasaDeFallo(tasa).enviarFactura(FACTURA).exitosa());
    }

    @ParameterizedTest(name = "Tasa {0} → se limita a 0.0 y siempre acepta")
    @ValueSource(doubles = {-0.5, -10.0})
    @DisplayName("Tasas negativas se limitan a 0")
    void tasaNegativa(double tasa) {
        assertTrue(conTasaDeFallo(tasa).enviarFactura(FACTURA).exitosa());
    }
}