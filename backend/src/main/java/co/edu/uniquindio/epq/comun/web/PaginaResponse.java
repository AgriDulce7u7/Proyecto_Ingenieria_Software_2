package co.edu.uniquindio.epq.comun.web;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Página de resultados con un formato estable para el frontend (no expone la clase Page de Spring).
 */
public record PaginaResponse<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static <T> PaginaResponse<T> de(Page<T> pagina) {
        return new PaginaResponse<>(pagina.getContent(), pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages());
    }
}
