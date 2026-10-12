package co.edu.uniquindio.epq.cartera.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.epq.cartera.dominio.EstadoAcuerdo;
import co.edu.uniquindio.epq.cartera.dominio.EstadoProcesoCobro;
import co.edu.uniquindio.epq.cartera.servicio.AcuerdoPagoService;
import co.edu.uniquindio.epq.cartera.servicio.MonitoreoCarteraService;
import co.edu.uniquindio.epq.cartera.servicio.ReporteCarteraService;
import co.edu.uniquindio.epq.cartera.web.dto.AcuerdoPagoResponse;
import co.edu.uniquindio.epq.cartera.web.dto.PagarCuotaRequest;
import co.edu.uniquindio.epq.cartera.web.dto.PlanCuotasResponse;
import co.edu.uniquindio.epq.cartera.web.dto.ProcesoCobroResponse;
import co.edu.uniquindio.epq.cartera.web.dto.RegistrarAcuerdoRequest;
import co.edu.uniquindio.epq.cartera.web.dto.ReporteCarteraResponse;
import co.edu.uniquindio.epq.cartera.web.dto.ResultadoMonitoreoCarteraResponse;
import co.edu.uniquindio.epq.comun.exportacion.FormatoExportacion;
import co.edu.uniquindio.epq.seguridad.acceso.AccesoCartera;
import co.edu.uniquindio.epq.seguridad.acceso.AccesoCobro;
import co.edu.uniquindio.epq.seguridad.acceso.AccesoReporteCartera;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cartera")
@Tag(name = "F-07 Cobro persuasivo", description = "CU-08 Gestionar cobro persuasivo (RF-11, RF-12, RN-03, RN-07)")
public class CarteraController {

    private final ReporteCarteraService reporteService;
    private final AcuerdoPagoService acuerdoService;
    private final MonitoreoCarteraService monitoreoService;

    public CarteraController(ReporteCarteraService reporteService, AcuerdoPagoService acuerdoService,
                             MonitoreoCarteraService monitoreoService) {
        this.reporteService = reporteService;
        this.acuerdoService = acuerdoService;
        this.monitoreoService = monitoreoService;
    }

    @GetMapping("/morosos")
    @AccesoReporteCartera
    @Operation(summary = "Reporte de cartera morosa (usuario, deuda y antigüedad)", description = "RF-11, SWR-29")
    public ReporteCarteraResponse morosos(@RequestParam(required = false) Integer municipioId,
                                          @RequestParam(required = false) Integer minPeriodos,
                                          @RequestParam(defaultValue = "false") boolean soloCandidatos) {
        return reporteService.morosos(municipioId, minPeriodos, soloCandidatos);
    }

    @GetMapping("/morosos/exportar")
    @AccesoReporteCartera
    @Operation(summary = "Exportar el reporte de cartera en PDF o Excel", description = "SWR-29. formato=pdf|excel")
    public ResponseEntity<byte[]> exportar(@RequestParam(defaultValue = "pdf") String formato,
                                           @RequestParam(required = false) Integer municipioId,
                                           @RequestParam(required = false) Integer minPeriodos,
                                           @RequestParam(defaultValue = "false") boolean soloCandidatos) {
        return reporteService.exportar(municipioId, minPeriodos, soloCandidatos, FormatoExportacion.desde(formato))
                .comoDescarga();
    }

    @GetMapping("/candidatos-suspension")
    @AccesoReporteCartera
    @Operation(summary = "Candidatos a suspensión: 2 o más períodos consecutivos de mora sin acuerdo", description = "SWR-32, RN-03")
    public ReporteCarteraResponse candidatos(@RequestParam(required = false) Integer municipioId) {
        return reporteService.morosos(municipioId, null, true);
    }

    @GetMapping("/acuerdos/simulacion")
    @AccesoCartera
    @Operation(summary = "Simular el plan de cuotas antes de registrar el acuerdo", description = "SWR-30")
    public PlanCuotasResponse simular(@RequestParam String contrato, @RequestParam(required = false) Integer numeroCuotas,
                                      @RequestParam(required = false) LocalDate fechaPrimeraCuota) {
        return acuerdoService.simular(contrato, numeroCuotas, fechaPrimeraCuota);
    }

    @PostMapping("/acuerdos")
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoCartera
    @Operation(summary = "Registrar un acuerdo de pago con cuotas calculadas automáticamente", description = "RF-12, SWR-30")
    public AcuerdoPagoResponse registrar(@Valid @RequestBody RegistrarAcuerdoRequest solicitud) {
        return acuerdoService.registrar(solicitud);
    }

    @GetMapping("/acuerdos")
    @AccesoCartera
    @Operation(summary = "Listar acuerdos de pago")
    public List<AcuerdoPagoResponse> listar(@RequestParam(required = false) EstadoAcuerdo estado,
                                            @RequestParam(required = false) String contrato) {
        return acuerdoService.listar(estado, contrato);
    }

    @GetMapping("/acuerdos/{numeroAcuerdo}")
    @AccesoCartera
    @Operation(summary = "Detalle del acuerdo con sus cuotas")
    public AcuerdoPagoResponse obtener(@PathVariable String numeroAcuerdo) {
        return acuerdoService.obtener(numeroAcuerdo);
    }

    @PostMapping("/acuerdos/{numeroAcuerdo}/cuotas/{numeroCuota}/pago")
    @AccesoCobro
    @Operation(summary = "Registrar el pago de una cuota (en orden). Al pagar la última, las facturas quedan pagadas")
    public AcuerdoPagoResponse pagarCuota(@PathVariable String numeroAcuerdo, @PathVariable int numeroCuota,
                                          @Valid @RequestBody PagarCuotaRequest solicitud) {
        return acuerdoService.pagarCuota(numeroAcuerdo, numeroCuota, solicitud);
    }

    @GetMapping("/procesos-cobro")
    @AccesoCartera
    @Operation(summary = "Procesos de cobro coercitivo (activo, suspendido por acuerdo o cerrado)", description = "RN-07")
    public List<ProcesoCobroResponse> procesos(@RequestParam(required = false) EstadoProcesoCobro estado) {
        return acuerdoService.procesosCobro(estado);
    }

    @PostMapping("/monitoreo")
    @AccesoCartera
    @Operation(summary = "Ejecutar el monitoreo de cartera ahora (normalmente corre cada día)",
            description = "Vence facturas, incumple acuerdos con cuotas vencidas (SWR-31) y marca candidatos a suspensión (SWR-32)")
    public ResultadoMonitoreoCarteraResponse monitoreo() {
        return monitoreoService.ejecutar();
    }
}
