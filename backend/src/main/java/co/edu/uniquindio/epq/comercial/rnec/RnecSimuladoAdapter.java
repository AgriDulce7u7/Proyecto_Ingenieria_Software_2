package co.edu.uniquindio.epq.comercial.rnec;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RNEC simulada para el prototipo. Reglas de demostración:
 * <ul>
 *   <li>Documento que empieza por {@code 99}: servicio no disponible (CU-01 4b).</li>
 *   <li>Documento que empieza por {@code 00}: identificación no encontrada.</li>
 *   <li>{@code app.rnec.simulado-disponible=false}: simula la caída total del servicio.</li>
 *   <li>Cualquier otro documento: identidad validada.</li>
 * </ul>
 */
@Component
@ConditionalOnProperty(prefix = "app.rnec", name = "modo", havingValue = "simulado", matchIfMissing = true)
public class RnecSimuladoAdapter implements ValidadorIdentidadRnec {

    private final RnecProperties propiedades;

    public RnecSimuladoAdapter(RnecProperties propiedades) {
        this.propiedades = propiedades;
    }

    @Override
    public ResultadoValidacionRnec validar(SolicitudValidacionRnec solicitud) {
        String numero = solicitud.numeroDocumento();
        if (!propiedades.simuladoDisponible() || numero.startsWith("99")) {
            return ResultadoValidacionRnec.noDisponible("RNEC simulada: el servicio no respondió");
        }
        if (numero.startsWith("00")) {
            return new ResultadoValidacionRnec(ResultadoValidacionRnec.Estado.NO_ENCONTRADA,
                    "RNEC simulada: la identificación no se encuentra en el archivo nacional");
        }
        return new ResultadoValidacionRnec(ResultadoValidacionRnec.Estado.VALIDADA,
                "RNEC simulada: identificación %s %s vigente".formatted(solicitud.tipoDocumento(), numero));
    }
}
