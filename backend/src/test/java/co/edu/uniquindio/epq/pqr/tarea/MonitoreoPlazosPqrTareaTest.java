package co.edu.uniquindio.epq.pqr.tarea;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.epq.pqr.servicio.MonitoreoPlazosPqrService;
import co.edu.uniquindio.epq.pqr.web.dto.ResultadoMonitoreoResponse;

@DisplayName("MonitoreoPlazosPqrTarea - revisión periódica de plazos (SWR-07, RN-04)")
class MonitoreoPlazosPqrTareaTest {

    private final MonitoreoPlazosPqrService monitoreo = mock(MonitoreoPlazosPqrService.class);
    private final MonitoreoPlazosPqrTarea tarea = new MonitoreoPlazosPqrTarea(monitoreo);

    @Test
    @DisplayName("Cada ejecución programada lanza el monitoreo de plazos")
    void ejecutaElMonitoreo() {
        when(monitoreo.ejecutarMonitoreo())
                .thenReturn(new ResultadoMonitoreoResponse(LocalDateTime.of(2026, 9, 15, 10, 0), 2, 1));

        tarea.programar();

        verify(monitoreo).ejecutarMonitoreo();
    }

    @Test
    @DisplayName("Si el monitoreo falla, el error no llega al scheduler y la siguiente ejecución sigue programada")
    void falloAislado() {
        when(monitoreo.ejecutarMonitoreo()).thenThrow(new IllegalStateException("BD caída"));

        assertDoesNotThrow(tarea::programar);
    }
}