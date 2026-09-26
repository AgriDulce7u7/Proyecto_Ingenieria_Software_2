package co.edu.uniquindio.epq.pqr.servicio;

import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.canal;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.estado;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.gestor;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.tipoSolicitud;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.pqr.dominio.CanalAtencion;
import co.edu.uniquindio.epq.pqr.dominio.CanalAtencionTipo;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqr;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitud;
import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitudTipo;
import co.edu.uniquindio.epq.pqr.repositorio.CanalAtencionRepository;
import co.edu.uniquindio.epq.pqr.repositorio.EstadoPqrRepository;
import co.edu.uniquindio.epq.pqr.repositorio.GestorRepository;
import co.edu.uniquindio.epq.pqr.repositorio.TipoSolicitudRepository;

@DisplayName("ResolutorCatalogosPqr - enums del dominio a registros de catálogo")
class ResolutorCatalogosPqrTest {

    private final EstadoPqrRepository estadoRepository = mock(EstadoPqrRepository.class);
    private final TipoSolicitudRepository tipoSolicitudRepository = mock(TipoSolicitudRepository.class);
    private final CanalAtencionRepository canalRepository = mock(CanalAtencionRepository.class);
    private final GestorRepository gestorRepository = mock(GestorRepository.class);

    private final ResolutorCatalogosPqr resolutor = new ResolutorCatalogosPqr(estadoRepository,
            tipoSolicitudRepository, canalRepository, gestorRepository);

    @Nested
    @DisplayName("catálogos")
    class Catalogos {

        @Test
        @DisplayName("Busca cada catálogo por el nombre con tilde que tiene en la BD")
        void resuelvePorNombre() {
            EstadoPqr enTramite = estado(EstadoPqrTipo.EN_TRAMITE);
            TipoSolicitud peticion = tipoSolicitud("Petición");
            CanalAtencion correo = canal("Correo electrónico");
            when(estadoRepository.findByNombre("En trámite")).thenReturn(Optional.of(enTramite));
            when(tipoSolicitudRepository.findByNombre("Petición")).thenReturn(Optional.of(peticion));
            when(canalRepository.findByNombre("Correo electrónico")).thenReturn(Optional.of(correo));

            assertSame(enTramite, resolutor.estado(EstadoPqrTipo.EN_TRAMITE));
            assertSame(peticion, resolutor.tipoSolicitud(TipoSolicitudTipo.PETICION));
            assertSame(correo, resolutor.canal(CanalAtencionTipo.CORREO));
        }

        @Test
        @DisplayName("Catálogo faltante en la BD: IllegalStateException que sugiere revisar el seed")
        void catalogoFaltante() {
            when(estadoRepository.findByNombre("Cerrado")).thenReturn(Optional.empty());
            when(tipoSolicitudRepository.findByNombre("Queja")).thenReturn(Optional.empty());
            when(canalRepository.findByNombre("Presencial")).thenReturn(Optional.empty());

            IllegalStateException error =
                    assertThrows(IllegalStateException.class, () -> resolutor.estado(EstadoPqrTipo.CERRADO));
            assertTrue(error.getMessage().contains("02_seed_data.sql"));
            assertThrows(IllegalStateException.class, () -> resolutor.tipoSolicitud(TipoSolicitudTipo.QUEJA));
            assertThrows(IllegalStateException.class, () -> resolutor.canal(CanalAtencionTipo.PRESENCIAL));
        }
    }

    @Nested
    @DisplayName("gestorActivo")
    class GestorActivo {

        @Test
        @DisplayName("Gestor activo: lo devuelve")
        void activo() {
            Gestor laura = gestor(1, "Laura Giraldo", true);
            when(gestorRepository.findById(1)).thenReturn(Optional.of(laura));

            assertSame(laura, resolutor.gestorActivo(1));
        }

        @Test
        @DisplayName("Gestor inexistente: RecursoNoEncontradoException (404)")
        void inexistente() {
            when(gestorRepository.findById(99)).thenReturn(Optional.empty());

            assertThrows(RecursoNoEncontradoException.class, () -> resolutor.gestorActivo(99));
        }

        @Test
        @DisplayName("Gestor inactivo: ReglaNegocioException (422) con su nombre")
        void inactivo() {
            when(gestorRepository.findById(4)).thenReturn(Optional.of(gestor(4, "Jorge Arango", false)));

            ReglaNegocioException error = assertThrows(ReglaNegocioException.class, () -> resolutor.gestorActivo(4));
            assertEquals("El gestor 'Jorge Arango' está inactivo", error.getMessage());
        }
    }
}