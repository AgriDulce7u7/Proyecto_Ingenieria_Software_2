package co.edu.uniquindio.epq.pqr.notificacion;

import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.RADICADO;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.gestor;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.pqrEn;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.dominio.Notificacion;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.dominio.TipoNotificacion;
import co.edu.uniquindio.epq.pqr.repositorio.NotificacionRepository;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;

/**
 * La PQR de prueba tiene fecha estimada 22/09/2026 y fecha límite 22/09/2026 23:59 (ver DatosPruebaPqr).
 */
@DisplayName("ServicioNotificacionPqr - redacción, envío y registro de notificaciones")
class ServicioNotificacionPqrTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-15T15:00:00Z"), ZoneId.of("America/Bogota"));
    private static final Long ID = 100L;

    private final PqrRepository pqrRepository = mock(PqrRepository.class);
    private final NotificacionRepository notificacionRepository = mock(NotificacionRepository.class);
    private final CanalNotificacion canal = mock(CanalNotificacion.class);
    private final ServicioNotificacionPqr servicio =
            new ServicioNotificacionPqr(pqrRepository, notificacionRepository, canal, CLOCK);

    private final Gestor laura = gestor(1, "Laura Giraldo", true);
    private Pqr pqr;

    @BeforeEach
    void configurar() {
        pqr = pqrEn(EstadoPqrTipo.EN_TRAMITE, laura);
        when(pqrRepository.findById(ID)).thenReturn(Optional.of(pqr));
        when(canal.enviar(any())).thenReturn(true);
    }

    private MensajeNotificacion mensajeEnviado() {
        ArgumentCaptor<MensajeNotificacion> captor = ArgumentCaptor.forClass(MensajeNotificacion.class);
        verify(canal).enviar(captor.capture());
        return captor.getValue();
    }

    private Notificacion notificacionGuardada() {
        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).save(captor.capture());
        return captor.getValue();
    }

    // ------------------------------------------------------------------ al ciudadano

    @Nested
    @DisplayName("notificaciones al ciudadano")
    class AlCiudadano {

        @Test
        @DisplayName("Radicación: al correo del ciudadano con radicado, tipo y fecha estimada")
        void radicacion() {
            servicio.notificarRadicacion(ID);

            MensajeNotificacion m = mensajeEnviado();
            Notificacion n = notificacionGuardada();
            assertAll(
                    () -> assertEquals("ana@correo.com", m.destinatario()),
                    () -> assertEquals("EPQ - PQR radicada " + RADICADO, m.asunto()),
                    () -> assertTrue(m.cuerpo().startsWith("Estimado(a) Ana Gómez, su reclamo fue radicada")),
                    () -> assertTrue(m.cuerpo().contains("Fecha estimada de respuesta: 22/09/2026")),
                    () -> assertEquals(TipoNotificacion.RADICACION_CIUDADANO, n.getTipo()),
                    () -> assertNull(n.getGestor(), "la notificación al ciudadano no se asocia a un gestor"),
                    () -> assertSame(pqr, n.getPqr()),
                    () -> assertEquals(LocalDateTime.now(CLOCK), n.getFechaEnvio()),
                    () -> assertTrue(n.isEnviadoOk()));
        }

        @Test
        @DisplayName("Respuesta: incluye el texto de la respuesta formal")
        void respuesta() {
            ReflectionTestUtils.setField(pqr, "respuesta", "Se ajustará el valor en la próxima factura.");

            servicio.notificarRespuesta(ID);

            MensajeNotificacion m = mensajeEnviado();
            assertEquals("ana@correo.com", m.destinatario());
            assertTrue(m.cuerpo().endsWith("fue respondida: Se ajustará el valor en la próxima factura."));
            assertEquals(TipoNotificacion.RESPUESTA_CIUDADANO, notificacionGuardada().getTipo());
        }
    }

    // ------------------------------------------------------------------ al gestor

    @Nested
    @DisplayName("notificaciones al gestor")
    class AlGestor {

        @Test
        @DisplayName("Asignación: al correo del gestor con asunto y fecha límite")
        void asignacion() {
            servicio.notificarAsignacion(ID);

            MensajeNotificacion m = mensajeEnviado();
            Notificacion n = notificacionGuardada();
            assertAll(
                    () -> assertEquals(laura.getCorreo(), m.destinatario()),
                    () -> assertTrue(m.cuerpo().contains("\"Cobro elevado\"")),
                    () -> assertTrue(m.cuerpo().contains("Fecha límite de respuesta: 22/09/2026 23:59")),
                    () -> assertEquals(TipoNotificacion.ASIGNACION_GESTOR, n.getTipo()),
                    () -> assertSame(laura, n.getGestor()));
        }

        @Test
        @DisplayName("Asignación sin gestor: no envía ni registra nada")
        void asignacionSinGestor() {
            ReflectionTestUtils.setField(pqr, "gestor", null);

            servicio.notificarAsignacion(ID);

            verifyNoInteractions(canal);
            verify(notificacionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Vencimiento: avisa al gestor y menciona el reporte a la SSPD")
        void vencimiento() {
            servicio.notificarVencimiento(ID);

            MensajeNotificacion m = mensajeEnviado();
            assertEquals(laura.getCorreo(), m.destinatario());
            assertTrue(m.asunto().contains("PQR VENCIDA"));
            assertTrue(m.cuerpo().contains("SSPD"));
            assertEquals(TipoNotificacion.PQR_VENCIDA, notificacionGuardada().getTipo());
        }

        @Test
        @DisplayName("Vencimiento sin gestor: se registra con '(sin correo)'")
        void vencimientoSinGestor() {
            ReflectionTestUtils.setField(pqr, "gestor", null);
            when(canal.enviar(any())).thenReturn(false);

            servicio.notificarVencimiento(ID);

            assertNull(mensajeEnviado().destinatario());
            Notificacion n = notificacionGuardada();
            assertTrue(n.getMensaje().contains("Para: (sin correo)"));
            assertFalse(n.isEnviadoOk());
        }

        @Test
        @DisplayName("Alerta de 48 h: calcula las horas restantes hasta la fecha límite")
        void alertaVencimiento() {
            servicio.notificarAlertaVencimiento(pqr, LocalDateTime.of(2026, 9, 21, 0, 0));

            MensajeNotificacion m = mensajeEnviado();
            assertTrue(m.cuerpo().contains("aproximadamente 47 horas"), m.cuerpo());
            assertEquals(TipoNotificacion.VENCIMIENTO_48H, notificacionGuardada().getTipo());
        }

        @Test
        @DisplayName("Alerta con el plazo ya cumplido: las horas restantes nunca son negativas")
        void alertaSinHorasNegativas() {
            servicio.notificarAlertaVencimiento(pqr, LocalDateTime.of(2026, 9, 25, 0, 0));

            assertTrue(mensajeEnviado().cuerpo().contains("aproximadamente 0 horas"));
        }
    }

    // ------------------------------------------------------------------ fallos

    @Nested
    @DisplayName("fallos")
    class Fallos {

        @Test
        @DisplayName("Si el canal lanza una excepción, se registra la notificación como no enviada")
        void canalConExcepcion() {
            when(canal.enviar(any())).thenThrow(new IllegalStateException("SMTP no disponible"));

            servicio.notificarRadicacion(ID);

            assertFalse(notificacionGuardada().isEnviadoOk());
        }

        @Test
        @DisplayName("PQR inexistente: RecursoNoEncontradoException sin enviar nada")
        void pqrInexistente() {
            when(pqrRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RecursoNoEncontradoException.class, () -> servicio.notificarRadicacion(999L));
            verifyNoInteractions(canal);
        }
    }
}