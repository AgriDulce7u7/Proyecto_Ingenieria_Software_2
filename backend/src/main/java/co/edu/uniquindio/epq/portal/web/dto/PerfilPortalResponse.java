package co.edu.uniquindio.epq.portal.web.dto;

public record PerfilPortalResponse(
        String codigoInterno,
        String tipoDocumento,
        String numeroDocumento,
        String nombreCompleto,
        String correo,
        String telefono,
        String direccionNotificacion,
        String municipio) {
}
