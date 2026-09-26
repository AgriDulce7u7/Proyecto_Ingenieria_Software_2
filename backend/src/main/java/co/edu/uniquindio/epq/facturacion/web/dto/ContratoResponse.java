package co.edu.uniquindio.epq.facturacion.web.dto;

import java.time.LocalDate;

public record ContratoResponse(
        Integer id,
        String numeroContrato,
        String servicio,
        String direccionServicio,
        LocalDate fechaInicio,
        String estado,
        String tipoDocumentoCliente,
        String numeroDocumentoCliente,
        String cliente) {
}
