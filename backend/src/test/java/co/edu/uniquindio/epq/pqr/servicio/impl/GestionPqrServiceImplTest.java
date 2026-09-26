package co.edu.uniquindio.epq.pqr.servicio.impl;

import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.RADICADO;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.gestor;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.pqrEn;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.comun.excepcion.TransicionEstadoInvalidaException;
import co.edu.uniquindio.epq.pqr.DatosPruebaPqr;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.evento.PqrAsignadaEvento;
import co.edu.uniquindio.epq.pqr.evento.PqrEstadoCambiadoEvento;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;
import co.edu.uniquindio.epq.pqr.servicio.ResolutorCatalogosPqr;
import co.edu.uniquindio.epq.pqr.web.dto.CerrarPqrRequest;
import co.edu.uniquindio.epq.pqr.web.dto.PqrDetalleResponse;
import co.edu.uniquindio.epq.pqr.web.dto.ReasignarPqrRequest;
import co.edu.uniquindio.epq.pqr.web.dto.ResponderPqrRequest;
import co.edu.uniquindio.epq.pqr.web.dto.TomarPqrRequest;

@DisplayName("GestionPqrServiceImpl - ciclo de vida de la PQR")
class GestionPqrServiceImplTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final String RESPUESTA_VALIDA = "Se revisó la lectura del medidor y se ajustará el valor facturado.";

    private final PqrRepository pqrRepository = mock(PqrRepository.class);
    private final ResolutorCatalogosPqr catalogos = mock(ResolutorCatalogosPqr.class);
    private final ApplicationEventPublisher eventos = mock(ApplicationEventPublisher.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-15T15:00:00Z"), ZONA);

    private final Gestor laura = gestor(1, "Laura Giraldo", true);
    private final Gestor andres = gestor(2, "Andres Ocampo", true);

    private GestionPqrServiceImpl servicio;

    @BeforeEach
    void configurar() {
        servicio = new GestionPqrServiceImpl(pqrRepository, catalogos, eventos, clock);
        when(catalogos.estado(any())).thenAnswer(inv -> DatosPruebaPqr.estado(inv.getArgument(0)));
        when(catalogos.gestorActivo(1)).thenReturn(laura);
        when(catalogos.gestorActivo(2)).thenReturn(andres);
    }

    private Pqr registrarEnRepositorio(Pqr pqr) {
        when(pqrRepository.findByRadicado(RADICADO)).thenReturn(Optional.of(pqr));
        return pqr;
    }

    private List<Object> eventosPublicados(int cantidad) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventos, times(cantidad)).publishEvent(captor.capture());
        return captor.getAllValues();
    }

    // ------------------------------------------------------------------ tomar

    @Nested
    @DisplayName("tomar")
    class Tomar {

        @Test
        @DisplayName("El gestor asignado toma la PQR: pasa a En trámite y solo publica el cambio de estado")
        void gestorAsignadoTomaLaPqr() {
            Pqr pqr = registrarEnRepositorio(pqrEn(EstadoPqrTipo.RADICADO, laura));

            PqrDetalleResponse respuesta = servicio.tomar(RADICADO, new TomarPqrRequest(1));

            assertEquals(EstadoPqrTipo.EN_TRAMITE, pqr.getEstadoTipo());
            assertEquals(EstadoPqrTipo.EN_TRAMITE.getNombre(), respuesta.estado());
            assertEquals(List.of("RESUELTO"), respuesta.accionesDisponibles());

            PqrEstadoCambiadoEvento evento = assertInstanceOf(PqrEstadoCambiadoEvento.class, eventosPublicados(1).get(0));
            assertAll(
                    () -> assertEquals(EstadoPqrTipo.RADICADO, evento.estadoAnterior()),
                    () -> assertEquals(EstadoPqrTipo.EN_TRAMITE, evento.estadoNuevo()),
                    () -> assertEquals(laura.getCorreo(), evento.usuario()));
        }

        @Test
        @DisplayName("Otro gestor toma la PQR: se le asigna y se publican los eventos de asignación y de estado")
        void otroGestorTomaLaPqr() {
            Pqr pqr = registrarEnRepositorio(pqrEn(EstadoPqrTipo.RADICADO, laura));

            servicio.tomar(RADICADO, new TomarPqrRequest(2));

            assertSame(andres, pqr.getGestor());
            List<Object> publicados = eventosPublicados(2);
            PqrAsignadaEvento asignacion = assertInstanceOf(PqrAsignadaEvento.class, publicados.get(0));
            assertEquals(2, asignacion.gestorId());
            assertInstanceOf(PqrEstadoCambiadoEvento.class, publicados.get(1));
        }

        @Test
        @DisplayName("El radicado se busca sin espacios en los extremos")
        void recortaElRadicado() {
            registrarEnRepositorio(pqrEn(EstadoPqrTipo.RADICADO, laura));

            servicio.tomar("  " + RADICADO + "  ", new TomarPqrRequest(1));

            verify(pqrRepository).findByRadicado(RADICADO);
        }

        @Test
        @DisplayName("Radicado inexistente: lanza RecursoNoEncontradoException y no publica eventos")
        void radicadoInexistente() {
            when(pqrRepository.findByRadicado(RADICADO)).thenReturn(Optional.empty());

            assertThrows(RecursoNoEncontradoException.class, () -> servicio.tomar(RADICADO, new TomarPqrRequest(1)));
            verify(eventos, never()).publishEvent(any(Object.class));
        }

        @Test
        @DisplayName("Gestor inactivo: se propaga la regla de negocio y la PQR no cambia")
        void gestorInactivo() {
            Pqr pqr = registrarEnRepositorio(pqrEn(EstadoPqrTipo.RADICADO, laura));
            when(catalogos.gestorActivo(4)).thenThrow(new ReglaNegocioException("El gestor 'Jorge' está inactivo"));

            assertThrows(ReglaNegocioException.class, () -> servicio.tomar(RADICADO, new TomarPqrRequest(4)));
            assertEquals(EstadoPqrTipo.RADICADO, pqr.getEstadoTipo());
            verify(eventos, never()).publishEvent(any(Object.class));
        }

        @Test
        @DisplayName("PQR resuelta: no se puede volver a tomar (transición inválida)")
        void pqrResuelta() {
            Pqr pqr = registrarEnRepositorio(pqrEn(EstadoPqrTipo.RESUELTO, laura));

            assertThrows(TransicionEstadoInvalidaException.class,
                    () -> servicio.tomar(RADICADO, new TomarPqrRequest(1)));
            assertEquals(EstadoPqrTipo.RESUELTO, pqr.getEstadoTipo());
            verify(eventos, never()).publishEvent(any(Object.class));
        }

        @Test
        @DisplayName("PQR vencida: un gestor puede tomarla y pasa a En trámite")
        void pqrVencida() {
            Pqr pqr = registrarEnRepositorio(pqrEn(EstadoPqrTipo.VENCIDO, laura));

            servicio.tomar(RADICADO, new TomarPqrRequest(1));

            assertEquals(EstadoPqrTipo.EN_TRAMITE, pqr.getEstadoTipo());
        }
    }

    // ------------------------------------------------------------------ reasignar

    @Nested
    @DisplayName("reasignar")
    class Reasignar {

        @Test
        @DisplayName("Cambia el gestor, conserva el estado y publica el evento con motivo y usuario")
        void reasignaAOtroGestor() {
            Pqr pqr = registrarEnRepositorio(pqrEn(EstadoPqrTipo.EN_TRAMITE, laura));

            PqrDetalleResponse respuesta = servicio.reasignar(RADICADO,
                    new ReasignarPqrRequest(2, "coordinador.pqr", "Balanceo de carga"));

            assertSame(andres, pqr.getGestor());
            assertEquals(EstadoPqrTipo.EN_TRAMITE.getNombre(), respuesta.estado());
            PqrAsignadaEvento evento = assertInstanceOf(PqrAsignadaEvento.class, eventosPublicados(1).get(0));
            assertAll(
                    () -> assertEquals(2, evento.gestorId()),
                    () -> assertEquals("Balanceo de carga", evento.motivo()),
                    () -> assertEquals("coordinador.pqr", evento.usuario()));
        }

        @Test
        @DisplayName("Reasignar al mismo gestor: lanza ReglaNegocioException y no publica eventos")
        void mismoGestor() {
            registrarEnRepositorio(pqrEn(EstadoPqrTipo.EN_TRAMITE, laura));

            assertThrows(ReglaNegocioException.class,
                    () -> servicio.reasignar(RADICADO, new ReasignarPqrRequest(1, "coordinador.pqr", null)));
            verify(eventos, never()).publishEvent(any(Object.class));
        }

        @Test
        @DisplayName("PQR cerrada: no se puede reasignar")
        void pqrCerrada() {
            Pqr pqr = registrarEnRepositorio(pqrEn(EstadoPqrTipo.CERRADO, laura));

            assertThrows(ReglaNegocioException.class,
                    () -> servicio.reasignar(RADICADO, new ReasignarPqrRequest(2, "coordinador.pqr", null)));
            assertSame(laura, pqr.getGestor());
        }
    }

    // ------------------------------------------------------------------ responder

    @Nested
    @DisplayName("responder")
    class Responder {

        @Test
        @DisplayName("PQR en trámite: pasa a Resuelto con la respuesta recortada y la fecha del reloj")
        void respondePqrEnTramite() {
            Pqr pqr = registrarEnRepositorio(pqrEn(EstadoPqrTipo.EN_TRAMITE, laura));

            PqrDetalleResponse respuesta = servicio.responder(RADICADO,
                    new ResponderPqrRequest(1, "   " + RESPUESTA_VALIDA + "   "));

            assertAll(
                    () -> assertEquals(EstadoPqrTipo.RESUELTO, pqr.getEstadoTipo()),
                    () -> assertEquals(RESPUESTA_VALIDA, pqr.getRespuesta()),
                    () -> assertEquals(LocalDateTime.now(clock), pqr.getFechaResolucion()),
                    () -> assertEquals(List.of("CERRADO"), respuesta.accionesDisponibles()));

            PqrEstadoCambiadoEvento evento = assertInstanceOf(PqrEstadoCambiadoEvento.class, eventosPublicados(1).get(0));
            assertEquals("Respuesta formal registrada", evento.comentario());
        }

        @Test
        @DisplayName("PQR vencida: se admite la respuesta y se audita como extemporánea")
        void respondePqrVencida() {
            Pqr pqr = registrarEnRepositorio(pqrEn(EstadoPqrTipo.VENCIDO, laura));

            servicio.responder(RADICADO, new ResponderPqrRequest(1, RESPUESTA_VALIDA));

            assertEquals(EstadoPqrTipo.RESUELTO, pqr.getEstadoTipo());
            PqrEstadoCambiadoEvento evento = assertInstanceOf(PqrEstadoCambiadoEvento.class, eventosPublicados(1).get(0));
            assertEquals(EstadoPqrTipo.VENCIDO, evento.estadoAnterior());
            assertEquals("Respuesta extemporánea registrada", evento.comentario());
        }

        @Test
        @DisplayName("Gestor no asignado: lanza ReglaNegocioException y la PQR sigue en trámite")
        void gestorNoAsignado() {
            Pqr pqr = registrarEnRepositorio(pqrEn(EstadoPqrTipo.EN_TRAMITE, laura));

            assertThrows(ReglaNegocioException.class,
                    () -> servicio.responder(RADICADO, new ResponderPqrRequest(2, RESPUESTA_VALIDA)));
            assertEquals(EstadoPqrTipo.EN_TRAMITE, pqr.getEstadoTipo());
            assertNull(pqr.getRespuesta());
        }

        @Test
        @DisplayName("PQR radicada sin tomar: no admite respuesta (transición inválida)")
        void pqrRadicada() {
            registrarEnRepositorio(pqrEn(EstadoPqrTipo.RADICADO, laura));

            assertThrows(TransicionEstadoInvalidaException.class,
                    () -> servicio.responder(RADICADO, new ResponderPqrRequest(1, RESPUESTA_VALIDA)));
            verify(eventos, never()).publishEvent(any(Object.class));
        }

        @Test
        @DisplayName("Respuesta en blanco: se rechaza por RN-06")
        void respuestaEnBlanco() {
            Pqr pqr = registrarEnRepositorio(pqrEn(EstadoPqrTipo.EN_TRAMITE, laura));

            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> servicio.responder(RADICADO, new ResponderPqrRequest(1, "   ")));
            assertTrue(error.getMessage().contains("RN-06"));
            assertEquals(EstadoPqrTipo.EN_TRAMITE, pqr.getEstadoTipo());
        }
    }

    // ------------------------------------------------------------------ cerrar

    @Nested
    @DisplayName("cerrar")
    class Cerrar {

        @Test
        @DisplayName("PQR resuelta sin observación: pasa a Cerrado con comentario por defecto")
        void cierraSinObservacion() {
            Pqr pqr = registrarEnRepositorio(pqrEn(EstadoPqrTipo.RESUELTO, laura));

            PqrDetalleResponse respuesta = servicio.cerrar(RADICADO, new CerrarPqrRequest("coordinador.pqr", null));

            assertEquals(EstadoPqrTipo.CERRADO, pqr.getEstadoTipo());
            assertTrue(respuesta.accionesDisponibles().isEmpty());
            PqrEstadoCambiadoEvento evento = assertInstanceOf(PqrEstadoCambiadoEvento.class, eventosPublicados(1).get(0));
            assertAll(
                    () -> assertEquals("Cierre del trámite", evento.comentario()),
                    () -> assertEquals("coordinador.pqr", evento.usuario()));
        }

        @Test
        @DisplayName("PQR resuelta con observación: la incluye recortada en el comentario")
        void cierraConObservacion() {
            registrarEnRepositorio(pqrEn(EstadoPqrTipo.RESUELTO, laura));

            servicio.cerrar(RADICADO, new CerrarPqrRequest("coordinador.pqr", "  Ciudadano conforme  "));

            PqrEstadoCambiadoEvento evento = assertInstanceOf(PqrEstadoCambiadoEvento.class, eventosPublicados(1).get(0));
            assertEquals("Cierre del trámite: Ciudadano conforme", evento.comentario());
        }

        @Test
        @DisplayName("PQR en trámite: no se puede cerrar sin respuesta (transición inválida)")
        void pqrEnTramite() {
            Pqr pqr = registrarEnRepositorio(pqrEn(EstadoPqrTipo.EN_TRAMITE, laura));

            assertThrows(TransicionEstadoInvalidaException.class,
                    () -> servicio.cerrar(RADICADO, new CerrarPqrRequest("coordinador.pqr", null)));
            assertEquals(EstadoPqrTipo.EN_TRAMITE, pqr.getEstadoTipo());
            verify(eventos, never()).publishEvent(any(Object.class));
        }
    }
}