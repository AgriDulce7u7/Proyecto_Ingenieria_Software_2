package co.edu.uniquindio.epq.pqr.web.dto;

/**
 * Elemento de catálogo para listas desplegables del frontend.
 *
 * @param codigo valor que debe enviarse a la API (por ejemplo {@code QUEJA})
 * @param nombre texto para mostrar al usuario (por ejemplo {@code Queja})
 */
public record ElementoCatalogoResponse(Integer id, String codigo, String nombre) {
}
