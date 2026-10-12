package co.edu.uniquindio.epq.dashboard.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * F-08: indicadores de gestión comercial, facturación, cartera y PQR calculados en el momento de la
 * consulta a partir de los datos operativos (SWR-34: sin intervención manual).
 */
public record DashboardResponse(LocalDateTime generadoEn, String mesReferencia, List<CategoriaIndicadoresResponse> categorias) {
}
