package co.edu.uniquindio.epq.comun.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Metadatos de la documentación OpenAPI (Swagger UI en /swagger-ui.html).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sigcaOpenApi() {
        return new OpenAPI().info(new Info()
                .title("SIGCA-EPQ API")
                .version("0.1.0")
                .description("Prototipo del Sistema de Información para la Gestión Comercial y Atención al "
                        + "Cliente de Empresas Públicas del Quindío. F-01: Facturación mensual automática. "
                        + "F-02: Registro y seguimiento de PQR."));
    }
}
