package co.edu.uniquindio.epq.pqr.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.epq.comun.exportacion.FormatoExportacion;
import co.edu.uniquindio.epq.pqr.servicio.supervision.SupervisionPqrService;
import co.edu.uniquindio.epq.pqr.web.dto.AlertaPqrResponse;
import co.edu.uniquindio.epq.pqr.web.dto.CumplimientoPqrResponse;
import co.edu.uniquindio.epq.seguridad.acceso.AccesoIndicadoresPqr;
import co.edu.uniquindio.epq.seguridad.acceso.AccesoSupervisionPqr;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Supervisión de la Oficina de PQR: alertas del equipo para el jefe (StR-02c) e indicador
 * de cumplimiento de plazos en días hábiles (SyR-02f).
 */
@RestController
@RequestMapping("/api/pqr")
@Tag(name = "F-02 Supervisión PQR", description = "Alertas del equipo e indicadores de cumplimiento de plazos")
public class SupervisionPqrController {

    private final SupervisionPqrService supervisionService;

    public SupervisionPqrController(SupervisionPqrService supervisionService) {
        this.supervisionService = supervisionService;
    }

    @GetMapping("/supervision/alertas")
    @AccesoSupervisionPqr
    @Operation(summary = "Alertas de vencimiento de las PQR pendientes del equipo",
            description = "StR-02c. Sin jefeId se listan todas las PQR pendientes de la oficina.")
    public List<AlertaPqrResponse> alertas(@RequestParam(required = false) Integer jefeId) {
        return supervisionService.alertasDelEquipo(jefeId);
    }

    @GetMapping("/indicadores/cumplimiento")
    @AccesoIndicadoresPqr
    @Operation(summary = "Porcentaje de PQR respondidas dentro del plazo legal", description = "SyR-02f")
    public CumplimientoPqrResponse cumplimiento(
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) {
        return supervisionService.cumplimiento(desde, hasta);
    }

    @GetMapping("/indicadores/cumplimiento/exportar")
    @AccesoIndicadoresPqr
    @Operation(summary = "Exportar las PQR respondidas fuera de plazo o vencidas", description = "SyR-02f. formato=pdf|excel")
    public ResponseEntity<byte[]> exportar(
            @RequestParam(defaultValue = "pdf") String formato,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) {
        return supervisionService.exportarIncumplimientos(desde, hasta, FormatoExportacion.desde(formato))
                .comoDescarga();
    }
}
