package co.edu.uniquindio.epq.pqr.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.comun.excepcion.TransicionEstadoInvalidaException;
import co.edu.uniquindio.epq.pqr.dominio.CanalAtencionTipo;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitudTipo;
import co.edu.uniquindio.epq.pqr.servicio.ConsultaPqrService;
import co.edu.uniquindio.epq.pqr.servicio.FiltroPqr;
import co.edu.uniquindio.epq.pqr.servicio.GestionPqrService;
import co.edu.uniquindio.epq.pqr.servicio.MonitoreoPlazosPqrService;
import co.edu.uniquindio.epq.pqr.servicio.RegistroPqrService;
import co.edu.uniquindio.epq.pqr.web.dto.CerrarPqrRequest;
import co.edu.uniquindio.epq.pqr.web.dto.ConsultaEstadoPqrResponse;
import co.edu.uniquindio.epq.pqr.web.dto.HistorialPqrResponse;
import co.edu.uniquindio.epq.pqr.web.dto.PqrDetalleResponse;
import co.edu.uniquindio.epq.pqr.web.dto.PqrRegistradaResponse;
import co.edu.uniquindio.epq.pqr.web.dto.PqrResumenResponse;
import co.edu.uniquindio.epq.pqr.web.dto.ReasignarPqrRequest;
import co.edu.uniquindio.epq.pqr.web.dto.RegistrarPqrRequest;
import co.edu.uniquindio.epq.pqr.web.dto.ResponderPqrRequest;
import co.edu.uniquindio.epq.pqr.web.dto.ResultadoMonitoreoResponse;
import co.edu.uniquindio.epq.pqr.web.dto.TomarPqrRequest;

/**
 * Pruebas de la capa HTTP: rutas, códigos de estado, validación de DTOs, serialización JSON y
 * traducción de excepciones a ProblemDetail (RFC 7807). Los servicios se reemplazan por mocks.
 */
@WebMvcTest(PqrController.class)
@DisplayName("PqrController - API REST de PQR")
class PqrControllerTest {

    private static final String RADICADO = "202609-0001";
    private static final LocalDateTime RECEPCION = LocalDateTime.of(2026, 9, 1, 8, 0, 15);
    private static final LocalDateTime LIMITE = LocalDateTime.of(2026, 9, 22, 23, 59);

