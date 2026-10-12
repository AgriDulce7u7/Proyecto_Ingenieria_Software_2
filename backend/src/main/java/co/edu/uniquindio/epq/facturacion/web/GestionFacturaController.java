package co.edu.uniquindio.epq.facturacion.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.epq.facturacion.documento.GeneradorDocumentosFactura;
import co.edu.uniquindio.epq.facturacion.servicio.AnulacionFacturaService;
import co.edu.uniquindio.epq.facturacion.web.dto.AnularFacturaRequest;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaDetalleResponse;
import co.edu.uniquindio.epq.seguridad.acceso.AccesoComercial;
import co.edu.uniquindio.epq.seguridad.acceso.AccesoConsultaFacturacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Operaciones de la entrega 2 sobre facturas individuales: anulación (DE-01) y PDF oficial (SWR-27).
 */
@RestController
@RequestMapping("/api/facturacion/facturas")
@Tag(name = "F-01 Facturación", description = "Generación de facturas mensuales e integración con el ERP")
public class GestionFacturaController {

    private final AnulacionFacturaService anulacionService;
    private final GeneradorDocumentosFactura documentos;

    public GestionFacturaController(AnulacionFacturaService anulacionService, GeneradorDocumentosFactura documentos) {
        this.anulacionService = anulacionService;
        this.documentos = documentos;
    }

    @PostMapping("/{numeroFactura}/anulacion")
    @AccesoComercial
    @Operation(summary = "Anular una factura pendiente o vencida (auditado, RF-15)")
    public FacturaDetalleResponse anular(@PathVariable String numeroFactura,
                                         @Valid @RequestBody AnularFacturaRequest solicitud) {
        return anulacionService.anular(numeroFactura, solicitud.motivo());
    }

    @GetMapping("/{numeroFactura}/pdf")
    @AccesoConsultaFacturacion
    @Operation(summary = "Descargar la factura en PDF (formato oficial EPQ)", description = "SWR-27")
    public ResponseEntity<byte[]> pdf(@PathVariable String numeroFactura) {
        return documentos.facturaPdf(numeroFactura).comoDescarga();
    }
}
