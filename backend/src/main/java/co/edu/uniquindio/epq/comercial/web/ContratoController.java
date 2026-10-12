package co.edu.uniquindio.epq.comercial.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.epq.comercial.dominio.EstadoContrato;
import co.edu.uniquindio.epq.comercial.dominio.TipoServicio;
import co.edu.uniquindio.epq.comercial.servicio.ConsultaContratosService;
import co.edu.uniquindio.epq.comercial.servicio.GestionContratosService;
import co.edu.uniquindio.epq.comercial.web.dto.AsignarMedidorRequest;
import co.edu.uniquindio.epq.comercial.web.dto.CambiarEstadoContratoRequest;
import co.edu.uniquindio.epq.comercial.web.dto.ContratoDetalleResponse;
import co.edu.uniquindio.epq.comercial.web.dto.ContratoResumenResponse;
import co.edu.uniquindio.epq.comercial.web.dto.HistorialContratoResponse;
import co.edu.uniquindio.epq.comercial.web.dto.RegistrarContratoRequest;
import co.edu.uniquindio.epq.comercial.web.dto.TarifaResponse;
import co.edu.uniquindio.epq.seguridad.acceso.AccesoComercial;
import co.edu.uniquindio.epq.seguridad.acceso.AccesoInterno;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Tag(name = "F-04 Contratos de servicio", description = "CU-02 Gestionar contrato (RF-04, RN-01, RN-08)")
public class ContratoController {

    private final GestionContratosService gestionService;
    private final ConsultaContratosService consultaService;

    public ContratoController(GestionContratosService gestionService, ConsultaContratosService consultaService) {
        this.gestionService = gestionService;
        this.consultaService = consultaService;
    }

    @PostMapping("/contratos")
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoComercial
    @Operation(summary = "Registrar un contrato de servicio",
            description = "SWR-17 (RN-01) sin duplicados por predio y servicio, SWR-18 tarifa automática, "
                    + "SWR-19 acueducto y gas requieren medidor para activarse")
    public ContratoDetalleResponse registrar(@Valid @RequestBody RegistrarContratoRequest solicitud) {
        return gestionService.registrar(solicitud);
    }

    @GetMapping("/contratos")
    @AccesoInterno
    @Operation(summary = "Buscar contratos")
    public List<ContratoResumenResponse> listar(@RequestParam(required = false) EstadoContrato estado,
                                                @RequestParam(required = false) TipoServicio servicio,
                                                @RequestParam(required = false) String usuario,
                                                @RequestParam(required = false) Integer municipioId,
                                                @RequestParam(required = false) Boolean candidatoSuspension) {
        return consultaService.listar(estado, servicio, usuario, municipioId, candidatoSuspension);
    }

    @GetMapping("/contratos/{numero}")
    @AccesoInterno
    @Operation(summary = "Detalle del contrato (tarifa, medidor, historial y acciones disponibles)")
    public ContratoDetalleResponse obtener(@PathVariable String numero) {
        return consultaService.obtener(numero);
    }

    @PatchMapping("/contratos/{numero}/estado")
    @AccesoComercial
    @Operation(summary = "Cambiar el estado (ACTIVO, SUSPENDIDO, CANCELADO) con motivo", description = "SWR-20")
    public ContratoDetalleResponse cambiarEstado(@PathVariable String numero,
                                                 @Valid @RequestBody CambiarEstadoContratoRequest solicitud) {
        return gestionService.cambiarEstado(numero, solicitud);
    }

    @PutMapping("/contratos/{numero}/medidor")
    @AccesoComercial
    @Operation(summary = "Instalar o reemplazar el medidor del contrato (opcionalmente lo activa)", description = "SWR-19")
    public ContratoDetalleResponse asignarMedidor(@PathVariable String numero,
                                                  @Valid @RequestBody AsignarMedidorRequest solicitud) {
        return gestionService.asignarMedidor(numero, solicitud);
    }

    @GetMapping("/contratos/{numero}/historial")
    @AccesoInterno
    @Operation(summary = "Historial de estados del contrato")
    public List<HistorialContratoResponse> historial(@PathVariable String numero) {
        return consultaService.historial(numero);
    }

    @GetMapping("/tarifas")
    @AccesoInterno
    @Operation(summary = "Tarifas vigentes por servicio")
    public List<TarifaResponse> tarifas(@RequestParam(required = false) TipoServicio servicio,
                                        @RequestParam(required = false) LocalDate fecha) {
        return consultaService.tarifasVigentes(servicio, fecha);
    }

    @GetMapping("/tarifas/aplicable")
    @AccesoInterno
    @Operation(summary = "Tarifa que se aplicaría según servicio, municipio y estrato", description = "SWR-18, RN-08")
    public TarifaResponse tarifaAplicable(@RequestParam TipoServicio servicio, @RequestParam Integer municipioId,
                                          @RequestParam Integer estrato,
                                          @RequestParam(required = false) LocalDate fecha) {
        return consultaService.tarifaAplicable(servicio, municipioId, estrato, fecha);
    }
}
