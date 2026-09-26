package co.edu.uniquindio.epq.pqr.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitudTipo;
import co.edu.uniquindio.epq.pqr.servicio.ConsultaPqrService;
import co.edu.uniquindio.epq.pqr.servicio.FiltroPqr;
import co.edu.uniquindio.epq.pqr.servicio.GestionPqrService;
import co.edu.uniquindio.epq.pqr.servicio.MonitoreoPlazosPqrService;
import co.edu.uniquindio.epq.pqr.servicio.RegistroPqrService;
import co.edu.uniquindio.epq.pqr.web.dto.CerrarPqrRequest;
import co.edu.uniquindio.epq.pqr.web.dto.ConsultaEstadoPqrResponse;
import co.edu.uniquindio.epq.pqr.web.dto.HistorialPqrResponse;
import co.edu.uniquindio.epq.pqr.web.dto.NotificacionResponse;
import co.edu.uniquindio.epq.pqr.web.dto.PqrDetalleResponse;
import co.edu.uniquindio.epq.pqr.web.dto.PqrRegistradaResponse;
import co.edu.uniquindio.epq.pqr.web.dto.PqrResumenResponse;
import co.edu.uniquindio.epq.pqr.web.dto.ReasignarPqrRequest;
import co.edu.uniquindio.epq.pqr.web.dto.RegistrarPqrRequest;
import co.edu.uniquindio.epq.pqr.web.dto.ResponderPqrRequest;
import co.edu.uniquindio.epq.pqr.web.dto.ResultadoMonitoreoResponse;
import co.edu.uniquindio.epq.pqr.web.dto.TomarPqrRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * API REST de PQR (F-02). El controlador solo traduce HTTP ↔ servicios; no contiene lógica de negocio.
 */
@RestController
@RequestMapping("/api/pqr")
@Tag(name = "F-02 PQR", description = "Registro y seguimiento de Peticiones, Quejas y Reclamos")
public class PqrController {

    private final RegistroPqrService registroService;
    private final ConsultaPqrService consultaService;
    private final GestionPqrService gestionService;
    private final MonitoreoPlazosPqrService monitoreoService;

    public PqrController(RegistroPqrService registroService, ConsultaPqrService consultaService,
                         GestionPqrService gestionService, MonitoreoPlazosPqrService monitoreoService) {
        this.registroService = registroService;
        this.consultaService = consultaService;
        this.gestionService = gestionService;
        this.monitoreoService = monitoreoService;
    }

    // ------------------------------------------------------------------ Portal ciudadano

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar una PQR (ciudadano)", description = "RF-08, SWR-05, SWR-06")
    public PqrRegistradaResponse registrar(@Valid @RequestBody RegistrarPqrRequest solicitud) {
        return registroService.registrar(solicitud);
    }

    @GetMapping("/consulta/{radicado}")
    @Operation(summary = "Consultar el estado de una PQR sin iniciar sesión", description = "RF-10, SWR-08")
    public ConsultaEstadoPqrResponse consultarEstado(@PathVariable String radicado) {
        return consultaService.consultarEstadoPublico(radicado);
    }

    // ------------------------------------------------------------------ Back-office (Gestor de PQR)

    @GetMapping
    @Operation(summary = "Listar PQR con filtros opcionales (bandeja del gestor)")
    public List<PqrResumenResponse> listar(@RequestParam(required = false) EstadoPqrTipo estado,
                                           @RequestParam(required = false) TipoSolicitudTipo tipo,
                                           @RequestParam(required = false) Integer gestorId,
                                           @RequestParam(required = false) String documento) {
        return consultaService.listar(new FiltroPqr(estado, tipo, gestorId, documento));
    }

    @GetMapping("/{radicado}")
    @Operation(summary = "Detalle completo de una PQR")
    public PqrDetalleResponse obtenerDetalle(@PathVariable String radicado) {
        return consultaService.obtenerDetalle(radicado);
    }

    @GetMapping("/{radicado}/historial")
    @Operation(summary = "Trazabilidad de la PQR (auditoría)", description = "SWR-09")
    public List<HistorialPqrResponse> obtenerHistorial(@PathVariable String radicado) {
        return consultaService.obtenerHistorial(radicado);
    }

    @GetMapping("/{radicado}/notificaciones")
    @Operation(summary = "Notificaciones enviadas sobre la PQR")
    public List<NotificacionResponse> obtenerNotificaciones(@PathVariable String radicado) {
        return consultaService.obtenerNotificaciones(radicado);
    }

    @PatchMapping("/{radicado}/tomar")
    @Operation(summary = "El gestor toma la PQR para atención (Radicado → En trámite)")
    public PqrDetalleResponse tomar(@PathVariable String radicado, @Valid @RequestBody TomarPqrRequest solicitud) {
        return gestionService.tomar(radicado, solicitud);
    }

    @PatchMapping("/{radicado}/reasignar")
    @Operation(summary = "Reasignar la PQR a otro gestor")
    public PqrDetalleResponse reasignar(@PathVariable String radicado,
                                        @Valid @RequestBody ReasignarPqrRequest solicitud) {
        return gestionService.reasignar(radicado, solicitud);
    }

    @PatchMapping("/{radicado}/responder")
    @Operation(summary = "Registrar la respuesta formal (En trámite/Vencido → Resuelto)", description = "RN-06")
    public PqrDetalleResponse responder(@PathVariable String radicado,
                                        @Valid @RequestBody ResponderPqrRequest solicitud) {
        return gestionService.responder(radicado, solicitud);
    }

    @PatchMapping("/{radicado}/cerrar")
    @Operation(summary = "Cerrar la PQR (Resuelto → Cerrado)")
    public PqrDetalleResponse cerrar(@PathVariable String radicado, @Valid @RequestBody CerrarPqrRequest solicitud) {
        return gestionService.cerrar(radicado, solicitud);
    }

    @PostMapping("/monitoreo")
    @Operation(summary = "Ejecutar manualmente el monitoreo de plazos",
            description = "Normalmente corre de forma automática. Envía alertas de 48 h (SWR-07) y marca PQR vencidas.")
    public ResultadoMonitoreoResponse ejecutarMonitoreo() {
        return monitoreoService.ejecutarMonitoreo();
    }
}
