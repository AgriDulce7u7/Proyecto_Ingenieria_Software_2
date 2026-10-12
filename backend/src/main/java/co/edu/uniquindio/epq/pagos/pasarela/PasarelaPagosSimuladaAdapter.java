package co.edu.uniquindio.epq.pagos.pasarela;

import java.time.Clock;
import java.time.YearMonth;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Pasarela simulada para el prototipo. Tarjetas de prueba:
 * <ul>
 *   <li>{@code 4111111111111111}: aprobada.</li>
 *   <li>Terminada en {@code 0002}: rechazada por fondos insuficientes (código 51).</li>
 *   <li>Terminada en {@code 0069}: rechazada por tarjeta vencida (código 54).</li>
 *   <li>Terminada en {@code 0119}: la pasarela no responde (prueba de reintentos y timeout).</li>
 *   <li>Número que no cumple el algoritmo de Luhn: rechazada (código 14).</li>
 * </ul>
 */
@Component
@ConditionalOnProperty(prefix = "app.pasarela", name = "modo", havingValue = "simulada", matchIfMissing = true)
public class PasarelaPagosSimuladaAdapter implements PasarelaPagosGateway {

    private final Clock clock;

    public PasarelaPagosSimuladaAdapter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public RespuestaPasarela cobrar(SolicitudCobro solicitud) {
        DatosTarjeta tarjeta = solicitud.tarjeta();
        String numero = tarjeta.numero();
        if (numero.endsWith("0119")) {
            throw new PasarelaNoDisponibleException("Pasarela simulada: tiempo de espera agotado");
        }
        if (!cumpleLuhn(numero)) {
            return RespuestaPasarela.rechazada("14", "Número de tarjeta inválido");
        }
        if (numero.endsWith("0069")
                || YearMonth.of(tarjeta.anioVencimiento(), tarjeta.mesVencimiento()).isBefore(YearMonth.now(clock))) {
            return RespuestaPasarela.rechazada("54", "Tarjeta vencida");
        }
        if (numero.endsWith("0002")) {
            return RespuestaPasarela.rechazada("51", "Fondos insuficientes");
        }
        String codigo = "TX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return RespuestaPasarela.aprobada(codigo, "Transacción aprobada por la pasarela simulada");
    }

    /** Algoritmo de Luhn para validar el número de tarjeta. */
    static boolean cumpleLuhn(String numero) {
        int suma = 0;
        boolean duplicar = false;
        for (int i = numero.length() - 1; i >= 0; i--) {
            int digito = numero.charAt(i) - '0';
            if (digito < 0 || digito > 9) {
                return false;
            }
            if (duplicar) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }
            suma += digito;
            duplicar = !duplicar;
        }
        return numero.length() >= 13 && suma % 10 == 0;
    }
}
