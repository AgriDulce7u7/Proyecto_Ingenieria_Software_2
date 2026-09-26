package co.edu.uniquindio.epq.pqr.servicio.impl;

import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.gestor;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.uniquindio.epq.pqr.DatosPruebaPqr;
import co.edu.uniquindio.epq.pqr.dominio.CanalAtencionTipo;
import co.edu.uniquindio.epq.pqr.dominio.Ciudadano;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.dominio.PlazoRespuesta;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitudTipo;
import co.edu.uniquindio.epq.pqr.evento.PqrAsignadaEvento;
import co.edu.uniquindio.epq.pqr.evento.PqrRegistradaEvento;
import co.edu.uniquindio.epq.pqr.repositorio.CiudadanoRepository;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;
import co.edu.uniquindio.epq.pqr.servicio.ResolutorCatalogosPqr;
import co.edu.uniquindio.epq.pqr.servicio.asignacion.EstrategiaAsignacionGestor;
import co.edu.uniquindio.epq.pqr.servicio.plazo.CalculadoraPlazoRespuesta;
import co.edu.uniquindio.epq.pqr.servicio.radicado.GeneradorRadicado;
import co.edu.uniquindio.epq.pqr.web.dto.PqrRegistradaResponse;
import co.edu.uniquindio.epq.pqr.web.dto.RegistrarPqrRequest;

