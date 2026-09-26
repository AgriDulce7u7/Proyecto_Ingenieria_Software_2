package co.edu.uniquindio.epq.facturacion.servicio.impl;

import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.NUMERO_FACTURA;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.PERIODO;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.factura;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.lote;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoFactura;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.dominio.LoteFacturacion;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.LoteFacturacionRepository;
import co.edu.uniquindio.epq.facturacion.servicio.sincronizacion.ResultadoSincronizacionFactura;
import co.edu.uniquindio.epq.facturacion.servicio.sincronizacion.SincronizadorFacturaErp;
import co.edu.uniquindio.epq.facturacion.web.dto.ResultadoSincronizacionResponse;

@DisplayName("SincronizacionErpServiceImpl - orquestación de la sincronización con el ERP")
class SincronizacionErpServiceImplTest {

    private final LoteFacturacionRepository loteRepository = mock(LoteFacturacionRepository.class);
    private final FacturaRepository facturaRepository = mock(FacturaRepository.class);
    private final SincronizadorFacturaErp sincronizador = mock(SincronizadorFacturaErp.class);

    private final SincronizacionErpServiceImpl servicio =
            new SincronizacionErpServiceImpl(loteRepository, facturaRepository, sincronizador);

    private LoteFacturacion lote;

    @BeforeEach
    void configurar() {
        lote = lote();
        when(loteRepository.findByPeriodo(PERIODO)).thenReturn(Optional.of(lote));
    }

    private static ResultadoSincronizacionFactura exito(String numero) {
        return new ResultadoSincronizacionFactura(numero, true, 1, "Referencia ERP: ERP-1");
    }

    private static ResultadoSincronizacionFactura fallo(String numero) {
        return new ResultadoSincronizacionFactura(numero, false, 3, "ERP no disponible");
    }

    // ------------------------------------------------------------------ lote

    @Nested
    @DisplayName("sincronizarLote")
    class SincronizarLote {

        @Test
        @DisplayName("Solo procesa las facturas GENERADAS o con ERROR_SINCRONIZACION del lote")
        void consultaSoloPendientes() {
            when(facturaRepository.buscarIdsPorLoteYEstados(any(), any())).thenReturn(List.of());

            servicio.sincronizarLote("2026-08");

            verify(facturaRepository).buscarIdsPorLoteYEstados(lote.getId(),
                    EnumSet.of(EstadoFactura.GENERADA, EstadoFactura.ERROR_SINCRONIZACION));
        }

        @Test
        @DisplayName("Sin pendientes: devuelve ceros y no llama al sincronizador")
        void sinPendientes() {
            when(facturaRepository.buscarIdsPorLoteYEstados(any(), any())).thenReturn(List.of());

            ResultadoSincronizacionResponse resultado = servicio.sincronizarLote("2026-08");

            assertAll(
                    () -> assertEquals("2026-08", resultado.periodo()),
                    () -> assertEquals(0, resultado.facturasProcesadas()),
                    () -> assertEquals(0, resultado.exitosas()),
                    () -> assertEquals(0, resultado.fallidas()),
                    () -> assertTrue(resultado.facturasConError().isEmpty()));
            verify(sincronizador, never()).sincronizar(anyLong());
        }

        @Test
        @DisplayName("Cuenta éxitos y fallos; una excepción en una factura no detiene las demás")
        void resultadosMixtos() {
            when(facturaRepository.buscarIdsPorLoteYEstados(any(), any())).thenReturn(List.of(1L, 2L, 3L, 4L));
            when(sincronizador.sincronizar(1L)).thenReturn(exito("FAC-202608-000001"));
            when(sincronizador.sincronizar(2L)).thenReturn(fallo("FAC-202608-000002"));
            when(sincronizador.sincronizar(3L)).thenThrow(new IllegalStateException("Error inesperado"));
            when(sincronizador.sincronizar(4L)).thenReturn(exito("FAC-202608-000004"));

            ResultadoSincronizacionResponse resultado = servicio.sincronizarLote("2026-08");

            assertAll(
                    () -> assertEquals(4, resultado.facturasProcesadas()),
                    () -> assertEquals(2, resultado.exitosas()),
                    () -> assertEquals(2, resultado.fallidas()),
                    () -> assertEquals(List.of("FAC-202608-000002", "id:3"), resultado.facturasConError()));
            verify(sincronizador).sincronizar(4L);
        }

        @Test
        @DisplayName("Lote inexistente: lanza RecursoNoEncontradoException")
        void loteInexistente() {
            assertThrows(RecursoNoEncontradoException.class, () -> servicio.sincronizarLote("1999-01"));
            verify(sincronizador, never()).sincronizar(anyLong());
        }

        @Test
        @DisplayName("Periodo mal formado: lanza IllegalArgumentException (400) sin consultar la BD")
        void periodoInvalido() {
            assertThrows(IllegalArgumentException.class, () -> servicio.sincronizarLote("enero"));
            verify(loteRepository, never()).findByPeriodo(any());
        }
    }

    // ------------------------------------------------------------------ factura puntual

    @Nested
    @DisplayName("sincronizarFactura")
    class SincronizarFactura {

        @Test
        @DisplayName("Busca la factura por número y delega en el sincronizador con su id")
        void delegaEnElSincronizador() {
            Factura factura = factura();
            ResultadoSincronizacionFactura esperado = exito(NUMERO_FACTURA);
            when(facturaRepository.findByNumeroFactura(NUMERO_FACTURA)).thenReturn(Optional.of(factura));
            when(sincronizador.sincronizar(factura.getId())).thenReturn(esperado);

            assertSame(esperado, servicio.sincronizarFactura(NUMERO_FACTURA));
        }

        @Test
        @DisplayName("Factura inexistente: lanza RecursoNoEncontradoException")
        void facturaInexistente() {
            when(facturaRepository.findByNumeroFactura("FAC-199901-000001")).thenReturn(Optional.empty());

            assertThrows(RecursoNoEncontradoException.class, () -> servicio.sincronizarFactura("FAC-199901-000001"));
            verify(sincronizador, never()).sincronizar(anyLong());
        }
    }
}