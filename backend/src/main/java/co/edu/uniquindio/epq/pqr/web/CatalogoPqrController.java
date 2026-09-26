package co.edu.uniquindio.epq.pqr.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.epq.pqr.servicio.CatalogoPqrService;
import co.edu.uniquindio.epq.pqr.web.dto.ElementoCatalogoResponse;
import co.edu.uniquindio.epq.pqr.web.dto.GestorResponse;
import co.edu.uniquindio.epq.pqr.web.dto.NotificacionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "F-02 Catálogos PQR", description = "Datos de apoyo para los formularios del frontend")
public class CatalogoPqrController {

    private final CatalogoPqrService catalogoService;

    public CatalogoPqrController(CatalogoPqrService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/pqr/catalogos/tipos-solicitud")
    @Operation(summary = "Tipos de solicitud (Petición, Queja, Reclamo)")
    public List<ElementoCatalogoResponse> tiposSolicitud() {
        return catalogoService.tiposSolicitud();
    }

    @GetMapping("/pqr/catalogos/canales")
    @Operation(summary = "Canales de atención")
    public List<ElementoCatalogoResponse> canales() {
        return catalogoService.canales();
    }

    @GetMapping("/pqr/catalogos/estados")
    @Operation(summary = "Estados de una PQR")
    public List<ElementoCatalogoResponse> estados() {
        return catalogoService.estados();
    }

    @GetMapping("/gestores")
    @Operation(summary = "Gestores de PQR activos")
    public List<GestorResponse> gestores() {
        return catalogoService.gestoresActivos();
    }

    @GetMapping("/gestores/{gestorId}/notificaciones")
    @Operation(summary = "Bandeja de notificaciones de un gestor (asignaciones y alertas)")
    public List<NotificacionResponse> notificacionesDeGestor(@PathVariable Integer gestorId) {
        return catalogoService.notificacionesDeGestor(gestorId);
    }
}
