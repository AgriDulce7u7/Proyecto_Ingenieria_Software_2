package co.edu.uniquindio.epq.dashboard.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.epq.dashboard.servicio.DashboardService;
import co.edu.uniquindio.epq.dashboard.web.dto.CategoriaIndicadoresResponse;
import co.edu.uniquindio.epq.dashboard.web.dto.DashboardResponse;
import co.edu.uniquindio.epq.seguridad.acceso.AccesoGerencial;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "F-08 Dashboard gerencial", description = "CU-09 Consultar dashboard gerencial (RF-13)")
@AccesoGerencial
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "Indicadores de gestión comercial, facturación, cartera y PQR con tendencia de 12 meses",
            description = "SWR-33 solo Gerente y Directivo; SWR-34 calculados en tiempo real; SWR-35 tendencia de 12 meses")
    public DashboardResponse consultar() {
        return dashboardService.consultar();
    }

    @GetMapping("/categoria")
    @Operation(summary = "Indicadores de una sola categoría (para refrescar un panel)")
    public CategoriaIndicadoresResponse categoria(@RequestParam String nombre) {
        return dashboardService.categoria(nombre);
    }
}
