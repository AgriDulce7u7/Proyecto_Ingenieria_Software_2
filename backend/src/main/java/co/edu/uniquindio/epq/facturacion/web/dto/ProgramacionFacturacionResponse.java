package co.edu.uniquindio.epq.facturacion.web.dto;

import java.time.LocalDate;

/**
 * Información de la programación automática (SWR-01) para mostrar en el tablero.
 */
public record ProgramacionFacturacionResponse(
        boolean ejecucionAutomaticaHabilitada,
        LocalDate hoy,
        boolean hoyEsPrimerDiaHabil,
        LocalDate primerDiaHabilMesActual,
        LocalDate proximaEjecucion,
        String periodoPendienteDeFacturar,
        String estadoLotePeriodoPendiente,
        int tiempoMaximoLoteMinutos,
        int diasParaVencimiento) {
}
