package co.edu.uniquindio.epq.pqr.evento;

import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.RADICADO;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.gestor;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.pqrEn;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.HistorialPqr;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.repositorio.HistorialPqrRepository;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;

@DisplayName("AuditoriaPqrListener - trazabilidad de la PQR (SWR-09)")
class AuditoriaPqrListenerTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-15T15:00:00Z"), ZoneId.of("America/Bogota"));
    private static final Long ID = 100L;

    private final PqrRepository pqrRepository = mock(PqrRepository.class);
    private final HistorialPqrRepository historialRepository = mock(HistorialPqrRepository.class);
    private final AuditoriaPqrListener listener = new AuditoriaPqrListener(pqrRepository, historialRepository, CLOCK);

    private Pqr pqr;

    @BeforeEach
    void configurar() {
        pqr = pqrEn(EstadoPqrTipo.EN_TRAMITE, gestor(1, "Laura Giraldo", true));
        when(pqrRepository.findById(ID)).thenReturn(Optional.of(pqr));
    }

    private HistorialPqr registroGuardado() {
        ArgumentCaptor<HistorialPqr> captor = ArgumentCaptor.forClass(HistorialPqr.class);
        verify(historialRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("Registro: deja constancia del radicado, el canal y el usuario, con la fecha del reloj")
    void alRegistrar() {
        listener.alRegistrar(new PqrRegistradaEvento(ID, RADICADO, "ciudadano:CC-1094000111"));

        HistorialPqr h = registroGuardado();
        assertAll(
                () -> assertSame(pqr, h.getPqr()),
                () -> assertEquals("PQR radicada con número " + RADICADO + " por el canal Web", h.getComentario()),
                () -> assertEquals("ciudadano:CC-1094000111", h.getUsuarioCambio()),
                () -> assertEquals(LocalDateTime.now(CLOCK), h.getFechaCambio()),
                () -> assertEquals(EstadoPqrTipo.EN_TRAMITE, h.getEstado().getTipo()));
    }

    @Test
    @DisplayName("Registro sin canal: indica 'sin canal'")
    void alRegistrarSinCanal() {
        ReflectionTestUtils.setField(pqr, "canal", null);

        listener.alRegistrar(new PqrRegistradaEvento(ID, RADICADO, "ciudadano:CC-1"));

        assertEquals("PQR radicada con número " + RADICADO + " por el canal sin canal", registroGuardado().getComentario());
    }

    @Test
    @DisplayName("Asignación con motivo: incluye el gestor y el motivo")
    void alAsignarConMotivo() {
        listener.alAsignar(new PqrAsignadaEvento(ID, 1, "Balanceo de carga", "coordinador"));

        HistorialPqr h = registroGuardado();
        assertEquals("Asignada al gestor Laura Giraldo. Motivo: Balanceo de carga", h.getComentario());
        assertEquals("coordinador", h.getUsuarioCambio());
    }

    @Test
    @DisplayName("Asignación sin motivo o con motivo en blanco: solo el gestor")
    void alAsignarSinMotivo() {
        listener.alAsignar(new PqrAsignadaEvento(ID, 1, "   ", "SISTEMA"));

        assertEquals("Asignada al gestor Laura Giraldo", registroGuardado().getComentario());
    }

    @Test
    @DisplayName("Cambio de estado: 'anterior → nuevo. comentario'")
    void alCambiarEstado() {
        listener.alCambiarEstado(new PqrEstadoCambiadoEvento(ID, EstadoPqrTipo.RADICADO, EstadoPqrTipo.EN_TRAMITE,
                "Inicio de atención", "laura@epq.com.co"));

        HistorialPqr h = registroGuardado();
        assertEquals("Radicado → En trámite. Inicio de atención", h.getComentario());
        assertEquals("laura@epq.com.co", h.getUsuarioCambio());
    }

    @Test
    @DisplayName("PQR inexistente: RecursoNoEncontradoException y no se escribe historial")
    void pqrInexistente() {
        when(pqrRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> listener.alRegistrar(new PqrRegistradaEvento(999L, RADICADO, "SISTEMA")));
        verify(historialRepository, never()).save(any());
    }
}