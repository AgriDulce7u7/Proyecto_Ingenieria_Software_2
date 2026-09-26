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
 *   <li>{@code pqr}: F-02 Registrar y dar seguimiento a PQR.</li>
 *   <li>{@code facturacion}: F-01 Generar facturas mensuales de servicio.</li>
 *   <li>{@code comun}: componentes transversales (calendario laboral, errores, configuración).</li>
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