@DisplayName("RegistroPqrServiceImpl - radicación de PQR")
class RegistroPqrServiceImplTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-15T15:00:00Z"), ZONA);
    private static final LocalDateTime AHORA = LocalDateTime.now(CLOCK);
    private static final String RADICADO = "202609-0007";
    private static final Long ID_PQR = 100L;
    private static final PlazoRespuesta PLAZO =
            new PlazoRespuesta(LocalDateTime.of(2026, 10, 6, 23, 59), LocalDate.of(2026, 10, 6));

    private final PqrRepository pqrRepository = mock(PqrRepository.class);
    private final CiudadanoRepository ciudadanoRepository = mock(CiudadanoRepository.class);
    private final ResolutorCatalogosPqr catalogos = mock(ResolutorCatalogosPqr.class);
    private final CalculadoraPlazoRespuesta calculadoraPlazo = mock(CalculadoraPlazoRespuesta.class);
    private final GeneradorRadicado generadorRadicado = mock(GeneradorRadicado.class);
    private final EstrategiaAsignacionGestor estrategiaAsignacion = mock(EstrategiaAsignacionGestor.class);
    private final ApplicationEventPublisher eventos = mock(ApplicationEventPublisher.class);

    private final Gestor laura = gestor(1, "Laura Giraldo", true);

    private RegistroPqrServiceImpl servicio;

    @BeforeEach
    void configurar() {
        servicio = new RegistroPqrServiceImpl(pqrRepository, ciudadanoRepository, catalogos, calculadoraPlazo,
                generadorRadicado, estrategiaAsignacion, eventos, CLOCK);

        when(catalogos.estado(any())).thenAnswer(inv -> DatosPruebaPqr.estado(inv.getArgument(0)));
        when(catalogos.tipoSolicitud(any())).thenAnswer(
                inv -> DatosPruebaPqr.tipoSolicitud(inv.<TipoSolicitudTipo>getArgument(0).getNombre()));
        when(catalogos.canal(any())).thenAnswer(
                inv -> DatosPruebaPqr.canal(inv.<CanalAtencionTipo>getArgument(0).getNombre()));
        when(calculadoraPlazo.calcular(any(), any())).thenReturn(PLAZO);
        when(generadorRadicado.generar(any())).thenReturn(RADICADO);

        // Por defecto: ciudadano nuevo, gestor disponible y la BD asigna el id al guardar.
        when(ciudadanoRepository.findByTipoDocumentoAndNumeroDocumento(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(ciudadanoRepository.save(any(Ciudadano.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pqrRepository.save(any(Pqr.class))).thenAnswer(inv -> {
            Pqr pqr = inv.getArgument(0);
            ReflectionTestUtils.setField(pqr, "id", ID_PQR);
            return pqr;
        });
        when(estrategiaAsignacion.seleccionarPara(any())).thenReturn(Optional.of(laura));
    }

    private static RegistrarPqrRequest solicitud(TipoSolicitudTipo tipo, CanalAtencionTipo canal,
                                                 String telefono, String direccion) {
        return new RegistrarPqrRequest("CC", " 1094000111 ", "  Ana Gómez  ", "  ana@correo.com  ",
                telefono, direccion, tipo, canal, "  Cobro elevado  ",
                "  El valor facturado duplica mi consumo habitual.  ");
    }

    private static RegistrarPqrRequest solicitudBasica() {
        return solicitud(TipoSolicitudTipo.RECLAMO, CanalAtencionTipo.WEB, "3001234567", "Cra 14 # 20-30");
    }

    private Pqr pqrGuardada() {
        ArgumentCaptor<Pqr> captor = ArgumentCaptor.forClass(Pqr.class);
        verify(pqrRepository).save(captor.capture());
        return captor.getValue();
    }

    private List<Object> eventosPublicados(int cantidad) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventos, times(cantidad)).publishEvent(captor.capture());
        return captor.getAllValues();
    }

    // ------------------------------------------------------------------ ciudadano

    @Nested
    @DisplayName("ciudadano")
    class CiudadanoSolicitante {

        @Test
        @DisplayName("Ciudadano nuevo: se registra con los datos recortados y la fecha actual")
        void registraCiudadanoNuevo() {
            servicio.registrar(solicitudBasica());

            ArgumentCaptor<Ciudadano> captor = ArgumentCaptor.forClass(Ciudadano.class);
            verify(ciudadanoRepository).save(captor.capture());
            Ciudadano nuevo = captor.getValue();
            assertAll(
                    () -> assertEquals("CC", nuevo.getTipoDocumento()),
                    () -> assertEquals("1094000111", nuevo.getNumeroDocumento()),
                    () -> assertEquals("Ana Gómez", nuevo.getNombreCompleto()),
                    () -> assertEquals("ana@correo.com", nuevo.getCorreo()),
                    () -> assertEquals("3001234567", nuevo.getTelefono()),
                    () -> assertEquals(AHORA, nuevo.getFechaRegistro()),
                    () -> assertSame(nuevo, pqrGuardada().getCiudadano()));
        }

        @Test
        @DisplayName("Ciudadano nuevo: teléfono y dirección en blanco se guardan como null")
        void limpiaCamposOpcionales() {
            servicio.registrar(solicitud(TipoSolicitudTipo.PETICION, null, "   ", ""));

            ArgumentCaptor<Ciudadano> captor = ArgumentCaptor.forClass(Ciudadano.class);
            verify(ciudadanoRepository).save(captor.capture());
            assertNull(captor.getValue().getTelefono());
            assertNull(captor.getValue().getDireccion());
        }

        @Test
        @DisplayName("Busca al ciudadano por tipo y número de documento sin espacios")
        void buscaPorDocumentoRecortado() {
            servicio.registrar(solicitudBasica());

            verify(ciudadanoRepository).findByTipoDocumentoAndNumeroDocumento("CC", "1094000111");
        }

        @Test
        @DisplayName("Ciudadano existente: no se crea otro, se actualiza su contacto y se conserva lo no enviado")
        void actualizaCiudadanoExistente() {
            Ciudadano existente = Ciudadano.registrar("CC", "1094000111", "Ana G.", "viejo@correo.com",
                    "3110000000", "Calle vieja", AHORA.minusYears(1));
            when(ciudadanoRepository.findByTipoDocumentoAndNumeroDocumento("CC", "1094000111"))
                    .thenReturn(Optional.of(existente));

            servicio.registrar(solicitud(TipoSolicitudTipo.QUEJA, null, "", "Cra 14 # 20-30"));

            verify(ciudadanoRepository, never()).save(any());
            assertAll(
                    () -> assertEquals("Ana Gómez", existente.getNombreCompleto()),
                    () -> assertEquals("ana@correo.com", existente.getCorreo()),
                    () -> assertEquals("3110000000", existente.getTelefono(), "teléfono vacío conserva el actual"),
                    () -> assertEquals("Cra 14 # 20-30", existente.getDireccion()),
                    () -> assertSame(existente, pqrGuardada().getCiudadano()));
        }
    }

    // ------------------------------------------------------------------ radicación

    @Nested
    @DisplayName("radicación")
    class Radicacion {

        @Test
        @DisplayName("Crea la PQR en Radicado con radicado, plazo, fecha y textos recortados")
        void creaPqrRadicada() {
            PqrRegistradaResponse respuesta = servicio.registrar(solicitudBasica());

            Pqr pqr = pqrGuardada();
            assertAll(
                    () -> assertEquals(RADICADO, pqr.getRadicado()),
                    () -> assertEquals(EstadoPqrTipo.RADICADO, pqr.getEstadoTipo()),
                    () -> assertEquals(AHORA, pqr.getFechaRecepcion()),
                    () -> assertEquals(PLAZO.fechaLimite(), pqr.getFechaLimiteRespuesta()),
                    () -> assertEquals(PLAZO.fechaEstimada(), pqr.getFechaEstimadaRespuesta()),
                    () -> assertEquals("Cobro elevado", pqr.getAsunto()),
                    () -> assertEquals("El valor facturado duplica mi consumo habitual.", pqr.getDescripcion()),
                    () -> assertEquals(RADICADO, respuesta.radicado()),
                    () -> assertEquals(EstadoPqrTipo.RADICADO.getNombre(), respuesta.estado()),
                    () -> assertTrue(respuesta.mensaje().contains(RADICADO)));
            verify(generadorRadicado).generar(AHORA);
        }

        @ParameterizedTest(name = "Tipo {0} → plazo calculado para ese tipo")
        @EnumSource(TipoSolicitudTipo.class)
        @DisplayName("Calcula el plazo según el tipo de solicitud y la fecha de recepción")
        void calculaPlazoPorTipo(TipoSolicitudTipo tipo) {
            PqrRegistradaResponse respuesta = servicio.registrar(solicitud(tipo, null, null, null));

            verify(calculadoraPlazo).calcular(tipo, AHORA);
            verify(catalogos).tipoSolicitud(tipo);
            assertEquals(tipo.getNombre(), respuesta.tipoSolicitud());
        }

        @Test
        @DisplayName("Sin canal: usa WEB por defecto")
        void canalPorDefectoWeb() {
            servicio.registrar(solicitud(TipoSolicitudTipo.RECLAMO, null, null, null));

            verify(catalogos).canal(CanalAtencionTipo.WEB);
            assertEquals(CanalAtencionTipo.WEB.getNombre(), pqrGuardada().getCanal().getNombre());
        }

        @Test
        @DisplayName("Con canal explícito: respeta el canal enviado")
        void canalExplicito() {
            servicio.registrar(solicitud(TipoSolicitudTipo.RECLAMO, CanalAtencionTipo.PRESENCIAL, null, null));

            verify(catalogos).canal(CanalAtencionTipo.PRESENCIAL);
        }
    }

    // ------------------------------------------------------------------ asignación y eventos

    @Nested
    @DisplayName("asignación y eventos")
    class AsignacionYEventos {

        @Test
        @DisplayName("Con gestor disponible: lo asigna y publica Registrada y luego Asignada")
        void asignaGestorYPublicaEventos() {
            PqrRegistradaResponse respuesta = servicio.registrar(solicitudBasica());

            assertSame(laura, pqrGuardada().getGestor());
            assertEquals(laura.getNombre(), respuesta.gestorAsignado());

            List<Object> publicados = eventosPublicados(2);
            PqrRegistradaEvento registrada = assertInstanceOf(PqrRegistradaEvento.class, publicados.get(0));
            PqrAsignadaEvento asignada = assertInstanceOf(PqrAsignadaEvento.class, publicados.get(1));
            assertAll(
                    () -> assertEquals(ID_PQR, registrada.pqrId()),
                    () -> assertEquals(RADICADO, registrada.radicado()),
                    () -> assertEquals("ciudadano:CC-1094000111", registrada.usuario()),
                    () -> assertEquals(ID_PQR, asignada.pqrId()),
                    () -> assertEquals(laura.getId(), asignada.gestorId()),
                    () -> assertEquals(RegistroPqrServiceImpl.USUARIO_SISTEMA, asignada.usuario()));
        }

        @Test
        @DisplayName("Sin gestor disponible: queda sin asignar y solo publica el evento de registro")
        void sinGestorDisponible() {
            when(estrategiaAsignacion.seleccionarPara(any())).thenReturn(Optional.empty());

            PqrRegistradaResponse respuesta = servicio.registrar(solicitudBasica());

            assertNull(pqrGuardada().getGestor());
            assertNull(respuesta.gestorAsignado());
            assertInstanceOf(PqrRegistradaEvento.class, eventosPublicados(1).get(0));
        }

        @Test
        @DisplayName("Guarda la PQR antes de publicar eventos y de pedir el gestor a la estrategia")
        void ordenDeOperaciones() {
            servicio.registrar(solicitudBasica());

            InOrder orden = inOrder(pqrRepository, eventos, estrategiaAsignacion);
            orden.verify(pqrRepository).save(any(Pqr.class));
            orden.verify(eventos).publishEvent(any(PqrRegistradaEvento.class));
            orden.verify(estrategiaAsignacion).seleccionarPara(any(Pqr.class));
            orden.verify(eventos).publishEvent(any(PqrAsignadaEvento.class));
        }
    }
}