package co.edu.uniquindio.epq.comercial.web.dto;

/**
 * CU-01 paso 6: confirmación con el código interno asignado. Si la RNEC no validó la identidad,
 * {@code advertencia} explica que el usuario quedó pendiente de validación (SWR-14).
 */
public record UsuarioRegistradoResponse(
        Integer id,
        String codigoInterno,
        String nombreCompleto,
        String estadoValidacion,
        String advertencia,
        PredioResponse predio,
        String servicioSolicitado,
        String mensaje) {
}
