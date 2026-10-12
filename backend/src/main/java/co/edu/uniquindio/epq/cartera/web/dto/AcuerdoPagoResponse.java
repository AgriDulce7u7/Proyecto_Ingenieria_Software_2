package co.edu.uniquindio.epq.cartera.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AcuerdoPagoResponse(
        String numeroAcuerdo,
        String numeroContrato,
        String servicio,
        String usuario,
        String documentoUsuario,
        LocalDateTime fechaAcuerdo,
        BigDecimal totalDeuda,
        BigDecimal valorPagado,
        BigDecimal saldoPendiente,
        int numeroCuotas,
        String estado,
        LocalDateTime fechaCambioEstado,
        String observacion,
        String registradoPor,
        List<String> facturasIncluidas,
        List<CuotaResponse> cuotas) {
}
