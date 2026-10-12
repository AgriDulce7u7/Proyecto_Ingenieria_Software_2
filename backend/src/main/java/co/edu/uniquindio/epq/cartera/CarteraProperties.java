package co.edu.uniquindio.epq.cartera;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Parámetros de cobro persuasivo (F-07).
 *
 * @param periodosParaSuspension RN-03 / SWR-32: períodos consecutivos de mora para ser candidato a suspensión
 * @param cuotaMinima            valor mínimo de cuota usado para calcular el número de cuotas (SWR-30)
 * @param minCuotas              mínimo de cuotas de un acuerdo
 * @param maxCuotas              máximo de cuotas de un acuerdo
 * @param diasPrimeraCuota       días entre el acuerdo y la primera cuota cuando no se indica la fecha
 * @param monitoreoCron          ejecución diaria del monitoreo de cartera
 */
@ConfigurationProperties(prefix = "app.cartera")
public record CarteraProperties(
        @DefaultValue("2") int periodosParaSuspension,
        @DefaultValue("50000") BigDecimal cuotaMinima,
        @DefaultValue("2") int minCuotas,
        @DefaultValue("12") int maxCuotas,
        @DefaultValue("30") int diasPrimeraCuota,
        @DefaultValue("0 30 2 * * *") String monitoreoCron) {
}
