package co.edu.uniquindio.epq.seguridad.config;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import co.edu.uniquindio.epq.seguridad.SeguridadProperties;
import co.edu.uniquindio.epq.seguridad.contexto.ClaimsToken;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Seguridad de la API (RNF-05, RF-14): autenticación sin estado con JWT y control de acceso por roles.
 *
 * <ul>
 *   <li>Rutas públicas: inicio de sesión, radicación y consulta pública de PQR (RF-10), catálogos y Swagger.</li>
 *   <li>Portal de autogestión ({@code /api/portal/**}): solo rol CIUDADANO.</li>
 *   <li>Resto de {@code /api/**}: usuario autenticado + {@code @PreAuthorize} por rol en cada endpoint.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfiguracionSeguridad {

    private static final String[] RUTAS_PUBLICAS = {
            "/api/auth/login", "/api/auth/ciudadano/login", "/api/auth/ciudadano/activacion",
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
            "/actuator/health", "/actuator/info", "/error"
    };

    private static final String[] CONSULTAS_PUBLICAS = {
            "/api/pqr/consulta/**", "/api/pqr/catalogos/**", "/api/municipios"
    };

    @Bean
    public SecurityFilterChain cadenaSeguridad(HttpSecurity http, ManejadorErroresSeguridad errores,
                                               JwtAuthenticationConverter convertidorJwt) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)          // API sin estado: no usa cookies de sesión
                .cors(Customizer.withDefaults())             // usa la configuración de CorsConfig
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(RUTAS_PUBLICAS).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/pqr").permitAll()          // radicar PQR (RF-08)
                        .requestMatchers(HttpMethod.GET, CONSULTAS_PUBLICAS).permitAll()   // RF-10, SWR-08
                        .requestMatchers("/api/portal/**").hasRole("CIUDADANO")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(servidor -> servidor
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorJwt))
                        .authenticationEntryPoint(errores)
                        .accessDeniedHandler(errores))
                .exceptionHandling(excepciones -> excepciones
                        .authenticationEntryPoint(errores)
                        .accessDeniedHandler(errores));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder(SeguridadProperties propiedades) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave(propiedades)));
    }

    @Bean
    public JwtDecoder jwtDecoder(SeguridadProperties propiedades) {
        NimbusJwtDecoder decodificador = NimbusJwtDecoder.withSecretKey(clave(propiedades))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decodificador.setJwtValidator(JwtValidators.createDefaultWithIssuer(propiedades.jwt().emisor()));
        return decodificador;
    }

    /** Convierte el claim "roles" en autoridades ROLE_* para hasRole/hasAnyRole. */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter autoridades = new JwtGrantedAuthoritiesConverter();
        autoridades.setAuthoritiesClaimName(ClaimsToken.ROLES);
        autoridades.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(autoridades);
        return convertidor;
    }

    private static SecretKey clave(SeguridadProperties propiedades) {
        return new SecretKeySpec(propiedades.jwt().secreto().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
