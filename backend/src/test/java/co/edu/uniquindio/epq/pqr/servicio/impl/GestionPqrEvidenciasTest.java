package co.edu.uniquindio.epq.pqr.servicio.impl;

import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.RADICADO;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.gestor;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.pqrEn;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.pqr.DatosPruebaPqr;
import co.edu.uniquindio.epq.pqr.EvidenciaPqrProperties;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.repositorio.EvidenciaPqrRepository;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;
import co.edu.uniquindio.epq.pqr.servicio.ResolutorCatalogosPqr;
import co.edu.uniquindio.epq.pqr.web.dto.ResponderPqrRequest;

/** RN-06 / RF-09: la respuesta de una PQR debe quedar soportada con evidencia documental. */
class GestionPqrEvidenciasTest {

    private static final String RESPUESTA = "Se revisó la lectura del medidor y se ajustará el valor facturado.";

    private final PqrRepository pqrRepository = mock(PqrRepository.class);
    private final ResolutorCatalogosPqr catalogos = mock(ResolutorCatalogosPqr.class);
    private final ApplicationEventPublisher eventos = mock(ApplicationEventPublisher.class);
    private final EvidenciaPqrRepository evidencias = mock(EvidenciaPqrRepository.class);
    private final Gestor laura = gestor(1, "Laura Giraldo", true);

    private GestionPqrServiceImpl servicio;
    private Pqr pqr;

    @BeforeEach
    void configurar() {
        servicio = new GestionPqrServiceImpl(pqrRepository, catalogos, eventos,
                Clock.fixed(Instant.parse("2026-09-15T15:00:00Z"), ZoneId.of("America/Bogota")), evidencias,
                new EvidenciaPqrProperties(true, 5_242_880L, List.of("application/pdf")));
        when(catalogos.estado(any())).thenAnswer(inv -> DatosPruebaPqr.estado(inv.getArgument(0)));
        when(catalogos.gestorActivo(1)).thenReturn(laura);
        pqr = pqrEn(EstadoPqrTipo.EN_TRAMITE, laura);
        when(pqrRepository.findByRadicado(RADICADO)).thenReturn(Optional.of(pqr));
    }

    @Test
    void noPermiteResponderSinEvidencias() {
        when(evidencias.countByPqrId(pqr.getId())).thenReturn(0L);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.responder(RADICADO, new ResponderPqrRequest(1, RESPUESTA)));

        assertTrue(error.getMessage().contains("RN-06"));
        assertEquals(EstadoPqrTipo.EN_TRAMITE, pqr.getEstadoTipo());
        verify(eventos, never()).publishEvent(any());
    }

    @Test
    void conEvidenciaAdjuntaLaPqrQuedaResuelta() {
        when(evidencias.countByPqrId(pqr.getId())).thenReturn(1L);

        servicio.responder(RADICADO, new ResponderPqrRequest(1, RESPUESTA));

        assertEquals(EstadoPqrTipo.RESUELTO, pqr.getEstadoTipo());
    }
}
