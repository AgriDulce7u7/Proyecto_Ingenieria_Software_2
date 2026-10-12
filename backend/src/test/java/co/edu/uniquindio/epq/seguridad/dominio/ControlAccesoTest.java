package co.edu.uniquindio.epq.seguridad.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** SWR-15: bloqueo temporal tras 3 intentos fallidos consecutivos. */
class ControlAccesoTest {

    private final PoliticaBloqueo politica = new PoliticaBloqueo(3, 15);
    private final LocalDateTime t0 = LocalDateTime.of(2026, 10, 11, 8, 0);

    @Test
    void bloqueaAlTercerIntentoFallidoDurante15Minutos() {
        ControlAcceso control = new ControlAcceso();

        assertFalse(control.registrarFallo(t0, politica));
        assertEquals(2, control.intentosRestantes(politica));
        assertFalse(control.registrarFallo(t0.plusSeconds(10), politica));
        assertTrue(control.registrarFallo(t0.plusSeconds(20), politica));

        assertTrue(control.estaBloqueado(t0.plusMinutes(14)));
        assertFalse(control.estaBloqueado(t0.plusMinutes(16)));
    }

    @Test
    void unAccesoExitosoReiniciaElContador() {
        ControlAcceso control = new ControlAcceso();
        control.registrarFallo(t0, politica);
        control.registrarFallo(t0, politica);

        control.reiniciar();

        assertEquals(0, control.getIntentosFallidos());
        assertNull(control.getBloqueadoHasta());
        assertFalse(control.registrarFallo(t0, politica)); // vuelve a tener 3 oportunidades
    }

    @Test
    void trasExpirarElBloqueoSeConcedenNuevosIntentos() {
        ControlAcceso control = new ControlAcceso();
        for (int i = 0; i < 3; i++) {
            control.registrarFallo(t0, politica);
        }
        LocalDateTime despues = t0.plusMinutes(20);
        assertFalse(control.registrarFallo(despues, politica));
        assertFalse(control.estaBloqueado(despues));
    }
}