    private static final String PQR_VALIDA = """
            {
              "tipoDocumento": "CC", "numeroDocumento": "1094000111", "nombreCompleto": "Ana Gómez",
              "correo": "ana@correo.com", "telefono": "3001234567", "direccion": "Cra 14 # 20-30, Armenia",
              "tipoSolicitud": "RECLAMO", "canal": "WEB",
              "asunto": "Cobro elevado en la factura",
              "descripcion": "El valor facturado este mes duplica mi consumo habitual."
            }
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RegistroPqrService registroService;
    @MockitoBean
    private ConsultaPqrService consultaService;
    @MockitoBean
    private GestionPqrService gestionService;
    @MockitoBean
    private MonitoreoPlazosPqrService monitoreoService;

    private ResultActions enviarJson(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder peticion,
                                     String json) throws Exception {
        return mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(json));
    }

    /** Verifica el formato estándar de error del ManejadorGlobalExcepciones. */
    private static void esProblema(ResultActions resultado, int estado, String titulo) throws Exception {
        resultado.andExpect(status().is(estado))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(estado))
                .andExpect(jsonPath("$.title").value(titulo))
                .andExpect(jsonPath("$.detail").isString());
    }

    private static PqrDetalleResponse detalle(String estado, List<String> acciones) {
        return new PqrDetalleResponse(100L, RADICADO, "Reclamo", "Web", estado, "Cobro elevado",
                "Descripción del reclamo", null, null, RECEPCION, LIMITE, LIMITE.toLocalDate(), null, null,
                false, acciones);
    }

    // ------------------------------------------------------------------ POST /api/pqr

    @Nested
    @DisplayName("POST /api/pqr")
    class Registrar {

        @Test
        @DisplayName("201 Created con el radicado y los datos convertidos al DTO")
        void registraPqr() throws Exception {
            when(registroService.registrar(any())).thenReturn(new PqrRegistradaResponse(RADICADO, "Reclamo",
                    "Radicado", RECEPCION, LIMITE, LIMITE.toLocalDate(), "Laura Giraldo", "Mensaje " + RADICADO));

            enviarJson(post("/api/pqr"), PQR_VALIDA)
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.radicado").value(RADICADO))
                    .andExpect(jsonPath("$.estado").value("Radicado"))
                    .andExpect(jsonPath("$.gestorAsignado").value("Laura Giraldo"))
                    .andExpect(jsonPath("$.fechaRecepcion").value("2026-09-01T08:00:15"))
                    .andExpect(jsonPath("$.fechaEstimadaRespuesta").value("2026-09-22"));

            ArgumentCaptor<RegistrarPqrRequest> captor = ArgumentCaptor.forClass(RegistrarPqrRequest.class);
            verify(registroService).registrar(captor.capture());
            assertAll(
                    () -> assertEquals(TipoSolicitudTipo.RECLAMO, captor.getValue().tipoSolicitud()),
                    () -> assertEquals(CanalAtencionTipo.WEB, captor.getValue().canal()),
                    () -> assertEquals("ana@correo.com", captor.getValue().correo()));
        }

        @Test
        @DisplayName("400 con todos los campos obligatorios reportados y sin llamar al servicio")
        void camposObligatorios() throws Exception {
            ResultActions resultado = enviarJson(post("/api/pqr"), "{}");

            esProblema(resultado, 400, "Datos inválidos");
            resultado.andExpect(jsonPath("$.errores.tipoDocumento").exists())
                    .andExpect(jsonPath("$.errores.numeroDocumento").exists())
                    .andExpect(jsonPath("$.errores.nombreCompleto").exists())
                    .andExpect(jsonPath("$.errores.correo").exists())
                    .andExpect(jsonPath("$.errores.tipoSolicitud").exists())
                    .andExpect(jsonPath("$.errores.asunto").exists())
                    .andExpect(jsonPath("$.errores.descripcion").exists());
            verify(registroService, never()).registrar(any());
        }

        @Test
        @DisplayName("400 con mensajes claros para tipo de documento, correo, teléfono y descripción inválidos")
        void formatosInvalidos() throws Exception {
            String json = PQR_VALIDA
                    .replace("\"CC\"", "\"XX\"")
                    .replace("ana@correo.com", "no-es-correo")
                    .replace("3001234567", "abc")
                    .replace("El valor facturado este mes duplica mi consumo habitual.", "Corta");

            ResultActions resultado = enviarJson(post("/api/pqr"), json);

            esProblema(resultado, 400, "Datos inválidos");
            resultado.andExpect(jsonPath("$.errores.tipoDocumento").value("Tipo de documento válido: CC, CE, TI, NIT o PA"))
                    .andExpect(jsonPath("$.errores.correo").value("El correo electrónico no es válido"))
                    .andExpect(jsonPath("$.errores.telefono").value("El teléfono debe tener entre 7 y 20 dígitos"))
                    .andExpect(jsonPath("$.errores.descripcion").value("La descripción debe tener entre 10 y 5000 caracteres"));
            verify(registroService, never()).registrar(any());
        }

        @Test
        @DisplayName("400 'Solicitud inválida' si el tipo de solicitud no existe en el enum")
        void tipoSolicitudInexistente() throws Exception {
            esProblema(enviarJson(post("/api/pqr"), PQR_VALIDA.replace("RECLAMO", "SUGERENCIA")),
                    400, "Solicitud inválida");
        }

        @Test
        @DisplayName("400 'Solicitud inválida' si el JSON está mal formado")
        void jsonMalFormado() throws Exception {
            esProblema(enviarJson(post("/api/pqr"), "{ \"tipoDocumento\": "), 400, "Solicitud inválida");
        }
    }

    // ------------------------------------------------------------------ consultas

    @Nested
    @DisplayName("consultas")
    class Consultas {

        @Test
        @DisplayName("GET /consulta/{radicado}: 200 con el estado público")
        void consultaPublica() throws Exception {
            when(consultaService.consultarEstadoPublico(RADICADO)).thenReturn(new ConsultaEstadoPqrResponse(
                    RADICADO, "Reclamo", "En trámite", RECEPCION, LocalDate.of(2026, 9, 22), null));

            mvc.perform(get("/api/pqr/consulta/{radicado}", RADICADO))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.radicado").value(RADICADO))
                    .andExpect(jsonPath("$.estado").value("En trámite"))
                    .andExpect(jsonPath("$.ciudadano").doesNotExist());
        }

        @Test
        @DisplayName("GET /consulta/{radicado}: 404 si el radicado no existe")
        void consultaPublicaInexistente() throws Exception {
            when(consultaService.consultarEstadoPublico("199901-9999"))
                    .thenThrow(new RecursoNoEncontradoException("PQR con radicado", "199901-9999"));

            ResultActions resultado = mvc.perform(get("/api/pqr/consulta/{radicado}", "199901-9999"));

            esProblema(resultado, 404, "Recurso no encontrado");
            resultado.andExpect(jsonPath("$.detail").value(containsString("199901-9999")));
        }

        @Test
        @DisplayName("GET /api/pqr con filtros: los convierte a FiltroPqr")
        void listarConFiltros() throws Exception {
            when(consultaService.listar(any())).thenReturn(List.of(new PqrResumenResponse(RADICADO, "Queja",
                    "Radicado", "Asunto", "Ana Gómez", "Laura Giraldo", RECEPCION, LIMITE, false)));

            mvc.perform(get("/api/pqr")
                            .param("estado", "RADICADO").param("tipo", "QUEJA")
                            .param("gestorId", "2").param("documento", "1094000111"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].radicado").value(RADICADO));

            verify(consultaService).listar(new FiltroPqr(EstadoPqrTipo.RADICADO, TipoSolicitudTipo.QUEJA, 2, "1094000111"));
        }

        @Test
        @DisplayName("GET /api/pqr sin filtros: FiltroPqr con todos los campos en null")
        void listarSinFiltros() throws Exception {
            when(consultaService.listar(any())).thenReturn(List.of());

            mvc.perform(get("/api/pqr")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));

            ArgumentCaptor<FiltroPqr> captor = ArgumentCaptor.forClass(FiltroPqr.class);
            verify(consultaService).listar(captor.capture());
            assertNull(captor.getValue().estado());
            assertNull(captor.getValue().gestorId());
        }

        @Test
        @DisplayName("GET /api/pqr con estado inexistente o gestorId no numérico: 400")
        void filtrosInvalidos() throws Exception {
            esProblema(mvc.perform(get("/api/pqr").param("estado", "PENDIENTE")), 400, "Solicitud inválida");
            esProblema(mvc.perform(get("/api/pqr").param("gestorId", "abc")), 400, "Solicitud inválida");
            verify(consultaService, never()).listar(any());
        }

        @Test
        @DisplayName("GET /{radicado}: 200 con el detalle y las acciones disponibles")
        void obtenerDetalle() throws Exception {
            when(consultaService.obtenerDetalle(RADICADO)).thenReturn(detalle("Radicado", List.of("EN_TRAMITE")));

            mvc.perform(get("/api/pqr/{radicado}", RADICADO))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.radicado").value(RADICADO))
                    .andExpect(jsonPath("$.accionesDisponibles[0]").value("EN_TRAMITE"));
        }

        @Test
        @DisplayName("GET /{radicado}/historial y /notificaciones: 200 con listas")
        void historialYNotificaciones() throws Exception {
            when(consultaService.obtenerHistorial(RADICADO)).thenReturn(List.of(
                    new HistorialPqrResponse(1L, "Radicado", RECEPCION, "Radicación", "SISTEMA")));
            when(consultaService.obtenerNotificaciones(RADICADO)).thenReturn(List.of());

            mvc.perform(get("/api/pqr/{radicado}/historial", RADICADO))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].estado").value("Radicado"));
            mvc.perform(get("/api/pqr/{radicado}/notificaciones", RADICADO))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // ------------------------------------------------------------------ gestión

    @Nested
    @DisplayName("gestión del ciclo de vida")
    class Gestion {

        @Test
        @DisplayName("PATCH /tomar: 200 y pasa el gestorId al servicio")
        void tomar() throws Exception {
            when(gestionService.tomar(eq(RADICADO), any())).thenReturn(detalle("En trámite", List.of("RESUELTO")));

            enviarJson(patch("/api/pqr/{radicado}/tomar", RADICADO), "{\"gestorId\": 2}")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("En trámite"));

            verify(gestionService).tomar(RADICADO, new TomarPqrRequest(2));
        }

        @Test
        @DisplayName("PATCH /tomar sin gestorId: 400 y no llama al servicio")
        void tomarSinGestor() throws Exception {
            ResultActions resultado = enviarJson(patch("/api/pqr/{radicado}/tomar", RADICADO), "{}");

            esProblema(resultado, 400, "Datos inválidos");
            resultado.andExpect(jsonPath("$.errores.gestorId").value("El gestor es obligatorio"));
            verify(gestionService, never()).tomar(any(), any());
        }

        @Test
        @DisplayName("PATCH /tomar con transición inválida: 409")
        void tomarTransicionInvalida() throws Exception {
            when(gestionService.tomar(eq(RADICADO), any()))
                    .thenThrow(new TransicionEstadoInvalidaException("Cerrado", "En trámite"));

            esProblema(enviarJson(patch("/api/pqr/{radicado}/tomar", RADICADO), "{\"gestorId\": 1}"),
                    409, "Transición de estado inválida");
        }

        @Test
        @DisplayName("PATCH /reasignar con regla de negocio incumplida: 422")
        void reasignarReglaNegocio() throws Exception {
            when(gestionService.reasignar(eq(RADICADO), any()))
                    .thenThrow(new ReglaNegocioException("La PQR ya está asignada al gestor Laura Giraldo"));

            ResultActions resultado = enviarJson(patch("/api/pqr/{radicado}/reasignar", RADICADO),
                    "{\"gestorId\": 1, \"usuario\": \"coordinador\", \"motivo\": \"Balanceo\"}");

            esProblema(resultado, 422, "Regla de negocio incumplida");
            resultado.andExpect(jsonPath("$.detail").value("La PQR ya está asignada al gestor Laura Giraldo"));
            verify(gestionService).reasignar(RADICADO, new ReasignarPqrRequest(1, "coordinador", "Balanceo"));
        }

        @Test
        @DisplayName("PATCH /reasignar sin usuario: 400")
        void reasignarSinUsuario() throws Exception {
            esProblema(enviarJson(patch("/api/pqr/{radicado}/reasignar", RADICADO), "{\"gestorId\": 1}"),
                    400, "Datos inválidos");
        }

        @Test
        @DisplayName("PATCH /responder: 200 con respuesta válida")
        void responder() throws Exception {
            String respuesta = "Se revisó la lectura y se ajustará la factura.";
            when(gestionService.responder(eq(RADICADO), any())).thenReturn(detalle("Resuelto", List.of("CERRADO")));

            enviarJson(patch("/api/pqr/{radicado}/responder", RADICADO),
                    "{\"gestorId\": 1, \"respuesta\": \"" + respuesta + "\"}")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("Resuelto"));

            verify(gestionService).responder(RADICADO, new ResponderPqrRequest(1, respuesta));
        }

        @Test
        @DisplayName("PATCH /responder con respuesta corta: 400 (RN-06)")
        void responderCorta() throws Exception {
            ResultActions resultado = enviarJson(patch("/api/pqr/{radicado}/responder", RADICADO),
                    "{\"gestorId\": 1, \"respuesta\": \"Ok\"}");

            esProblema(resultado, 400, "Datos inválidos");
            resultado.andExpect(jsonPath("$.errores.respuesta").exists());
            verify(gestionService, never()).responder(any(), any());
        }

        @Test
        @DisplayName("PATCH /cerrar: 200 y la observación es opcional")
        void cerrar() throws Exception {
            when(gestionService.cerrar(eq(RADICADO), any())).thenReturn(detalle("Cerrado", List.of()));

            enviarJson(patch("/api/pqr/{radicado}/cerrar", RADICADO), "{\"usuario\": \"coordinador\"}")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accionesDisponibles", hasSize(0)));

            verify(gestionService).cerrar(RADICADO, new CerrarPqrRequest("coordinador", null));
        }

        @Test
        @DisplayName("POST /monitoreo: 200 con los contadores")
        void monitoreo() throws Exception {
            when(monitoreoService.ejecutarMonitoreo()).thenReturn(new ResultadoMonitoreoResponse(RECEPCION, 2, 1));

            mvc.perform(post("/api/pqr/monitoreo"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.alertasEnviadas").value(2))
                    .andExpect(jsonPath("$.pqrMarcadasVencidas").value(1));
        }
    }

    // ------------------------------------------------------------------ errores genéricos

    @Nested
    @DisplayName("manejo global de errores")
    class ErroresGenericos {

        @Test
        @DisplayName("Conflicto de integridad en la BD: 409 sin exponer detalles técnicos")
        void conflictoDeDatos() throws Exception {
            when(registroService.registrar(any()))
                    .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

            ResultActions resultado = enviarJson(post("/api/pqr"), PQR_VALIDA);

            esProblema(resultado, 409, "Conflicto de datos");
            resultado.andExpect(jsonPath("$.detail").value(not(containsString("duplicate key"))));
        }

        @Test
        @DisplayName("Error inesperado: 500 con mensaje genérico, sin filtrar la causa")
        void errorInesperado() throws Exception {
            when(consultaService.obtenerDetalle(RADICADO)).thenThrow(new IllegalStateException("NullPointer interno"));

            ResultActions resultado = mvc.perform(get("/api/pqr/{radicado}", RADICADO));

            esProblema(resultado, 500, "Error interno");
            resultado.andExpect(jsonPath("$.detail").value(not(containsString("NullPointer"))));
        }
    }
}