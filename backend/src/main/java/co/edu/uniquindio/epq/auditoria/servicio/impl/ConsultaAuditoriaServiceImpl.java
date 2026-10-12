package co.edu.uniquindio.epq.auditoria.servicio.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.auditoria.dominio.RegistroAuditoria;
import co.edu.uniquindio.epq.auditoria.repositorio.AuditoriaSpecifications;
import co.edu.uniquindio.epq.auditoria.repositorio.RegistroAuditoriaRepository;
import co.edu.uniquindio.epq.auditoria.servicio.ConsultaAuditoriaService;
import co.edu.uniquindio.epq.auditoria.servicio.FiltroAuditoria;
import co.edu.uniquindio.epq.auditoria.web.dto.RegistroAuditoriaResponse;
import co.edu.uniquindio.epq.comun.web.PaginaResponse;

@Service
@Transactional(readOnly = true)
public class ConsultaAuditoriaServiceImpl implements ConsultaAuditoriaService {

    private static final int TAMANO_MAXIMO = 200;

    private final RegistroAuditoriaRepository repositorio;

    public ConsultaAuditoriaServiceImpl(RegistroAuditoriaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public PaginaResponse<RegistroAuditoriaResponse> consultar(FiltroAuditoria filtro, int pagina, int tamano) {
        Specification<RegistroAuditoria> criterio = AuditoriaSpecifications.todos();
        if (filtro.usuario() != null && !filtro.usuario().isBlank()) {
            criterio = criterio.and(AuditoriaSpecifications.delUsuario(filtro.usuario()));
        }
        if (filtro.modulo() != null) {
            criterio = criterio.and(AuditoriaSpecifications.delModulo(filtro.modulo()));
        }
        if (filtro.accion() != null) {
            criterio = criterio.and(AuditoriaSpecifications.conAccion(filtro.accion()));
        }
        if (filtro.entidad() != null && !filtro.entidad().isBlank()) {
            criterio = criterio.and(AuditoriaSpecifications.deEntidad(filtro.entidad().trim(),
                    filtro.entidadId() == null || filtro.entidadId().isBlank() ? null : filtro.entidadId().trim()));
        }
        if (filtro.desde() != null) {
            criterio = criterio.and(AuditoriaSpecifications.desde(filtro.desde().atStartOfDay()));
        }
        if (filtro.hasta() != null) {
            criterio = criterio.and(AuditoriaSpecifications.hasta(filtro.hasta().plusDays(1).atStartOfDay()));
        }
        if (filtro.soloFallidos()) {
            criterio = criterio.and(AuditoriaSpecifications.soloFallidos());
        }
        int tamanoSeguro = Math.min(Math.max(tamano, 1), TAMANO_MAXIMO);
        PageRequest solicitud = PageRequest.of(Math.max(pagina, 0), tamanoSeguro,
                Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("id")));
        return PaginaResponse.de(repositorio.findAll(criterio, solicitud).map(RegistroAuditoriaResponse::de));
    }

    @Override
    public List<RegistroAuditoriaResponse> historialDeEntidad(String entidad, String entidadId) {
        return repositorio.findTop50ByEntidadAndEntidadIdOrderByFechaDesc(entidad, entidadId).stream()
                .map(RegistroAuditoriaResponse::de)
                .toList();
    }
}
