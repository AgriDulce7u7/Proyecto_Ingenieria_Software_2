package co.edu.uniquindio.epq.pqr.servicio.evidencia;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import co.edu.uniquindio.epq.comun.exportacion.ArchivoGenerado;
import co.edu.uniquindio.epq.pqr.web.dto.EvidenciaPqrResponse;

/**
 * RN-06: evidencias documentales de la respuesta de una PQR.
 */
public interface EvidenciaPqrService {

    EvidenciaPqrResponse adjuntar(String radicado, MultipartFile archivo, String descripcion);

    List<EvidenciaPqrResponse> listar(String radicado);

    ArchivoGenerado descargar(String radicado, Long evidenciaId);
}
