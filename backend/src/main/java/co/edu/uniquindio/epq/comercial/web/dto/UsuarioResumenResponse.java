package co.edu.uniquindio.epq.comercial.web.dto;

public record UsuarioResumenResponse(
        Integer id,
        String codigoInterno,
        String tipoDocumento,
        String numeroDocumento,
        String nombreCompleto,
        String municipio,
        String telefono,
        String correo,
        String estadoValidacion,
        boolean activo) {
}
