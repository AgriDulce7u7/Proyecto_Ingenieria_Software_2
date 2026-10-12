package co.edu.uniquindio.epq.auditoria.web;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.dominio.ModuloAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ConsultaAuditoriaService;
import co.edu.uniquindio.epq.auditoria.servicio.FiltroAuditoria;
import co.edu.uniquindio.epq.auditoria.web.dto.RegistroAuditoriaResponse;
import co.edu.uniquindio.epq.comun.web.PaginaResponse;
import co.edu.uniquindio.epq.seguridad.acceso.SoloAdministrador;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/auditoria")
@Tag(name = "RF-15 Auditoría", description = "CU-11 Consultar log de auditoría")
@SoloAdministrador
public class AuditoriaController {

    private final ConsultaAuditoriaService consultaService;

    public AuditoriaController(ConsultaAuditoriaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping
    @Operation(summary = "Consultar el log de auditoría con filtros y paginación")
    public PaginaResponse<RegistroAuditoriaResponse> consultar(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) ModuloAuditoria modulo,
            @RequestParam(required = false) AccionAuditoria accion,
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) String entidadId,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta,
            @RequestParam(defaultValue = "false") boolean soloFallidos,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "50") int tamano) {
        return consultaService.consultar(
                new FiltroAuditoria(usuario, modulo, accion, entidad, entidadId, desde, hasta, soloFallidos), pagina, tamano);
    }

    @GetMapping("/catalogo")
    @Operation(summary = "Módulos y acciones auditables (para los filtros del frontend)")
    public Map<String, List<String>> catalogo() {
        return Map.of(
                "modulos", Arrays.stream(ModuloAuditoria.values()).map(Enum::name).toList(),
                "acciones", Arrays.stream(AccionAuditoria.values()).map(Enum::name).toList());
    }
}
