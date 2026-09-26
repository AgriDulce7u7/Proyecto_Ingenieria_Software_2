package co.edu.uniquindio.epq.facturacion.web;

import java.time.YearMonth;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.epq.facturacion.dominio.EstadoContrato;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoFactura;
import co.edu.uniquindio.epq.facturacion.servicio.ConsultaFacturacionService;
import co.edu.uniquindio.epq.facturacion.servicio.GeneracionFacturacionService;
import co.edu.uniquindio.epq.facturacion.servicio.PeriodoFacturacion;
import co.edu.uniquindio.epq.facturacion.servicio.SincronizacionErpService;
import co.edu.uniquindio.epq.facturacion.servicio.sincronizacion.ResultadoSincronizacionFactura;
import co.edu.uniquindio.epq.facturacion.web.dto.ContratoResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaDetalleResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaResumenResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.GenerarLoteRequest;
import co.edu.uniquindio.epq.facturacion.web.dto.LoteFacturacionResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.ProgramacionFacturacionResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.ResultadoGeneracionLoteResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.ResultadoSincronizacionResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.SincronizacionErpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * API REST de facturación mensual (F-01). Sin lógica de negocio: delega en los servicios.
 */
@RestController
@RequestMapping("/api/facturacion")
@Tag(name = "F-01 Facturación", description = "Generación de facturas mensuales e integración con el ERP")
public class FacturacionController {

    private final GeneracionFacturacionService generacionService;
    private final SincronizacionErpService sincronizacionService;
    private final ConsultaFacturacionService consultaService;

    public FacturacionController(GeneracionFacturacionService generacionService,
                                 SincronizacionErpService sincronizacionService,
                                 ConsultaFacturacionService consultaService) {
        this.generacionService = generacionService;
        this.sincronizacionService = sincronizacionService;
        this.consultaService = consultaService;
    }

    // ------------------------------------------------------------------ Lotes

    @GetMapping("/programacion")
    @Operation(summary = "Estado de la programación automática (primer día hábil del mes)", description = "SWR-01")
    public ProgramacionFacturacionResponse obtenerProgramacion() {
        return consultaService.obtenerProgramacion();
    }

    @PostMapping("/lotes")
    @Operation(summary = "Generar (o completar) el lote de facturas de un periodo",
            description = "RF-05 / SWR-02 / SWR-03 / SWR-04. Si no se envía periodo se factura el mes anterior. "
                    + "Es idempotente: re-ejecutarlo solo genera las facturas faltantes.")
    public ResultadoGeneracionLoteResponse generarLote(@Valid @RequestBody(required = false) GenerarLoteRequest solicitud) {
        YearMonth periodo = solicitud == null || solicitud.periodo() == null
                ? null
                : PeriodoFacturacion.parsear(solicitud.periodo());
        return generacionService.generarLote(periodo, GeneracionFacturacionService.ORIGEN_MANUAL);
    }

    @GetMapping("/lotes")
    @Operation(summary = "Historial de lotes de facturación")
    public List<LoteFacturacionResponse> listarLotes() {
        return consultaService.listarLotes();
    }

    @GetMapping("/lotes/{periodo}")
    @Operation(summary = "Detalle de un lote (periodo YYYY-MM)")
    public LoteFacturacionResponse obtenerLote(@PathVariable String periodo) {
        return consultaService.obtenerLote(periodo);
    }

    @GetMapping("/lotes/{periodo}/facturas")
    @Operation(summary = "Facturas generadas en un lote")
    public List<FacturaResumenResponse> listarFacturasDelLote(@PathVariable String periodo) {
        return consultaService.listarFacturasDelLote(periodo);
    }

    @PostMapping("/lotes/{periodo}/sincronizacion")
    @Operation(summary = "Reintentar la sincronización con el ERP de las facturas pendientes del lote",
            description = "SWR-04")
    public ResultadoSincronizacionResponse sincronizarLote(@PathVariable String periodo) {
        return sincronizacionService.sincronizarLote(periodo);
    }

    // ------------------------------------------------------------------ Facturas

    @GetMapping("/facturas")
    @Operation(summary = "Buscar facturas con filtros opcionales")
    public List<FacturaResumenResponse> buscarFacturas(@RequestParam(required = false) String periodo,
                                                       @RequestParam(required = false) String contrato,
                                                       @RequestParam(required = false) EstadoFactura estado,
                                                       @RequestParam(required = false) String documento) {
        return consultaService.buscarFacturas(periodo, contrato, estado, documento);
    }

    @GetMapping("/facturas/{numeroFactura}")
    @Operation(summary = "Detalle de una factura (lecturas, tarifa y liquidación)")
    public FacturaDetalleResponse obtenerFactura(@PathVariable String numeroFactura) {
        return consultaService.obtenerFactura(numeroFactura);
    }

    @PostMapping("/facturas/{numeroFactura}/sincronizacion")
    @Operation(summary = "Sincronizar una factura puntual con el ERP", description = "SWR-04")
    public ResultadoSincronizacionFactura sincronizarFactura(@PathVariable String numeroFactura) {
        return sincronizacionService.sincronizarFactura(numeroFactura);
    }

    @GetMapping("/facturas/{numeroFactura}/sincronizaciones")
    @Operation(summary = "Bitácora de sincronizaciones de la factura con el ERP")
    public List<SincronizacionErpResponse> obtenerSincronizaciones(@PathVariable String numeroFactura) {
        return consultaService.obtenerSincronizaciones(numeroFactura);
    }

    // ------------------------------------------------------------------ Contratos

    @GetMapping("/contratos")
    @Operation(summary = "Contratos de servicio (filtro opcional por estado)")
    public List<ContratoResponse> listarContratos(@RequestParam(required = false) EstadoContrato estado) {
        return consultaService.listarContratos(estado);
    }
}
