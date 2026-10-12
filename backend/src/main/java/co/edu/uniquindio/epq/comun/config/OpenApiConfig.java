package co.edu.uniquindio.epq.comun.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Metadatos de la documentación OpenAPI (Swagger UI en /swagger-ui.html).
 * Declara el esquema Bearer JWT para poder probar los endpoints protegidos con el botón "Authorize".
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "bearer-jwt";

    @Bean
    public OpenAPI sigcaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SIGCA-EPQ API")
                        .version("0.2.0")
                        .description("Sistema de Información para la Gestión Comercial y Atención al Cliente de "
                                + "Empresas Públicas del Quindío. F-01 Facturación mensual, F-02 PQR, "
                                + "F-03 Usuarios del servicio, F-04 Contratos, F-05 Pagos, "
                                + "F-06 Factura e historial (portal ciudadano), F-07 Cobro persuasivo, "
                                + "F-08 Dashboard gerencial; transversal: autenticación JWT y roles (RF-14, RNF-05). "
                                + "Obtenga un token en POST /api/auth/login y úselo en 'Authorize'."))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }
}
