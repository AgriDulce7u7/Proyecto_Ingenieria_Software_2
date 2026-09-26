package co.edu.uniquindio.epq.pqr.servicio.impl;

import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.RADICADO;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.gestor;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.pqrEn;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.dominio.HistorialPqr;
import co.edu.uniquindio.epq.pqr.dominio.Notificacion;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.dominio.TipoNotificacion;
import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitudTipo;
import co.edu.uniquindio.epq.pqr.repositorio.HistorialPqrRepository;
import co.edu.uniquindio.epq.pqr.repositorio.NotificacionRepository;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;
import co.edu.uniquindio.epq.pqr.servicio.FiltroPqr;
import co.edu.uniquindio.epq.pqr.web.dto.ConsultaEstadoPqrResponse;
import co.edu.uniquindio.epq.pqr.web.dto.HistorialPqrResponse;
import co.edu.uniquindio.epq.pqr.web.dto.NotificacionResponse;
import co.edu.uniquindio.epq.pqr.web.dto.PqrDetalleResponse;
import co.edu.uniquindio.epq.pqr.web.dto.PqrResumenResponse;

@DisplayName("ConsultaPqrServiceImpl - consultas de PQR")
class ConsultaPqrServiceImplTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 9, 2, 9, 30);

    private final PqrRepository pqrRepository = mock(PqrRepository.class);
    private final HistorialPqrRepository historialRepository = mock(HistorialPqrRepository.class);
    private final NotificacionRepository notificacionRepository = mock(NotificacionRepository.class);

    private final ConsultaPqrServiceImpl servicio =
            new ConsultaPqrServiceImpl(pqrRepository, historialRepository, notificacionRepository);

    private final Gestor laura = gestor(1, "Laura Giraldo", true);

    private Pqr registrar(Pqr pqr) {
        when(pqrRepository.findByRadicado(RADICADO)).thenReturn(Optional.of(pqr));
        return pqr;
    }

    // ------------------------------------------------------------------ por radicado

    @Nested
    @DisplayName("consultas por radicado")
    class PorRadicado {

        @Test
        @DisplayName("Consulta pública: estado y fechas, buscando el radicado sin espacios")
        void consultaPublica() {
            registrar(pqrEn(EstadoPqrTipo.EN_TRAMITE, laura));

            ConsultaEstadoPqrResponse r = servicio.consultarEstadoPublico("  " + RADICADO + " ");

            assertAll(
                    () -> assertEquals(RADICADO, r.radicado()),
                    () -> assertEquals("Reclamo", r.tipoSolicitud()),
                    () -> assertEquals(EstadoPqrTipo.EN_TRAMITE.getNombre(), r.estado()),
                    () -> assertNull(r.fechaResolucion()));
            verify(pqrRepository).findByRadicado(RADICADO);
        }

        @Test
        @DisplayName("Detalle: incluye ciudadano, gestor y acciones disponibles")
        void detalle() {
            registrar(pqrEn(EstadoPqrTipo.RADICADO, laura));

            PqrDetalleResponse r = servicio.obtenerDetalle(RADICADO);

            assertAll(
                    () -> assertEquals("Ana Gómez", r.ciudadano().nombreCompleto()),
                    () -> assertEquals("Laura Giraldo", r.gestor().nombre()),
                    () -> assertEquals("Web", r.canal()),
                    () -> assertEquals(List.of("EN_TRAMITE"), r.accionesDisponibles()));
        }

        @Test
        @DisplayName("Historial: consulta por el id de la PQR y mapea estado, comentario y usuario")
        void historial() {
            Pqr pqr = registrar(pqrEn(EstadoPqrTipo.RADICADO, laura));
            when(historialRepository.findByPqrIdOrderByFechaCambioAscIdAsc(pqr.getId()))
                    .thenReturn(List.of(HistorialPqr.registrar(pqr, "Radicación de la PQR", "SISTEMA", FECHA)));

            List<HistorialPqrResponse> historial = servicio.obtenerHistorial(RADICADO);

            assertEquals(1, historial.size());
            assertAll(
                    () -> assertEquals("Radicado", historial.get(0).estado()),
                    () -> assertEquals("Radicación de la PQR", historial.get(0).comentario()),
                    () -> assertEquals("SISTEMA", historial.get(0).usuario()),
                    () -> assertEquals(FECHA, historial.get(0).fechaCambio()));
        }

        @Test
        @DisplayName("Notificaciones: el destinatario es el gestor o 'Ciudadano' si no hay gestor")
        void notificaciones() {
            Pqr pqr = registrar(pqrEn(EstadoPqrTipo.RADICADO, laura));
            when(notificacionRepository.findByPqrIdOrderByFechaEnvioDescIdDesc(pqr.getId())).thenReturn(List.of(
                    Notificacion.registrar(pqr, laura, TipoNotificacion.ASIGNACION_GESTOR, "Nueva PQR", FECHA, true),
                    Notificacion.registrar(pqr, null, TipoNotificacion.RADICACION_CIUDADANO, "Radicada", FECHA, false)));

            List<NotificacionResponse> notificaciones = servicio.obtenerNotificaciones(RADICADO);

            assertAll(
                    () -> assertEquals("Laura Giraldo", notificaciones.get(0).destinatario()),
                    () -> assertEquals("asignacion_gestor", notificaciones.get(0).tipo()),
                    () -> assertTrue(notificaciones.get(0).enviado()),
                    () -> assertEquals("Ciudadano", notificaciones.get(1).destinatario()),
                    () -> assertEquals(RADICADO, notificaciones.get(1).radicado()));
        }

        @Test
        @DisplayName("Radicado inexistente: todas las consultas lanzan RecursoNoEncontradoException")
        void radicadoInexistente() {
            when(pqrRepository.findByRadicado(any())).thenReturn(Optional.empty());

            List<Executable> consultas = List.of(
                    () -> servicio.consultarEstadoPublico(RADICADO),
                    () -> servicio.obtenerDetalle(RADICADO),
                    () -> servicio.obtenerHistorial(RADICADO),
                    () -> servicio.obtenerNotificaciones(RADICADO));
            consultas.forEach(consulta -> assertThrows(RecursoNoEncontradoException.class, consulta));
        }
    }

    // ------------------------------------------------------------------ bandeja

    @Nested
    @DisplayName("bandeja del gestor (listar)")
    class Listar {

        @Test
        @DisplayName("Ordena por fecha límite ascendente (lo más urgente primero) y mapea el resumen")
        @SuppressWarnings("unchecked")
        void ordenaPorFechaLimite() {
            when(pqrRepository.findAll(any(Specification.class), any(Sort.class)))
                    .thenReturn(List.of(pqrEn(EstadoPqrTipo.EN_TRAMITE, laura)));

            List<PqrResumenResponse> bandeja = servicio.listar(new FiltroPqr(null, null, null, null));

            ArgumentCaptor<Sort> orden = ArgumentCaptor.forClass(Sort.class);
            verify(pqrRepository).findAll(any(Specification.class), orden.capture());
            assertEquals(Sort.by(Sort.Direction.ASC, "fechaLimiteRespuesta"), orden.getValue());
            assertAll(
                    () -> assertEquals(RADICADO, bandeja.get(0).radicado()),
                    () -> assertEquals("Ana Gómez", bandeja.get(0).ciudadano()),
                    () -> assertEquals("Laura Giraldo", bandeja.get(0).gestor()));
        }

        @Test
        @DisplayName("PQR sin gestor: el resumen muestra gestor null")
        @SuppressWarnings("unchecked")
        void pqrSinGestor() {
            when(pqrRepository.findAll(any(Specification.class), any(Sort.class)))
                    .thenReturn(List.of(pqrEn(EstadoPqrTipo.RADICADO, null)));

            assertNull(servicio.listar(new FiltroPqr(null, null, null, null)).get(0).gestor());
        }

        @Test
        @DisplayName("Acepta todos los filtros combinados y documento en blanco")
        @SuppressWarnings("unchecked")
        void filtrosCombinados() {
            when(pqrRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of());

            assertTrue(servicio.listar(new FiltroPqr(EstadoPqrTipo.RADICADO, TipoSolicitudTipo.QUEJA, 1,
                    " 1094000111 ")).isEmpty());
            assertTrue(servicio.listar(new FiltroPqr(null, null, null, "   ")).isEmpty());
        }
    }
}