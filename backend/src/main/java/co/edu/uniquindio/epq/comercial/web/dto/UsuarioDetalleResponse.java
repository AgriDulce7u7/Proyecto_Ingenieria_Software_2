package co.edu.uniquindio.epq.comercial.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UsuarioDetalleResponse(
        Integer id,
        String codigoInterno,
        String tipoPersona,
        String tipoDocumento,
        String numeroDocumento,
        String nombreCompleto,
        String telefono,
        String correo,
        String direccionNotificacion,
        Integer municipioId,
        String municipio,
        String estadoValidacion,
        String detalleValidacion,
        LocalDateTime fechaValidacion,
        boolean activo,
        boolean cuentaPortalActiva,
        LocalDateTime fechaRegistro,
        LocalDateTime fechaActualizacion,
        String registradoPor,
        List<PredioResponse> predios,
        List<ContratoResumenResponse> contratos) {
}
