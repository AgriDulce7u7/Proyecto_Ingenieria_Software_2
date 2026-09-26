package co.edu.uniquindio.epq.pqr.servicio.impl;

import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.canal;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.estado;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.gestor;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.pqrEn;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.tipoSolicitud;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.dominio.Notificacion;
import co.edu.uniquindio.epq.pqr.dominio.TipoNotificacion;
import co.edu.uniquindio.epq.pqr.repositorio.CanalAtencionRepository;
import co.edu.uniquindio.epq.pqr.repositorio.EstadoPqrRepository;
import co.edu.uniquindio.epq.pqr.repositorio.GestorRepository;
import co.edu.uniquindio.epq.pqr.repositorio.NotificacionRepository;
import co.edu.uniquindio.epq.pqr.repositorio.TipoSolicitudRepository;
import co.edu.uniquindio.epq.pqr.web.dto.ElementoCatalogoResponse;
import co.edu.uniquindio.epq.pqr.web.dto.GestorResponse;
import co.edu.uniquindio.epq.pqr.web.dto.NotificacionResponse;

@DisplayName("CatalogoPqrServiceImpl - catálogos para el frontend")
class CatalogoPqrServiceImplTest {

    private final TipoSolicitudRepository tipoSolicitudRepository = mock(TipoSolicitudRepository.class);
    private final CanalAtencionRepository canalRepository = mock(CanalAtencionRepository.class);
    private final EstadoPqrRepository estadoRepository = mock(EstadoPqrRepository.class);
    private final GestorRepository gestorRepository = mock(GestorRepository.class);
    private final NotificacionRepository notificacionRepository = mock(NotificacionRepository.class);

    private final CatalogoPqrServiceImpl servicio = new CatalogoPqrServiceImpl(tipoSolicitudRepository,
            canalRepository, estadoRepository, gestorRepository, notificacionRepository);

    @Test
    @DisplayName("Tipos de solicitud: el código es el enum que espera la API y el nombre el de la BD")
    void tiposSolicitud() {
        when(tipoSolicitudRepository.findAll()).thenReturn(List.of(
                tipoSolicitud("Petición"), tipoSolicitud("Queja"), tipoSolicitud("Reclamo")));

        List<ElementoCatalogoResponse> tipos = servicio.tiposSolicitud();

        assertEquals(List.of("PETICION", "QUEJA", "RECLAMO"), tipos.stream().map(ElementoCatalogoResponse::codigo).toList());
        assertEquals("Petición", tipos.get(0).nombre());
    }

    @Test
    @DisplayName("Canales: traduce nombres con tilde y espacios a su código")
    void canales() {
        when(canalRepository.findAll()).thenReturn(List.of(canal("Web"), canal("Telefónico"), canal("Correo electrónico")));

        List<ElementoCatalogoResponse> canales = servicio.canales();

        assertEquals(List.of("WEB", "TELEFONICO", "CORREO"), canales.stream().map(ElementoCatalogoResponse::codigo).toList());
    }

    @Test
    @DisplayName("Estados: respeta el orden de la BD y traduce a su código")
    void estados() {
        when(estadoRepository.findAllByOrderByOrdenAsc()).thenReturn(List.of(
                estado(EstadoPqrTipo.RADICADO), estado(EstadoPqrTipo.EN_TRAMITE), estado(EstadoPqrTipo.RESUELTO)));

        List<ElementoCatalogoResponse> estados = servicio.estados();

        assertEquals(List.of("RADICADO", "EN_TRAMITE", "RESUELTO"),
                estados.stream().map(ElementoCatalogoResponse::codigo).toList());
        assertEquals("En trámite", estados.get(1).nombre());
    }

    @Test
    @DisplayName("Gestores activos: mapea id, nombre, correo, área y estado")
    void gestoresActivos() {
        Gestor laura = gestor(1, "Laura Giraldo", true);
        when(gestorRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(laura));

        GestorResponse r = servicio.gestoresActivos().get(0);

        assertAll(
                () -> assertEquals(1, r.id()),
                () -> assertEquals("Laura Giraldo", r.nombre()),
                () -> assertEquals(laura.getCorreo(), r.correo()),
                () -> assertEquals("Oficina de PQR", r.area()));
    }

    @Test
    @DisplayName("Notificaciones de un gestor existente: consulta su bandeja")
    void notificacionesDeGestor() {
        Gestor laura = gestor(1, "Laura Giraldo", true);
        when(gestorRepository.existsById(1)).thenReturn(true);
        when(notificacionRepository.findByGestorIdOrderByFechaEnvioDescIdDesc(1)).thenReturn(List.of(
                Notificacion.registrar(pqrEn(EstadoPqrTipo.EN_TRAMITE, laura), laura, TipoNotificacion.VENCIMIENTO_48H,
                        "La PQR vence en 48 horas", LocalDateTime.of(2026, 9, 20, 8, 0), true)));

        List<NotificacionResponse> bandeja = servicio.notificacionesDeGestor(1);

        assertEquals(1, bandeja.size());
        assertEquals("vencimiento_48h", bandeja.get(0).tipo());
    }

    @Test
    @DisplayName("Notificaciones de un gestor inexistente: 404 sin consultar notificaciones")
    void gestorInexistente() {
        when(gestorRepository.existsById(99)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.notificacionesDeGestor(99));
        verify(notificacionRepository, never()).findByGestorIdOrderByFechaEnvioDescIdDesc(any());
    }
}