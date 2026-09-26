package co.edu.uniquindio.epq.pqr.web.dto;

public record CiudadanoResponse(
        Integer id,
        String tipoDocumento,
        String numeroDocumento,
        String nombreCompleto,
        String correo,
        String telefono,
        String direccion) {
}
