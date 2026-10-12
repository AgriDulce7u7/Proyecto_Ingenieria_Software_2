package co.edu.uniquindio.epq.comercial.web.dto;

public record ContratoResumenResponse(
        String numeroContrato,
        String servicio,
        String direccionServicio,
        String municipio,
        String estado,
        boolean candidatoSuspension) {
}
