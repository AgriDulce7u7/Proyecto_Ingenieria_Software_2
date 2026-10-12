package co.edu.uniquindio.epq.auditoria.servicio;

import java.util.List;

import co.edu.uniquindio.epq.auditoria.web.dto.RegistroAuditoriaResponse;
import co.edu.uniquindio.epq.comun.web.PaginaResponse;

/**
 * CU-11: Consultar log de auditoría (solo Administrador del Sistema).
 */
public interface ConsultaAuditoriaService {

    PaginaResponse<RegistroAuditoriaResponse> consultar(FiltroAuditoria filtro, int pagina, int tamano);

    List<RegistroAuditoriaResponse> historialDeEntidad(String entidad, String entidadId);
}
