package co.edu.uniquindio.epq.pqr;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * RN-06 / RF-09: la respuesta de una PQR debe quedar registrada con evidencia documental adjunta.
 *
 * @param exigirParaResponder exige al menos una evidencia antes de registrar la respuesta
 * @param tamanoMaximoBytes   tamaño máximo por archivo
 * @param tiposPermitidos     tipos de contenido aceptados
 */
@ConfigurationProperties(prefix = "app.pqr.evidencias")
public record EvidenciaPqrProperties(
        @DefaultValue("true") boolean exigirParaResponder,
        @DefaultValue("5242880") long tamanoMaximoBytes,
        @DefaultValue({"application/pdf", "image/jpeg", "image/png", "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"}) List<String> tiposPermitidos) {
}
