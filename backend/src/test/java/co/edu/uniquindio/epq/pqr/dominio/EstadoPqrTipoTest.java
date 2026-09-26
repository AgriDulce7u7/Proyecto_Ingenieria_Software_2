package co.edu.uniquindio.epq.pqr.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EstadoPqrTipoTest {

    @Test
    void flujoNormalDelCicloDeVida() {
        assertTrue(EstadoPqrTipo.RADICADO.puedeTransitarA(EstadoPqrTipo.EN_TRAMITE));
        assertTrue(EstadoPqrTipo.EN_TRAMITE.puedeTransitarA(EstadoPqrTipo.RESUELTO));
        assertTrue(EstadoPqrTipo.RESUELTO.puedeTransitarA(EstadoPqrTipo.CERRADO));
    }

    @Test
    void vencimientoYRespuestaExtemporanea() {
        assertTrue(EstadoPqrTipo.RADICADO.puedeTransitarA(EstadoPqrTipo.VENCIDO));
        assertTrue(EstadoPqrTipo.EN_TRAMITE.puedeTransitarA(EstadoPqrTipo.VENCIDO));
        assertTrue(EstadoPqrTipo.VENCIDO.puedeTransitarA(EstadoPqrTipo.RESUELTO));
    }

    @Test
    void transicionesNoPermitidas() {
        assertFalse(EstadoPqrTipo.RADICADO.puedeTransitarA(EstadoPqrTipo.RESUELTO));
        assertFalse(EstadoPqrTipo.RADICADO.puedeTransitarA(EstadoPqrTipo.CERRADO));
        assertFalse(EstadoPqrTipo.RESUELTO.puedeTransitarA(EstadoPqrTipo.VENCIDO));
        assertTrue(EstadoPqrTipo.CERRADO.transicionesPermitidas().isEmpty());
    }

    @Test
    void resuelveNombresDelCatalogo() {
        assertEquals(EstadoPqrTipo.EN_TRAMITE, EstadoPqrTipo.desdeNombre("En trámite"));
        assertThrows(RuntimeException.class, () -> EstadoPqrTipo.desdeNombre("Inexistente"));
    }
}
