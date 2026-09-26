package co.edu.uniquindio.epq.pqr.servicio.impl;

import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.gestor;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.pqrEn;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.context.ApplicationEventPublisher;

import co.edu.uniquindio.epq.pqr.DatosPruebaPqr;
import co.edu.uniquindio.epq.pqr.PqrProperties;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.evento.PqrEstadoCambiadoEvento;
import co.edu.uniquindio.epq.pqr.notificacion.ServicioNotificacionPqr;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;
import co.edu.uniquindio.epq.pqr.servicio.ResolutorCatalogosPqr;
import co.edu.uniquindio.epq.pqr.web.dto.ResultadoMonitoreoResponse;

@DisplayName("MonitoreoPlazosPqrServiceImpl - vencimientos y alertas")
class MonitoreoPlazosPqrServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-15T15:00:00Z"), ZoneId.of("America/Bogota"));
    private static final LocalDateTime AHORA = LocalDateTime.now(CLOCK);
    private static final List<String> ESTADOS_CON_PLAZO = EstadoPqrTipo.nombresConPlazoEnCurso();

    private final PqrRepository pqrRepository = mock(PqrRepository.class);
    private final ResolutorCatalogosPqr catalogos = mock(ResolutorCatalogosPqr.class);
    private final ServicioNotificacionPqr notificaciones = mock(ServicioNotificacionPqr.class);
    private final ApplicationEventPublisher eventos = mock(ApplicationEventPublisher.class);

    private final Gestor laura = gestor(1, "Laura Giraldo", true);

    private MonitoreoPlazosPqrServiceImpl servicio;

    @BeforeEach
    void configurar() {
        servicio = crearServicio(48);
        when(catalogos.estado(any())).thenAnswer(inv -> DatosPruebaPqr.estado(inv.getArgument(0)));
        when(pqrRepository.buscarConPlazoVencido(any(), any())).thenReturn(List.of());
        when(pqrRepository.buscarProximasAVencer(any(), any(), any())).thenReturn(List.of());
    }

    private MonitoreoPlazosPqrServiceImpl crearServicio(int horasAlerta) {
        return new MonitoreoPlazosPqrServiceImpl(pqrRepository, catalogos, notificaciones, eventos,
                new PqrProperties(horasAlerta, "0 */15 * * * *"), CLOCK);
    }

    @Test
    @DisplayName("Sin PQR vencidas ni próximas a vencer: devuelve ceros y no modifica nada")
    void sinPendientes() {
        ResultadoMonitoreoResponse resultado = servicio.ejecutarMonitoreo();

        assertAll(
                () -> assertEquals(AHORA, resultado.fechaEjecucion()),
                () -> assertEquals(0, resultado.pqrMarcadasVencidas()),
                () -> assertEquals(0, resultado.alertasEnviadas()));
        verify(catalogos, never()).estado(any());
        verify(eventos, never()).publishEvent(any(Object.class));
        verify(notificaciones, never()).notificarAlertaVencimiento(any(), any());
    }

    @Test
    @DisplayName("Revisa primero los vencimientos y después las alertas")
    void ordenDeRevision() {
        servicio.ejecutarMonitoreo();

        InOrder orden = inOrder(pqrRepository);
        orden.verify(pqrRepository).buscarConPlazoVencido(any(), any());
        orden.verify(pqrRepository).buscarProximasAVencer(any(), any(), any());
    }

    // ------------------------------------------------------------------ vencimientos

    @Nested
    @DisplayName("vencimientos (RN-04)")
    class Vencimientos {

        @Test
        @DisplayName("Busca PQR con plazo en curso cuya fecha límite ya pasó")
        void consultaVencidas() {
            servicio.ejecutarMonitoreo();

            verify(pqrRepository).buscarConPlazoVencido(ESTADOS_CON_PLAZO, AHORA);
        }

        @Test
        @DisplayName("Marca como vencidas las PQR radicadas y en trámite, y publica un evento por cada una")
        void marcaVencidas() {
            Pqr radicada = pqrEn(EstadoPqrTipo.RADICADO, laura);
            Pqr enTramite = pqrEn(EstadoPqrTipo.EN_TRAMITE, laura);
            when(pqrRepository.buscarConPlazoVencido(any(), any())).thenReturn(List.of(radicada, enTramite));

            ResultadoMonitoreoResponse resultado = servicio.ejecutarMonitoreo();

            assertEquals(2, resultado.pqrMarcadasVencidas());
            assertEquals(EstadoPqrTipo.VENCIDO, radicada.getEstadoTipo());
            assertEquals(EstadoPqrTipo.VENCIDO, enTramite.getEstadoTipo());
            verify(catalogos, times(1)).estado(EstadoPqrTipo.VENCIDO);

            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            verify(eventos, times(2)).publishEvent(captor.capture());
            PqrEstadoCambiadoEvento primero = assertInstanceOf(PqrEstadoCambiadoEvento.class, captor.getAllValues().get(0));
            PqrEstadoCambiadoEvento segundo = assertInstanceOf(PqrEstadoCambiadoEvento.class, captor.getAllValues().get(1));
            assertAll(
                    () -> assertEquals(EstadoPqrTipo.RADICADO, primero.estadoAnterior()),
                    () -> assertEquals(EstadoPqrTipo.EN_TRAMITE, segundo.estadoAnterior()),
                    () -> assertEquals(EstadoPqrTipo.VENCIDO, primero.estadoNuevo()),
                    () -> assertEquals(RegistroPqrServiceImpl.USUARIO_SISTEMA, primero.usuario()),
                    () -> assertTrue(primero.comentario().contains("RN-04")));
        }
    }

    // ------------------------------------------------------------------ alertas

    @Nested
    @DisplayName("alertas de vencimiento (SWR-07)")
    class Alertas {

        @Test
        @DisplayName("Busca PQR que vencen dentro de las próximas 48 horas")
        void ventanaPorDefecto() {
            servicio.ejecutarMonitoreo();

            verify(pqrRepository).buscarProximasAVencer(ESTADOS_CON_PLAZO, AHORA, AHORA.plusHours(48));
        }

        @Test
        @DisplayName("La ventana de alerta se toma de la configuración")
        void ventanaConfigurable() {
            crearServicio(24).ejecutarMonitoreo();

            verify(pqrRepository).buscarProximasAVencer(ESTADOS_CON_PLAZO, AHORA, AHORA.plusHours(24));
        }

        @Test
        @DisplayName("Notifica al gestor y marca la alerta como enviada en cada PQR")
        void enviaAlertas() {
            Pqr primera = pqrEn(EstadoPqrTipo.RADICADO, laura);
            Pqr segunda = pqrEn(EstadoPqrTipo.EN_TRAMITE, laura);
            when(pqrRepository.buscarProximasAVencer(any(), any(), any())).thenReturn(List.of(primera, segunda));

            ResultadoMonitoreoResponse resultado = servicio.ejecutarMonitoreo();

            assertEquals(2, resultado.alertasEnviadas());
            verify(notificaciones).notificarAlertaVencimiento(primera, AHORA);
            verify(notificaciones).notificarAlertaVencimiento(segunda, AHORA);
            assertTrue(primera.isNotificadoVencimiento());
            assertTrue(segunda.isNotificadoVencimiento());
            assertEquals(EstadoPqrTipo.RADICADO, primera.getEstadoTipo(), "la alerta no cambia el estado");
        }

        @Test
        @DisplayName("Si la notificación falla, la alerta no queda marcada como enviada")
        void fallaNotificacion() {
            Pqr pqr = pqrEn(EstadoPqrTipo.EN_TRAMITE, laura);
            when(pqrRepository.buscarProximasAVencer(any(), any(), any())).thenReturn(List.of(pqr));
            doThrow(new IllegalStateException("Servidor de correo caído"))
                    .when(notificaciones).notificarAlertaVencimiento(pqr, AHORA);

            assertThrows(IllegalStateException.class, () -> servicio.ejecutarMonitoreo());
            assertFalse(pqr.isNotificadoVencimiento());
        }
    }

    @Test
    @DisplayName("Reporta vencidas y alertas en la misma ejecución")
    void resultadoCombinado() {
        when(pqrRepository.buscarConPlazoVencido(any(), any()))
                .thenReturn(List.of(pqrEn(EstadoPqrTipo.RADICADO, laura)));
        when(pqrRepository.buscarProximasAVencer(any(), any(), any())).thenReturn(List.of(
                pqrEn(EstadoPqrTipo.EN_TRAMITE, laura),
                pqrEn(EstadoPqrTipo.EN_TRAMITE, laura),
                pqrEn(EstadoPqrTipo.RADICADO, laura)));

        ResultadoMonitoreoResponse resultado = servicio.ejecutarMonitoreo();

        assertEquals(1, resultado.pqrMarcadasVencidas());
        assertEquals(3, resultado.alertasEnviadas());
    }
}