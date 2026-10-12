package co.edu.uniquindio.epq.pagos.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IntentoPagoResponse(Long id, LocalDateTime fecha, BigDecimal valor, String resultado,
                                  String codigoTransaccion, String codigoRespuesta, String mensaje,
                                  String ultimosDigitos, int intentos, String usuario) {
}
