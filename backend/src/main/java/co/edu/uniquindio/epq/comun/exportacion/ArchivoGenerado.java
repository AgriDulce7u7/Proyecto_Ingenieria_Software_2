package co.edu.uniquindio.epq.comun.exportacion;

import java.nio.charset.StandardCharsets;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Archivo generado por el sistema (PDF de factura, comprobante, reporte en Excel...).
 */
public record ArchivoGenerado(String nombre, String tipoContenido, byte[] contenido) {

    /** Respuesta HTTP de descarga con el encabezado Content-Disposition correcto. */
    public ResponseEntity<byte[]> comoDescarga() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombre, StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType(tipoContenido))
                .contentLength(contenido.length)
                .body(contenido);
    }
}
