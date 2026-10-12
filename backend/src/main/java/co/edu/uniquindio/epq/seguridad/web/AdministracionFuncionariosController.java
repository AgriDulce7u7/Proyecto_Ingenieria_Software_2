package co.edu.uniquindio.epq.seguridad.web;

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

import co.edu.uniquindio.epq.seguridad.Rol;
import co.edu.uniquindio.epq.seguridad.acceso.SoloAdministrador;
import co.edu.uniquindio.epq.seguridad.servicio.AdministracionFuncionariosService;
import co.edu.uniquindio.epq.seguridad.web.dto.ActualizarFuncionarioRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.CambiarEstadoFuncionarioRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.CambiarRolRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.CrearFuncionarioRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.FuncionarioResponse;
import co.edu.uniquindio.epq.seguridad.web.dto.RestablecerContrasenaRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.RolResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "RF-14 Administración", description = "CU-10 Administrar usuarios del sistema (funcionarios y roles)")
@SoloAdministrador
public class AdministracionFuncionariosController {

    private final AdministracionFuncionariosService servicio;

    public AdministracionFuncionariosController(AdministracionFuncionariosService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/roles")
    @Operation(summary = "Roles del sistema y sus permisos")
    public List<RolResponse> roles() {
        return servicio.roles();
    }

    @GetMapping("/funcionarios")
    @Operation(summary = "Listar cuentas de funcionarios")
    public List<FuncionarioResponse> listar(@RequestParam(required = false) Rol rol,
                                            @RequestParam(required = false) Boolean activo) {
        return servicio.listar(rol, activo);
    }

    @GetMapping("/funcionarios/{id}")
    public FuncionarioResponse obtener(@PathVariable Integer id) {
        return servicio.obtener(id);
    }

    @PostMapping("/funcionarios")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear cuenta de funcionario")
    public FuncionarioResponse crear(@Valid @RequestBody CrearFuncionarioRequest solicitud) {
        return servicio.crear(solicitud);
    }

    @PutMapping("/funcionarios/{id}")
    @Operation(summary = "Actualizar datos de un funcionario")
    public FuncionarioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ActualizarFuncionarioRequest solicitud) {
        return servicio.actualizar(id, solicitud);
    }

    @PatchMapping("/funcionarios/{id}/rol")
    @Operation(summary = "Cambiar el rol (auditado, RF-15)")
    public FuncionarioResponse cambiarRol(@PathVariable Integer id, @Valid @RequestBody CambiarRolRequest solicitud) {
        return servicio.cambiarRol(id, solicitud);
    }

    @PatchMapping("/funcionarios/{id}/estado")
    @Operation(summary = "Activar o desactivar una cuenta")
    public FuncionarioResponse cambiarEstado(@PathVariable Integer id,
                                             @Valid @RequestBody CambiarEstadoFuncionarioRequest solicitud) {
        return servicio.cambiarEstado(id, solicitud);
    }

    @PostMapping("/funcionarios/{id}/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Restablecer la contraseña (también desbloquea la cuenta)")
    public void restablecerContrasena(@PathVariable Integer id, @Valid @RequestBody RestablecerContrasenaRequest solicitud) {
        servicio.restablecerContrasena(id, solicitud);
    }
}
