package co.edu.uniquindio.epq.cartera.repositorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Fila del modelo de lectura de cartera morosa: un contrato con facturas vencidas fuera de acuerdo.
 */
public record FilaMorosidad(
        Integer contratoId,
        String numeroContrato,
        String servicio,
        String estadoContrato,
        boolean candidatoSuspension,
        String codigoUsuario,
        String tipoDocumento,
        String numeroDocumento,
        String nombreUsuario,
        String telefono,
        String correo,
        Integer municipioId,
        String municipio,
        int facturasVencidas,
        BigDecimal deuda,
        LocalDate vencimientoMasAntiguo,
        List<YearMonth> periodosVencidos,
        boolean acuerdoVigente,
        String estadoProcesoCobro) {
}
