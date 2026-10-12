package co.edu.uniquindio.epq.comercial.web;

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

import co.edu.uniquindio.epq.comercial.servicio.ConsultaUsuariosService;
import co.edu.uniquindio.epq.comercial.servicio.GestionUsuariosService;
import co.edu.uniquindio.epq.comercial.web.dto.ActualizarUsuarioRequest;
import co.edu.uniquindio.epq.comercial.web.dto.MotivoRequest;
import co.edu.uniquindio.epq.comercial.web.dto.PredioResponse;
import co.edu.uniquindio.epq.comercial.web.dto.RegistrarPredioRequest;
import co.edu.uniquindio.epq.comercial.web.dto.RegistrarUsuarioRequest;
import co.edu.uniquindio.epq.comercial.web.dto.UsuarioDetalleResponse;
import co.edu.uniquindio.epq.comercial.web.dto.UsuarioRegistradoResponse;
import co.edu.uniquindio.epq.comercial.web.dto.UsuarioResumenResponse;
import co.edu.uniquindio.epq.seguridad.acceso.AccesoComercial;
import co.edu.uniquindio.epq.seguridad.acceso.AccesoInterno;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios-servicio")
@Tag(name = "F-03 Usuarios del servicio", description = "CU-01 Gestionar usuario (RF-01, RF-02, RF-03)")
public class UsuarioServicioController {

    private final GestionUsuariosService gestionService;
    private final ConsultaUsuariosService consultaService;

    public UsuarioServicioController(GestionUsuariosService gestionService, ConsultaUsuariosService consultaService) {
        this.gestionService = gestionService;
        this.consultaService = consultaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoComercial
    @Operation(summary = "Registrar un usuario del servicio con su predio",
            description = "SWR-12 identificación única (409 con el perfil existente), SWR-13 datos obligatorios, "
                    + "SWR-14 continúa con advertencia si la RNEC no está disponible")
    public UsuarioRegistradoResponse registrar(@Valid @RequestBody RegistrarUsuarioRequest solicitud) {
        return gestionService.registrar(solicitud);
    }

    @GetMapping
    @AccesoInterno
    @Operation(summary = "Buscar usuarios por identificación, nombre o dirección del predio", description = "RF-03, SWR-16")
    public List<UsuarioResumenResponse> buscar(@RequestParam(required = false) String identificacion,
                                               @RequestParam(required = false) String nombre,
                                               @RequestParam(required = false) String direccion) {
        return consultaService.buscar(identificacion, nombre, direccion);
    }

    @GetMapping("/{codigo}")
    @AccesoInterno
    @Operation(summary = "Perfil del usuario con sus predios y contratos")
    public UsuarioDetalleResponse obtener(@PathVariable String codigo) {
        return consultaService.obtener(codigo);
    }

    @PutMapping("/{codigo}")
    @AccesoComercial
    @Operation(summary = "Actualizar datos del usuario (auditado campo por campo)", description = "RF-02, SWR-15")
    public UsuarioDetalleResponse actualizar(@PathVariable String codigo,
                                             @Valid @RequestBody ActualizarUsuarioRequest solicitud) {
        return gestionService.actualizar(codigo, solicitud);
    }

    @PatchMapping("/{codigo}/desactivacion")
    @AccesoComercial
    @Operation(summary = "Desactivar (eliminación lógica) un usuario sin contratos vigentes")
    public UsuarioDetalleResponse desactivar(@PathVariable String codigo, @Valid @RequestBody MotivoRequest solicitud) {
        return gestionService.desactivar(codigo, solicitud.motivo());
    }

    @PatchMapping("/{codigo}/reactivacion")
    @AccesoComercial
    @Operation(summary = "Reactivar un usuario desactivado")
    public UsuarioDetalleResponse reactivar(@PathVariable String codigo, @Valid @RequestBody MotivoRequest solicitud) {
        return gestionService.reactivar(codigo, solicitud.motivo());
    }

    @PostMapping("/{codigo}/validacion-rnec")
    @AccesoComercial
    @Operation(summary = "Reintentar la validación de identidad contra la RNEC", description = "IS-03, SWR-14")
    public UsuarioDetalleResponse revalidar(@PathVariable String codigo) {
        return gestionService.revalidarIdentidad(codigo);
    }

    @GetMapping("/{codigo}/predios")
    @AccesoInterno
    @Operation(summary = "Predios registrados por el usuario")
    public List<PredioResponse> predios(@PathVariable String codigo) {
        return consultaService.predios(codigo);
    }

    @PostMapping("/{codigo}/predios")
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoComercial
    @Operation(summary = "Registrar otro predio del usuario")
    public PredioResponse registrarPredio(@PathVariable String codigo, @Valid @RequestBody RegistrarPredioRequest solicitud) {
        return gestionService.registrarPredio(codigo, solicitud);
    }
}
