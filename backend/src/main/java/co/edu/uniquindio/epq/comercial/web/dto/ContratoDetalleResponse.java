package co.edu.uniquindio.epq.comercial.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @param accionesDisponibles estados a los que puede pasar el contrato (para habilitar botones)
 * @param advertencias        avisos del registro (p. ej. contrato pendiente por falta de medidor, SWR-19)
 */
public record ContratoDetalleResponse(
        Integer id,
        String numeroContrato,
        String estado,
        String servicio,
        boolean requiereMedidor,
        String codigoUsuario,
        String nombreUsuario,
        String documentoUsuario,
        PredioResponse predio,
        TarifaResponse tarifa,
        MedidorResponse medidorActivo,
        LocalDate fechaInicio,
        LocalDateTime fechaRegistro,
        String registradoPor,
        boolean candidatoSuspension,
        List<String> accionesDisponibles,
        List<HistorialContratoResponse> historial,
        List<String> advertencias) {
}
