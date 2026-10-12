package co.edu.uniquindio.epq.seguridad.web.dto;

import java.time.Instant;
import java.util.List;

/**
 * Resultado del inicio de sesión. El frontend envía {@code token} en el encabezado
 * {@code Authorization: Bearer <token>}.
 */
public record SesionResponse(
        String token,
        String tipoToken,
        Instant expiraEn,
        PerfilResponse usuario) {

    public record PerfilResponse(Integer id, String usuario, String nombre, String rol, String descripcionRol,
                                 String tipo, Integer gestorId, Integer municipioId, List<String> permisos) {
    }
}
