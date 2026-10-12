package co.edu.uniquindio.epq.portal.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.epq.facturacion.web.dto.FacturaDetalleResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaResumenResponse;
import co.edu.uniquindio.epq.pagos.servicio.ConsultaPagoService;
import co.edu.uniquindio.epq.pagos.servicio.RegistroPagoService;
import co.edu.uniquindio.epq.pagos.web.dto.ComprobantePagoResponse;
import co.edu.uniquindio.epq.pagos.web.dto.PagoEnLineaRequest;
import co.edu.uniquindio.epq.pagos.web.dto.PagoResponse;
import co.edu.uniquindio.epq.portal.servicio.PortalCiudadanoService;
import co.edu.uniquindio.epq.portal.web.dto.ActualizarContactoRequest;
import co.edu.uniquindio.epq.portal.web.dto.ContratoPortalResponse;
import co.edu.uniquindio.epq.portal.web.dto.FacturaActualResponse;
import co.edu.uniquindio.epq.portal.web.dto.HistorialConsumosResponse;
import co.edu.uniquindio.epq.portal.web.dto.PerfilPortalResponse;
import co.edu.uniquindio.epq.seguridad.acceso.AccesoCiudadano;
import co.edu.uniquindio.epq.seguridad.contexto.ContextoSeguridad;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * IU-01 Portal de autogestión ciudadana (requiere iniciar sesión como ciudadano).
 */
@RestController
@RequestMapping("/api/portal")
@Tag(name = "F-06 Portal ciudadano", description = "CU-05 Consultar factura e historial de consumos; pago en línea (CU-04)")
@AccesoCiudadano
public class PortalCiudadanoController {

    private final PortalCiudadanoService portalService;
    private final RegistroPagoService registroPagoService;
    private final ConsultaPagoService consultaPagoService;
    private final ContextoSeguridad contexto;

    public PortalCiudadanoController(PortalCiudadanoService portalService, RegistroPagoService registroPagoService,
                                     ConsultaPagoService consultaPagoService, ContextoSeguridad contexto) {
        this.portalService = portalService;
        this.registroPagoService = registroPagoService;
        this.consultaPagoService = consultaPagoService;
        this.contexto = contexto;
    }

    @GetMapping("/perfil")
    @Operation(summary = "Datos del usuario autenticado")
    public PerfilPortalResponse perfil() {
        return portalService.perfil();
    }

    @PutMapping("/perfil/contacto")
    @Operation(summary = "Actualizar datos de contacto")
    public PerfilPortalResponse actualizarContacto(@Valid @RequestBody ActualizarContactoRequest solicitud) {
        return portalService.actualizarContacto(solicitud);
    }

    @GetMapping("/contratos")
    @Operation(summary = "Contratos del usuario con su saldo pendiente", description = "CU-05 paso 2")
    public List<ContratoPortalResponse> contratos() {
        return portalService.contratos();
    }

    @GetMapping("/contratos/{numeroContrato}/factura-actual")
    @Operation(summary = "Factura del período actual o fecha estimada de disponibilidad", description = "SWR-25, SWR-28")
    public FacturaActualResponse facturaActual(@PathVariable String numeroContrato) {
        return portalService.facturaActual(numeroContrato);
    }

    @GetMapping("/contratos/{numeroContrato}/historial-consumos")
    @Operation(summary = "Historial de consumos (gráfica y tabla) de hasta 24 meses", description = "SWR-26")
    public HistorialConsumosResponse historial(@PathVariable String numeroContrato,
                                               @RequestParam(defaultValue = "24") int meses) {
        return portalService.historialConsumos(numeroContrato, meses);
    }

    @GetMapping("/facturas")
    @Operation(summary = "Facturas del usuario (opcionalmente de un contrato)")
    public List<FacturaResumenResponse> facturas(@RequestParam(required = false) String contrato) {
        return portalService.facturas(contrato);
    }

    @GetMapping("/facturas/{numeroFactura}")
    @Operation(summary = "Detalle de una factura del usuario", description = "SWR-25")
    public FacturaDetalleResponse factura(@PathVariable String numeroFactura) {
        return portalService.factura(numeroFactura);
    }

    @GetMapping("/facturas/{numeroFactura}/pdf")
    @Operation(summary = "Descargar la factura en PDF (formato oficial EPQ)", description = "SWR-27")
    public ResponseEntity<byte[]> facturaPdf(@PathVariable String numeroFactura) {
        return portalService.facturaPdf(numeroFactura).comoDescarga();
    }

    @PostMapping("/facturas/{numeroFactura}/pago-en-linea")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Pagar la factura en línea a través de la pasarela de pagos",
            description = "DS-03. 402 si la pasarela rechaza, 504 si no responde; en ambos casos la factura no cambia (RN-05). "
                    + "Tarjeta aprobada de prueba: 4111111111111111")
    public ComprobantePagoResponse pagarEnLinea(@PathVariable String numeroFactura,
                                                @Valid @RequestBody PagoEnLineaRequest solicitud) {
        return registroPagoService.pagarEnLinea(numeroFactura, solicitud);
    }

    @GetMapping("/pagos")
    @Operation(summary = "Pagos realizados por el usuario")
    public List<PagoResponse> pagos() {
        return portalService.pagos();
    }

    @GetMapping("/pagos/{codigoPago}/comprobante")
    @Operation(summary = "Comprobante de un pago del usuario", description = "SWR-22")
    public ComprobantePagoResponse comprobante(@PathVariable String codigoPago) {
        return consultaPagoService.comprobanteDelCiudadano(codigoPago, contexto.requerirUsuario().id());
    }

    @GetMapping("/pagos/{codigoPago}/comprobante/pdf")
    @Operation(summary = "Comprobante de pago en PDF")
    public ResponseEntity<byte[]> comprobantePdf(@PathVariable String codigoPago) {
        return consultaPagoService.comprobantePdfDelCiudadano(codigoPago, contexto.requerirUsuario().id()).comoDescarga();
    }
}
