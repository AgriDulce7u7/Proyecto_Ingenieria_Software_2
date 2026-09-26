package co.edu.uniquindio.epq.facturacion.tarea;

import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.FECHA_GENERACION;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.epq.comun.calendario.CalendarioLaboral;
import co.edu.uniquindio.epq.facturacion.FacturacionProperties;
import co.edu.uniquindio.epq.facturacion.dominio.LoteFacturacion;
import co.edu.uniquindio.epq.facturacion.repositorio.LoteFacturacionRepository;
import co.edu.uniquindio.epq.facturacion.servicio.GeneracionFacturacionService;
import co.edu.uniquindio.epq.facturacion.web.dto.ResultadoGeneracionLoteResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.ResultadoSincronizacionResponse;

/**
 * Octubre de 2026: el primer día hábil es el jueves 1. Se factura el periodo de septiembre.
 */
@DisplayName("FacturacionMensualTarea - facturación automática del primer día hábil (SWR-01)")
class FacturacionMensualTareaTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final YearMonth OCTUBRE = YearMonth.of(2026, 10);
    private static final YearMonth SEPTIEMBRE = YearMonth.of(2026, 9);
    private static final LocalDate PRIMER_HABIL = LocalDate.of(2026, 10, 1);

    private final GeneracionFacturacionService generacion = mock(GeneracionFacturacionService.class);
    private final LoteFacturacionRepository loteRepository = mock(LoteFacturacionRepository.class);
    private final CalendarioLaboral calendario = mock(CalendarioLaboral.class);

    @BeforeEach
    void configurar() {
        when(calendario.primerDiaHabil(OCTUBRE)).thenReturn(PRIMER_HABIL);
        when(generacion.periodoPorDefecto()).thenReturn(SEPTIEMBRE);
        when(generacion.generarLote(any(), anyString())).thenReturn(new ResultadoGeneracionLoteResponse(
                "2026-09", GeneracionFacturacionService.ORIGEN_AUTOMATICO, "COMPLETADO", FECHA_GENERACION,
                FECHA_GENERACION, 1500, true, 7, 2, 6, 0, 6, List.of(),
                new ResultadoSincronizacionResponse("2026-09", 6, 6, 0, List.of())));
    }

    private FacturacionMensualTarea tarea(LocalDate hoy, boolean automatica) {
        Clock clock = Clock.fixed(hoy.atTime(1, 0).atZone(ZONA).toInstant(), ZONA);
        return new FacturacionMensualTarea(generacion, loteRepository, calendario,
                new FacturacionProperties(automatica, "0 0 1 * * *", 15, 60), clock);
    }

    private void verificarQueFacturo() {
        verify(generacion).generarLote(null, GeneracionFacturacionService.ORIGEN_AUTOMATICO);
    }

    private void verificarQueNoFacturo() {
        verify(generacion, never()).generarLote(any(), anyString());
    }

    @Test
    @DisplayName("Primer día hábil sin lote del periodo: factura el mes anterior con origen AUTOMATICO")
    void facturaElPrimerDiaHabil() {
        tarea(PRIMER_HABIL, true).programar();

        verificarQueFacturo();
    }

    @Test
    @DisplayName("Ejecución automática deshabilitada: no factura ni consulta el calendario")
    void deshabilitada() {
        tarea(PRIMER_HABIL, false).programar();

        verificarQueNoFacturo();
        verify(calendario, never()).primerDiaHabil(any());
    }

    @Test
    @DisplayName("Antes del primer día hábil: no factura")
    void antesDelPrimerDiaHabil() {
        when(calendario.primerDiaHabil(YearMonth.of(2026, 11))).thenReturn(LocalDate.of(2026, 11, 3));

        tarea(LocalDate.of(2026, 11, 2), true).programar();

        verificarQueNoFacturo();
    }

    @Test
    @DisplayName("Lote del periodo ya completado: no vuelve a facturar")
    void loteCompletado() {
        LoteFacturacion completado = LoteFacturacion.abrir(SEPTIEMBRE, FECHA_GENERACION);
        completado.finalizar(6, false, FECHA_GENERACION.plusMinutes(1));
        when(loteRepository.findByPeriodo(SEPTIEMBRE)).thenReturn(Optional.of(completado));

        tarea(LocalDate.of(2026, 10, 5), true).programar();

        verificarQueNoFacturo();
    }

    @Test
    @DisplayName("Lote con error días después del primer hábil: reintenta (recupera ejecuciones fallidas)")
    void recuperaLoteConError() {
        LoteFacturacion conError = LoteFacturacion.abrir(SEPTIEMBRE, FECHA_GENERACION);
        conError.marcarError(FECHA_GENERACION.plusMinutes(1));
        when(loteRepository.findByPeriodo(SEPTIEMBRE)).thenReturn(Optional.of(conError));

        tarea(LocalDate.of(2026, 10, 5), true).programar();

        verificarQueFacturo();
    }

    @Test
    @DisplayName("Si la generación falla, el error no llega al scheduler")
    void falloAislado() {
        when(generacion.generarLote(any(), anyString())).thenThrow(new IllegalStateException("BD caída"));

        assertDoesNotThrow(() -> tarea(PRIMER_HABIL, true).programar());
    }
}