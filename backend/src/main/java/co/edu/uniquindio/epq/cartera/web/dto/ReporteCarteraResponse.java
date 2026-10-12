package co.edu.uniquindio.epq.cartera.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ReporteCarteraResponse(
        LocalDateTime generadoEn,
        int contratosEnMora,
        long usuariosEnMora,
        BigDecimal deudaTotal,
        long candidatosSuspension,
        Map<String, BigDecimal> deudaPorRango,
        List<MorosoResponse> morosos) {
}
