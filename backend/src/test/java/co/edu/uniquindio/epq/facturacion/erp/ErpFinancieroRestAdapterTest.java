package co.edu.uniquindio.epq.facturacion.erp;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.sun.net.httpserver.HttpServer;

import co.edu.uniquindio.epq.facturacion.ErpProperties;

/**
 * Prueba el adaptador contra un servidor HTTP real levantado en un puerto libre de localhost
 * (com.sun.net.httpserver, incluido en el JDK). Así se valida la petición que se envía y la
 * interpretación de cada tipo de respuesta, sin depender de un ERP externo.
 */
@DisplayName("ErpFinancieroRestAdapter - integración HTTP con el ERP (IS-01)")
class ErpFinancieroRestAdapterTest {

    private static final FacturaErp FACTURA = new FacturaErp("FAC-202608-000001", "2026-08", "CT-ACU-0001",
            "1094950001", "Acueducto", new BigDecimal("21.50"), new BigDecimal("81783.25"),
            new BigDecimal("81783.25"), LocalDate.of(2026, 9, 16));

    private HttpServer servidor;
    private boolean servidorDetenido;
    private ErpFinancieroRestAdapter adaptador;

    // Respuesta que devolverá el servidor y datos de la última petición recibida
    private int estadoRespuesta;
    private String cuerpoRespuesta;
    private String metodoRecibido;
    private String rutaRecibida;
    private String tipoContenidoRecibido;
    private String cuerpoRecibido;

    @BeforeEach
    void iniciarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/api/erp/facturas", intercambio -> {
            metodoRecibido = intercambio.getRequestMethod();
            rutaRecibida = intercambio.getRequestURI().getPath();
            tipoContenidoRecibido = intercambio.getRequestHeaders().getFirst("Content-Type");
            cuerpoRecibido = new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            byte[] bytes = cuerpoRespuesta == null ? new byte[0] : cuerpoRespuesta.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(estadoRespuesta, bytes.length == 0 ? -1 : bytes.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(bytes);
            }
        });
        servidor.start();

        String url = "http://localhost:%d/api/erp".formatted(servidor.getAddress().getPort());
        adaptador = new ErpFinancieroRestAdapter(
                new ErpProperties("rest", url, 3, 0, new ErpProperties.Simulado(0.0)), RestClient.builder());
    }

    @AfterEach
    void detenerServidor() {
        if (!servidorDetenido) {
            servidor.stop(0);
        }
    }

    private void responder(int estado, String cuerpo) {
        estadoRespuesta = estado;
        cuerpoRespuesta = cuerpo;
    }

    @Test
    @DisplayName("Envía un POST JSON a {url}/facturas con los datos de la factura")
    void formatoDeLaPeticion() {
        responder(200, "{\"referencia\": \"ERP-1\", \"mensaje\": \"ok\"}");

        adaptador.enviarFactura(FACTURA);

        assertAll(
                () -> assertEquals("POST", metodoRecibido),
                () -> assertEquals("/api/erp/facturas", rutaRecibida),
                () -> assertTrue(tipoContenidoRecibido.startsWith("application/json")),
                () -> assertTrue(cuerpoRecibido.contains("\"numeroFactura\":\"FAC-202608-000001\"")),
                () -> assertTrue(cuerpoRecibido.contains("\"total\":81783.25")),
                () -> assertTrue(cuerpoRecibido.contains("\"fechaVencimiento\":\"2026-09-16\"")));
    }

    @Test
    @DisplayName("200 con cuerpo: éxito con la referencia y el mensaje del ERP")
    void exitoConCuerpo() {
        responder(200, "{\"referencia\": \"ERP-7788\", \"mensaje\": \"Factura contabilizada\"}");

        RespuestaErp respuesta = adaptador.enviarFactura(FACTURA);

        assertTrue(respuesta.exitosa());
        assertEquals("ERP-7788", respuesta.referencia());
        assertEquals("Factura contabilizada", respuesta.mensaje());
    }

    @Test
    @DisplayName("204 sin cuerpo: éxito sin referencia y mensaje 'Aceptada'")
    void exitoSinCuerpo() {
        responder(204, null);

        RespuestaErp respuesta = adaptador.enviarFactura(FACTURA);

        assertTrue(respuesta.exitosa());
        assertNull(respuesta.referencia());
        assertEquals("Aceptada", respuesta.mensaje());
    }

    @Test
    @DisplayName("4xx: rechazo con el código HTTP y el cuerpo del error")
    void rechazoDeNegocio() {
        responder(422, "{\"error\": \"Contrato no existe en el ERP\"}");

        RespuestaErp respuesta = adaptador.enviarFactura(FACTURA);

        assertFalse(respuesta.exitosa());
        assertTrue(respuesta.mensaje().startsWith("HTTP 422: "));
        assertTrue(respuesta.mensaje().contains("Contrato no existe en el ERP"));
    }

    @Test
    @DisplayName("5xx: rechazo con el código HTTP")
    void errorDelServidor() {
        responder(503, "{\"error\": \"Mantenimiento\"}");

        RespuestaErp respuesta = adaptador.enviarFactura(FACTURA);

        assertFalse(respuesta.exitosa());
        assertTrue(respuesta.mensaje().startsWith("HTTP 503"));
    }

    @Test
    @DisplayName("ERP caído: la falla técnica se propaga para que el sincronizador la cuente como intento fallido")
    void erpCaido() {
        servidor.stop(0);
        servidorDetenido = true;

        assertThrows(ResourceAccessException.class, () -> adaptador.enviarFactura(FACTURA));
    }
}