package co.edu.uniquindio.epq.pqr.servicio;

import co.edu.uniquindio.epq.pqr.web.dto.PqrRegistradaResponse;
import co.edu.uniquindio.epq.pqr.web.dto.RegistrarPqrRequest;

/**
 * Caso de uso: el ciudadano registra una PQR (RF-08, SWR-05, SWR-06).
 *
 * <p>Las interfaces de servicio están segregadas por actor/caso de uso (ISP): el portal ciudadano
 * solo depende de lo que usa y no de las operaciones del back-office.</p>
 */
public interface RegistroPqrService {

    PqrRegistradaResponse registrar(RegistrarPqrRequest solicitud);
}
