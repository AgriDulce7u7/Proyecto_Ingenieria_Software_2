package co.edu.uniquindio.epq.dashboard.web.dto;

import java.util.List;

public record CategoriaIndicadoresResponse(String categoria, List<IndicadorResponse> indicadores) {
}
