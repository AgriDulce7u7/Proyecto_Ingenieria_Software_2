package co.edu.uniquindio.epq.comun.exportacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FormatoExportacionTest {

    @Test
    void reconoceLosFormatosSinDistinguirMayusculas() {
        assertEquals(FormatoExportacion.PDF, FormatoExportacion.desde("pdf"));
        assertEquals(FormatoExportacion.EXCEL, FormatoExportacion.desde("Excel"));
        assertEquals(FormatoExportacion.EXCEL, FormatoExportacion.desde(" xlsx "));
        assertEquals(FormatoExportacion.PDF, FormatoExportacion.desde(null)); // por defecto
        assertEquals("xlsx", FormatoExportacion.EXCEL.extension());
    }

    @Test
    void rechazaFormatosNoSoportados() {
        assertThrows(IllegalArgumentException.class, () -> FormatoExportacion.desde("csv"));
    }
}
