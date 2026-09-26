package co.edu.uniquindio.epq.pqr.servicio.radicado;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;

class GeneradorRadicadoMensualTest {

    private final PqrRepository repositorio = mock(PqrRepository.class);
    private final GeneradorRadicadoMensual generador = new GeneradorRadicadoMensual(repositorio);
    private final LocalDateTime fecha = LocalDateTime.of(2026, 9, 26, 8, 0);

    @Test
    void primerRadicadoDelMes() {
        when(repositorio.findTopByRadicadoStartingWithOrderByRadicadoDesc("202609-")).thenReturn(Optional.empty());
        assertEquals("202609-0001", generador.generar(fecha));
    }

    @Test
    void continuaElConsecutivoDelMes() {
        Pqr ultima = mock(Pqr.class);
        when(ultima.getRadicado()).thenReturn("202609-0041");
        when(repositorio.findTopByRadicadoStartingWithOrderByRadicadoDesc("202609-")).thenReturn(Optional.of(ultima));
        assertEquals("202609-0042", generador.generar(fecha));
    }
}
