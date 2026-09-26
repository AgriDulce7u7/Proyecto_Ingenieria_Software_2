package co.edu.uniquindio.epq.pqr.notificacion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("CanalCorreoSimulado - canal de notificación del prototipo")
class CanalCorreoSimuladoTest {

    private final CanalCorreoSimulado canal = new CanalCorreoSimulado();

    @Test
    @DisplayName("Con destinatario: el envío se considera exitoso")
    void envioConDestinatario() {
        assertTrue(canal.enviar(new MensajeNotificacion("ana@correo.com", "Asunto", "Cuerpo")));
    }

    @ParameterizedTest(name = "Destinatario \"{0}\" → no se envía")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Sin destinatario (null, vacío o espacios): el envío falla")
    void envioSinDestinatario(String destinatario) {
        MensajeNotificacion mensaje = new MensajeNotificacion(destinatario, "Asunto", "Cuerpo");

        assertFalse(mensaje.tieneDestinatario());
        assertFalse(canal.enviar(mensaje));
    }
}