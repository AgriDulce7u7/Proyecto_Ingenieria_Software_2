package co.edu.uniquindio.epq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada del backend del SIGCA-EPQ.
 *
 * <p>Organización por módulos funcionales (package-by-feature):</p>
 * <ul>
 *   <li>{@code facturacion}: F-01 Generar facturas mensuales de servicio (+ anulación y PDF).</li>
 *   <li>{@code pqr}: F-02 Registrar y dar seguimiento a PQR (plazos en días hábiles, evidencias, supervisión).</li>
 *   <li>{@code seguridad}: autenticación JWT, bloqueo por intentos fallidos y control de acceso por roles
 *       (transversal: RF-14, RNF-05).</li>
 *   <li>{@code auditoria}: log de auditoría transversal (RF-15).</li>
 *   <li>{@code comercial}: F-03 Usuarios del servicio y predios (validación RNEC) y F-04 Contratos y tarifas.</li>
 *   <li>{@code pagos}: F-05 Registro de pagos presenciales y en línea (pasarela).</li>
 *   <li>{@code portal}: F-06 Consultar factura e historial de consumos (portal del ciudadano).</li>
 *   <li>{@code cartera}: F-07 Cobro persuasivo, acuerdos de pago y candidatos a suspensión.</li>
 *   <li>{@code dashboard}: F-08 Indicadores gerenciales.</li>
 *   <li>{@code comun}: componentes transversales (calendario laboral, territorio, exportación PDF/Excel,
 *       notificaciones, errores, configuración).</li>
 * </ul>
 */
@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class SigcaEpqApplication {

    public static void main(String[] args) {
        SpringApplication.run(SigcaEpqApplication.class, args);
    }
}
