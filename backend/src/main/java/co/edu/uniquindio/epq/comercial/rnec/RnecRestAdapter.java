package co.edu.uniquindio.epq.comercial.rnec;

import java.time.Duration;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Adaptador REST hacia el servicio de la RNEC (IS-03). Contrato esperado:
 * {@code POST {url}/validaciones} → {@code {"estado": "VALIDADA|NO_COINCIDE|NO_ENCONTRADA", "mensaje": "..."}}.
 * Cualquier falla técnica se traduce a NO_DISPONIBLE (SWR-14).
 */
@Component
@ConditionalOnProperty(prefix = "app.rnec", name = "modo", havingValue = "rest")
public class RnecRestAdapter implements ValidadorIdentidadRnec {

    private final RestClient restClient;

    public RnecRestAdapter(RnecProperties propiedades, RestClient.Builder builder) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(propiedades.timeoutSegundos()));
        fabrica.setReadTimeout(Duration.ofSeconds(propiedades.timeoutSegundos()));
        this.restClient = builder.baseUrl(propiedades.url()).requestFactory(fabrica).build();
    }

    @Override
    public ResultadoValidacionRnec validar(SolicitudValidacionRnec solicitud) {
        try {
            Map<?, ?> cuerpo = restClient.post()
                    .uri("/validaciones")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(solicitud)
                    .retrieve()
                    .body(Map.class);
            if (cuerpo == null || cuerpo.get("estado") == null) {
                return ResultadoValidacionRnec.noDisponible("La RNEC respondió sin estado");
            }
            return new ResultadoValidacionRnec(ResultadoValidacionRnec.Estado.valueOf(String.valueOf(cuerpo.get("estado"))),
                    String.valueOf(cuerpo.get("mensaje")));
        } catch (RuntimeException ex) {
            return ResultadoValidacionRnec.noDisponible("RNEC no disponible: " + ex.getMessage());
        }
    }
}
