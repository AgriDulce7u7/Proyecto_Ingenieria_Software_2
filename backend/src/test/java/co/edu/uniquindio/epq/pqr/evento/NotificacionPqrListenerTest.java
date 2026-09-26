package co.edu.uniquindio.epq.pqr.evento;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.notificacion.ServicioNotificacionPqr;

@DisplayName("NotificacionPqrListener - envío de notificaciones tras el commit")
class NotificacionPqrListenerTest {

    private static final Long ID = 100L;

    private final ServicioNotificacionPqr notificaciones = mock(ServicioNotificacionPqr.class);
    private final NotificacionPqrListener listener = new NotificacionPqrListener(notificaciones);

    private static PqrEstadoCambiadoEvento cambioA(EstadoPqrTipo nuevo) {
        return new PqrEstadoCambiadoEvento(ID, EstadoPqrTipo.EN_TRAMITE, nuevo, "comentario", "SISTEMA");
    }

    @Test
    @DisplayName("Registro: notifica la radicación al ciudadano")
    void alRegistrar() {
        listener.alRegistrar(new PqrRegistradaEvento(ID, "202609-0001", "ciudadano:CC-1"));

        verify(notificaciones).notificarRadicacion(ID);
    }

    @Test
    @DisplayName("Asignación: notifica al gestor")
    void alAsignar() {
        listener.alAsignar(new PqrAsignadaEvento(ID, 1, null, "SISTEMA"));

        verify(notificaciones).notificarAsignacion(ID);
    }

    @Test
    @DisplayName("Paso a Resuelto: notifica la respuesta al ciudadano")
    void alResolver() {
        listener.alCambiarEstado(cambioA(EstadoPqrTipo.RESUELTO));

        verify(notificaciones).notificarRespuesta(ID);
    }

    @Test
    @DisplayName("Paso a Vencido: notifica el vencimiento al gestor")
    void alVencer() {
        listener.alCambiarEstado(cambioA(EstadoPqrTipo.VENCIDO));

        verify(notificaciones).notificarVencimiento(ID);
    }

    @ParameterizedTest(name = "Paso a {0} → sin notificación")
    @EnumSource(value = EstadoPqrTipo.class, names = {"RADICADO", "EN_TRAMITE", "CERRADO"})
    @DisplayName("Otros cambios de estado no generan notificaciones")
    void otrosEstados(EstadoPqrTipo nuevo) {
        listener.alCambiarEstado(cambioA(nuevo));

        verifyNoInteractions(notificaciones);
    }

    @Test
    @DisplayName("Si la notificación falla, el error se registra y no se propaga (la operación ya se confirmó)")
    void falloNoSePropaga() {
        doThrow(new IllegalStateException("Servidor de correo caído")).when(notificaciones).notificarRadicacion(ID);
        doThrow(new IllegalStateException("Servidor de correo caído")).when(notificaciones).notificarRespuesta(ID);

        assertDoesNotThrow(() -> listener.alRegistrar(new PqrRegistradaEvento(ID, "202609-0001", "SISTEMA")));
        assertDoesNotThrow(() -> listener.alCambiarEstado(cambioA(EstadoPqrTipo.RESUELTO)));
    }
}