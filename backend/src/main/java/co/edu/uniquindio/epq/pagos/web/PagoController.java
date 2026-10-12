package co.edu.uniquindio.epq.pagos.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.epq.pagos.dominio.CanalPago;
import co.edu.uniquindio.epq.pagos.servicio.ConsultaPagoService;
import co.edu.uniquindio.epq.pagos.servicio.RegistroPagoService;
import co.edu.uniquindio.epq.pagos.web.dto.ComprobantePagoResponse;
import co.edu.uniquindio.epq.pagos.web.dto.IntentoPagoResponse;
import co.edu.uniquindio.epq.pagos.web.dto.PagoResponse;
import co.edu.uniquindio.epq.pagos.web.dto.RegistrarPagoPresencialRequest;
import co.edu.uniquindio.epq.seguridad.acceso.AccesoComercial;
import co.edu.uniquindio.epq.seguridad.acceso.AccesoConsultaFacturacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pagos")
@Tag(name = "F-05 Pagos", description = "CU-04 Registrar pago (RF-06, RN-05)")
public class PagoController {

    private final RegistroPagoService registroService;
    private final ConsultaPagoService consultaService;

    public PagoController(RegistroPagoService registroService, ConsultaPagoService consultaService) {
        this.registroService = registroService;
        this.consultaService = consultaService;
    }

    @PostMapping("/presenciales")
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoComercial
    @Operation(summary = "Registrar un pago presencial o bancario y obtener el comprobante",
            description = "SWR-22 comprobante inmediato, SWR-23 rechaza facturas ya pagadas, SWR-24 canal del pago")
    public ComprobantePagoResponse registrarPresencial(@Valid @RequestBody RegistrarPagoPresencialRequest solicitud) {
        return registroService.registrarPresencial(solicitud);
    }

    @GetMapping
    @AccesoConsultaFacturacion
    @Operation(summary = "Consultar pagos")
    public List<PagoResponse> listar(@RequestParam(required = false) String factura,
                                     @RequestParam(required = false) String contrato,
                                     @RequestParam(required = false) CanalPago canal,
                                     @RequestParam(required = false) LocalDate desde,
                                     @RequestParam(required = false) LocalDate hasta) {
        return consultaService.listar(factura, contrato, canal, desde, hasta);
    }

    @GetMapping("/{codigoPago}/comprobante")
    @AccesoConsultaFacturacion
    @Operation(summary = "Comprobante electrónico de un pago")
    public ComprobantePagoResponse comprobante(@PathVariable String codigoPago) {
        return consultaService.comprobante(codigoPago);
    }

    @GetMapping("/{codigoPago}/comprobante/pdf")
    @AccesoConsultaFacturacion
    @Operation(summary = "Comprobante de pago en PDF")
    public ResponseEntity<byte[]> comprobantePdf(@PathVariable String codigoPago) {
        return consultaService.comprobantePdf(codigoPago).comoDescarga();
    }

    @GetMapping("/intentos")
    @AccesoConsultaFacturacion
    @Operation(summary = "Intentos de pago en línea de una factura (aprobados, rechazados y sin respuesta)")
    public List<IntentoPagoResponse> intentos(@RequestParam String factura) {
        return consultaService.intentosEnLinea(factura);
    }
}
