package co.edu.uniquindio.epq.cartera.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Fila del reporte de cartera morosa (SWR-29: usuario, deuda y antigüedad).
 */
public record MorosoResponse(
        String numeroContrato,
        String servicio,
        String estadoContrato,
        String codigoUsuario,
        String documentoUsuario,
        String nombreUsuario,
        String telefono,
        String correo,
        String municipio,
        int facturasVencidas,
        List<String> periodosVencidos,
        int periodosConsecutivos,
        BigDecimal deuda,
        LocalDate vencimientoMasAntiguo,
        long antiguedadDias,
        String rangoMora,
        boolean acuerdoVigente,
        String estadoProcesoCobro,
        boolean candidatoSuspension) {
}
