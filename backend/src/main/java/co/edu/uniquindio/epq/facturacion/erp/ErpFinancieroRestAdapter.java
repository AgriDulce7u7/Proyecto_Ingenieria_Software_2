package co.edu.uniquindio.epq.facturacion.erp;

import java.time.Duration;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import co.edu.uniquindio.epq.facturacion.ErpProperties;

/**
 * Adaptador REST hacia un ERP financiero real (IS-01). Se activa con {@code app.erp.modo=rest}.
 *
 * <p>Contrato esperado: {@code POST {url}/facturas} con la {@link FacturaErp} en JSON; responde
 * 2xx con un cuerpo {@code {"referencia": "...", "mensaje": "..."}}.</p>
 */
@Component
@ConditionalOnProperty(prefix = "app.erp", name = "modo", havingValue = "rest")
public class ErpFinancieroRestAdapter implements ErpFinancieroGateway {

    private final RestClient restClient;

    public ErpFinancieroRestAdapter(ErpProperties properties, RestClient.Builder builder) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(5));
        fabrica.setReadTimeout(Duration.ofSeconds(15));
        this.restClient = builder
                .baseUrl(properties.url())
                .requestFactory(fabrica)
                .build();
    }

    @Override
    public RespuestaErp enviarFactura(FacturaErp factura) {
        try {
            Map<?, ?> cuerpo = restClient.post()
                    .uri("/facturas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(factura)
                    .retrieve()
                    .body(Map.class);
            String referencia = cuerpo == null ? null : String.valueOf(cuerpo.get("referencia"));
            String mensaje = cuerpo == null ? "Aceptada" : String.valueOf(cuerpo.get("mensaje"));
            return RespuestaErp.exito(referencia, mensaje);
        } catch (RestClientResponseException ex) {
            // El ERP respondió con error (4xx/5xx): se registra como rechazo.
            return RespuestaErp.fallo("HTTP %d: %s".formatted(ex.getStatusCode().value(),
                    ex.getResponseBodyAsString()));
        }
    }
}
