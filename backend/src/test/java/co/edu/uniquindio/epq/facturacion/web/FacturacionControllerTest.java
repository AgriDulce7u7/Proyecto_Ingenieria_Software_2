package co.edu.uniquindio.epq.facturacion.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoContrato;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoFactura;
import co.edu.uniquindio.epq.facturacion.servicio.ConsultaFacturacionService;
import co.edu.uniquindio.epq.facturacion.servicio.GeneracionFacturacionService;
import co.edu.uniquindio.epq.facturacion.servicio.SincronizacionErpService;
import co.edu.uniquindio.epq.facturacion.servicio.sincronizacion.ResultadoSincronizacionFactura;
import co.edu.uniquindio.epq.facturacion.web.dto.ContratoResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaDetalleResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaResumenResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.IncidenciaFacturacionResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.LoteFacturacionResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.ProgramacionFacturacionResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.ResultadoGeneracionLoteResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.ResultadoSincronizacionResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.SincronizacionErpResponse;

/**
 * Pruebas de la capa HTTP de facturación: rutas, parámetros, validación y códigos de error.
 */
@WebMvcTest(FacturacionController.class)
@DisplayName("FacturacionController - API REST de facturación")
class FacturacionControllerTest {

    private static final String BASE = "/api/facturacion";
    private static final String PERIODO = "2026-08";
    private static final String NUMERO_FACTURA = "FAC-202608-000001";
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 9, 1, 1, 0, 5);

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GeneracionFacturacionService generacionService;
    @MockitoBean
    private SincronizacionErpService sincronizacionService;
    @MockitoBean
    private ConsultaFacturacionService consultaService;

    private static void esProblema(ResultActions resultado, int estado, String titulo) throws Exception {
        resultado.andExpect(status().is(estado))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(estado))
                .andExpect(jsonPath("$.title").value(titulo));
    }

    private static ResultadoGeneracionLoteResponse resultadoLote() {
        return new ResultadoGeneracionLoteResponse(PERIODO, GeneracionFacturacionService.ORIGEN_MANUAL, "COMPLETADO",
                INICIO, INICIO.plusSeconds(2), 2000, true, 7, 2, 6, 0, 6,
                List.of(new IncidenciaFacturacionResponse("CT-ACU-0005", "SIN_LECTURA", "No hay lectura")),
                new ResultadoSincronizacionResponse(PERIODO, 6, 6, 0, List.of()));
    }

    private static LoteFacturacionResponse lote() {
        return new LoteFacturacionResponse(1, PERIODO, "COMPLETADO", INICIO, INICIO.plusSeconds(2), 2L, 6, 6, 0, 0,
                new BigDecimal("450000.00"));
    }

    private static FacturaResumenResponse resumen() {
        return new FacturaResumenResponse(NUMERO_FACTURA, PERIODO, "CT-ACU-0001", "Carlos Mario Restrepo",
                "Acueducto", new BigDecimal("21.50"), new BigDecimal("81783.25"), LocalDate.of(2026, 9, 16),
                "SINCRONIZADA");
    }

    // ------------------------------------------------------------------ lotes

    @Nested
    @DisplayName("lotes")
    class Lotes {

        @Test
        @DisplayName("GET /programacion: 200 con el estado de la programación automática")
        void programacion() throws Exception {
            when(consultaService.obtenerProgramacion()).thenReturn(new ProgramacionFacturacionResponse(true,
                    LocalDate.of(2026, 9, 26), false, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 1),
                    PERIODO, "COMPLETADO", 60, 15));

            mvc.perform(get(BASE + "/programacion"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.periodoPendienteDeFacturar").value(PERIODO))
                    .andExpect(jsonPath("$.proximaEjecucion").value("2026-10-01"));
        }

        @Test
        @DisplayName("POST /lotes sin cuerpo: factura el periodo por defecto (null) con origen MANUAL")
        void generarLoteSinCuerpo() throws Exception {
            when(generacionService.generarLote(any(), anyString())).thenReturn(resultadoLote());

            mvc.perform(post(BASE + "/lotes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalFacturasDelLote").value(6))
                    .andExpect(jsonPath("$.incidencias[0].numeroContrato").value("CT-ACU-0005"))
                    .andExpect(jsonPath("$.sincronizacionErp.exitosas").value(6));

            verify(generacionService).generarLote(null, GeneracionFacturacionService.ORIGEN_MANUAL);
        }

        @Test
        @DisplayName("POST /lotes con cuerpo vacío {}: también usa el periodo por defecto")
        void generarLoteCuerpoVacio() throws Exception {
            when(generacionService.generarLote(any(), anyString())).thenReturn(resultadoLote());

            mvc.perform(post(BASE + "/lotes").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk());

            verify(generacionService).generarLote(null, GeneracionFacturacionService.ORIGEN_MANUAL);
        }

        @Test
        @DisplayName("POST /lotes con periodo: lo convierte a YearMonth")
        void generarLoteConPeriodo() throws Exception {
            when(generacionService.generarLote(any(), anyString())).thenReturn(resultadoLote());

            mvc.perform(post(BASE + "/lotes").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"periodo\": \"2026-08\"}"))
                    .andExpect(status().isOk());

            verify(generacionService).generarLote(YearMonth.of(2026, 8), GeneracionFacturacionService.ORIGEN_MANUAL);
        }

        @Test
        @DisplayName("POST /lotes con periodo mal formado: 400 y no genera nada")
        void generarLotePeriodoInvalido() throws Exception {
            ResultActions resultado = mvc.perform(post(BASE + "/lotes").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"periodo\": \"2026-13\"}"));

            esProblema(resultado, 400, "Datos inválidos");
            resultado.andExpect(jsonPath("$.errores.periodo").value("El periodo debe tener el formato YYYY-MM"));
            verify(generacionService, never()).generarLote(any(), anyString());
        }

        @Test
        @DisplayName("POST /lotes con periodo futuro: 422 por regla de negocio")
        void generarLotePeriodoFuturo() throws Exception {
            when(generacionService.generarLote(any(), anyString()))
                    .thenThrow(new ReglaNegocioException("No se puede facturar un periodo futuro"));

            esProblema(mvc.perform(post(BASE + "/lotes").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"periodo\": \"2030-01\"}")), 422, "Regla de negocio incumplida");
        }

        @Test
        @DisplayName("GET /lotes y /lotes/{periodo}: 200 con historial y detalle")
        void consultarLotes() throws Exception {
            when(consultaService.listarLotes()).thenReturn(List.of(lote()));
            when(consultaService.obtenerLote(PERIODO)).thenReturn(lote());

            mvc.perform(get(BASE + "/lotes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
            mvc.perform(get(BASE + "/lotes/{periodo}", PERIODO))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalFacturasGeneradas").value(6))
                    .andExpect(jsonPath("$.valorTotalFacturado").value(450000.00));
        }

        @Test
        @DisplayName("GET /lotes/{periodo}: 404 si no existe y 400 si el periodo es inválido")
        void loteInexistenteOInvalido() throws Exception {
            when(consultaService.obtenerLote("1999-01"))
                    .thenThrow(new RecursoNoEncontradoException("Lote de facturación", "1999-01"));
            when(consultaService.obtenerLote("enero"))
                    .thenThrow(new IllegalArgumentException("Periodo inválido 'enero'. Use el formato YYYY-MM"));

            esProblema(mvc.perform(get(BASE + "/lotes/{periodo}", "1999-01")), 404, "Recurso no encontrado");
            esProblema(mvc.perform(get(BASE + "/lotes/{periodo}", "enero")), 400, "Solicitud inválida");
        }

        @Test
        @DisplayName("GET /lotes/{periodo}/facturas y POST /lotes/{periodo}/sincronizacion: 200")
        void facturasYSincronizacionDelLote() throws Exception {
            when(consultaService.listarFacturasDelLote(PERIODO)).thenReturn(List.of(resumen()));
            when(sincronizacionService.sincronizarLote(PERIODO))
                    .thenReturn(new ResultadoSincronizacionResponse(PERIODO, 1, 1, 0, List.of()));

            mvc.perform(get(BASE + "/lotes/{periodo}/facturas", PERIODO))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].numeroFactura").value(NUMERO_FACTURA));
            mvc.perform(post(BASE + "/lotes/{periodo}/sincronizacion", PERIODO))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exitosas").value(1));
        }
    }

    // ------------------------------------------------------------------ facturas

    @Nested
    @DisplayName("facturas")
    class Facturas {

        @Test
        @DisplayName("GET /facturas con filtros: los pasa al servicio con el enum convertido")
        void buscarConFiltros() throws Exception {
            when(consultaService.buscarFacturas(any(), any(), any(), any())).thenReturn(List.of(resumen()));

            mvc.perform(get(BASE + "/facturas")
                            .param("periodo", PERIODO).param("contrato", "CT-ACU-0001")
                            .param("estado", "SINCRONIZADA").param("documento", "1094950001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].total").value(81783.25));

            verify(consultaService).buscarFacturas(PERIODO, "CT-ACU-0001", EstadoFactura.SINCRONIZADA, "1094950001");
        }

        @Test
        @DisplayName("GET /facturas con estado inexistente: 400")
        void buscarConEstadoInvalido() throws Exception {
            esProblema(mvc.perform(get(BASE + "/facturas").param("estado", "ANULADA")), 400, "Solicitud inválida");
            verify(consultaService, never()).buscarFacturas(any(), any(), any(), any());
        }

        @Test
        @DisplayName("GET /facturas/{numero}: 200 con la liquidación completa")
        void obtenerFactura() throws Exception {
            when(consultaService.obtenerFactura(NUMERO_FACTURA)).thenReturn(new FacturaDetalleResponse(
                    NUMERO_FACTURA, PERIODO, "SINCRONIZADA", INICIO, LocalDate.of(2026, 9, 16), null, "MED-CT-ACU-0001",
                    new BigDecimal("1538.00"), new BigDecimal("1559.50"), new BigDecimal("21.50"),
                    "Acueducto residencial 2026", new BigDecimal("3215.50"), new BigDecimal("69133.25"),
                    new BigDecimal("12650.00"), new BigDecimal("81783.25"), new BigDecimal("81783.25")));

            mvc.perform(get(BASE + "/facturas/{numero}", NUMERO_FACTURA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.consumo").value(21.50))
                    .andExpect(jsonPath("$.total").value(81783.25))
                    .andExpect(jsonPath("$.fechaVencimiento").value("2026-09-16"));
        }

        @Test
        @DisplayName("GET /facturas/{numero}: 404 si no existe")
        void facturaInexistente() throws Exception {
            when(consultaService.obtenerFactura("FAC-199901-000001"))
                    .thenThrow(new RecursoNoEncontradoException("Factura", "FAC-199901-000001"));

            esProblema(mvc.perform(get(BASE + "/facturas/{numero}", "FAC-199901-000001")), 404, "Recurso no encontrado");
        }

        @Test
        @DisplayName("POST /facturas/{numero}/sincronizacion: 200 si sincroniza, 422 si ya estaba sincronizada")
        void sincronizarFactura() throws Exception {
            when(sincronizacionService.sincronizarFactura(NUMERO_FACTURA))
                    .thenReturn(new ResultadoSincronizacionFactura(NUMERO_FACTURA, true, 1, "Referencia ERP: ERP-1"));
            when(sincronizacionService.sincronizarFactura("FAC-202608-000002"))
                    .thenThrow(new ReglaNegocioException("La factura FAC-202608-000002 ya está en estado 'sincronizada'"));

            mvc.perform(post(BASE + "/facturas/{numero}/sincronizacion", NUMERO_FACTURA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exitosa").value(true))
                    .andExpect(jsonPath("$.intentos").value(1));
            esProblema(mvc.perform(post(BASE + "/facturas/{numero}/sincronizacion", "FAC-202608-000002")),
                    422, "Regla de negocio incumplida");
        }

        @Test
        @DisplayName("GET /facturas/{numero}/sincronizaciones: 200 con la bitácora")
        void bitacora() throws Exception {
            when(consultaService.obtenerSincronizaciones(NUMERO_FACTURA)).thenReturn(List.of(
                    new SincronizacionErpResponse(1L, INICIO, "EXITOSO", 1, "Referencia ERP: ERP-1")));

            mvc.perform(get(BASE + "/facturas/{numero}/sincronizaciones", NUMERO_FACTURA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].estado").value("EXITOSO"));
        }
    }

    // ------------------------------------------------------------------ contratos

    @Nested
    @DisplayName("contratos")
    class Contratos {

        private final ContratoResponse contrato = new ContratoResponse(1, "CT-ACU-0001", "Acueducto",
                "Cra 14 # 20-35, Armenia", LocalDate.of(2022, 3, 10), "ACTIVO", "CC", "1094950001",
                "Carlos Mario Restrepo");

        @Test
        @DisplayName("GET /contratos?estado=ACTIVO: filtra por el enum")
        void contratosActivos() throws Exception {
            when(consultaService.listarContratos(EstadoContrato.ACTIVO)).thenReturn(List.of(contrato));

            mvc.perform(get(BASE + "/contratos").param("estado", "ACTIVO"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].numeroContrato").value("CT-ACU-0001"));
        }

        @Test
        @DisplayName("GET /contratos sin estado: pasa null al servicio")
        void todosLosContratos() throws Exception {
            when(consultaService.listarContratos(null)).thenReturn(List.of(contrato));

            mvc.perform(get(BASE + "/contratos")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

            verify(consultaService).listarContratos(null);
        }

        @Test
        @DisplayName("GET /contratos con estado inexistente: 400")
        void estadoInvalido() throws Exception {
            esProblema(mvc.perform(get(BASE + "/contratos").param("estado", "CANCELADO")), 400, "Solicitud inválida");
        }
    }
}