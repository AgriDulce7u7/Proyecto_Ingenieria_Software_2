package co.edu.uniquindio.epq.pagos.pasarela;

import java.time.Duration;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Adaptador REST/HTTPS hacia una pasarela real (IS-02). Contrato esperado:
 * {@code POST {url}/transacciones} → {@code {"aprobada": true, "codigoTransaccion": "...", "codigoRespuesta": "00", "mensaje": "..."}}.
 */
@Component
@ConditionalOnProperty(prefix = "app.pasarela", name = "modo", havingValue = "rest")
public class PasarelaPagosRestAdapter implements PasarelaPagosGateway {

    private final RestClient restClient;

    public PasarelaPagosRestAdapter(PasarelaProperties propiedades, RestClient.Builder builder) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(5));
        fabrica.setReadTimeout(Duration.ofSeconds(propiedades.timeoutSegundos()));
        this.restClient = builder.baseUrl(propiedades.url()).requestFactory(fabrica).build();
    }

    @Override
    public RespuestaPasarela cobrar(SolicitudCobro solicitud) {
        try {
            Map<?, ?> cuerpo = restClient.post()
                    .uri("/transacciones")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", solicitud.referencia())
                    .body(Map.of(
                            "referencia", solicitud.referencia(),
                            "monto", solicitud.monto(),
                            "descripcion", solicitud.descripcion(),
                            "tarjeta", Map.of("numero", solicitud.tarjeta().numero(),
                                    "titular", solicitud.tarjeta().titular(),
                                    "mes", solicitud.tarjeta().mesVencimiento(),
                                    "anio", solicitud.tarjeta().anioVencimiento(),
                                    "cvv", solicitud.tarjeta().cvv())))
                    .retrieve()
                    .body(Map.class);
            if (cuerpo == null) {
                throw new PasarelaNoDisponibleException("La pasarela respondió sin contenido");
            }
            boolean aprobada = Boolean.TRUE.equals(cuerpo.get("aprobada"));
            return new RespuestaPasarela(aprobada, (String) cuerpo.get("codigoTransaccion"),
                    String.valueOf(cuerpo.get("codigoRespuesta")), String.valueOf(cuerpo.get("mensaje")));
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().is5xxServerError()) {
                throw new PasarelaNoDisponibleException("La pasarela respondió HTTP " + ex.getStatusCode().value(), ex);
            }
            return RespuestaPasarela.rechazada(String.valueOf(ex.getStatusCode().value()), ex.getResponseBodyAsString());
        } catch (ResourceAccessException ex) {
            throw new PasarelaNoDisponibleException("No fue posible comunicarse con la pasarela", ex);
        }
    }
}
