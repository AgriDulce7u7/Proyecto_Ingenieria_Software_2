package co.edu.uniquindio.epq.seguridad.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.epq.seguridad.servicio.ServicioAutenticacion;
import co.edu.uniquindio.epq.seguridad.web.dto.ActivarCuentaCiudadanoRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.LoginCiudadanoRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.LoginRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.SesionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "IS-SIS-01: inicio y cierre de sesión (JWT)")
public class AutenticacionController {

    private final ServicioAutenticacion servicio;

    public AutenticacionController(ServicioAutenticacion servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/login")
    @Operation(summary = "Inicio de sesión de funcionarios", description = "3 intentos fallidos bloquean la cuenta 15 minutos")
    public SesionResponse login(@Valid @RequestBody LoginRequest solicitud) {
        return servicio.iniciarSesionFuncionario(solicitud);
    }

    @PostMapping("/ciudadano/login")
    @Operation(summary = "Inicio de sesión en el portal de autogestión (número de identificación + contraseña)",
            description = "RF-07, CU-05")
    public SesionResponse loginCiudadano(@Valid @RequestBody LoginCiudadanoRequest solicitud) {
        return servicio.iniciarSesionCiudadano(solicitud);
    }

    @PostMapping("/ciudadano/activacion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Crear la contraseña del portal para un usuario del servicio ya registrado (autorregistro)")
    public void activarCuentaCiudadano(@Valid @RequestBody ActivarCuentaCiudadanoRequest solicitud) {
        servicio.activarCuentaCiudadano(solicitud);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cierre de sesión (queda en el log de auditoría; el cliente descarta el token)")
    public void logout() {
        servicio.cerrarSesion();
    }

    @GetMapping("/perfil")
    @Operation(summary = "Datos del usuario autenticado (rol y permisos para armar el menú)")
    public SesionResponse.PerfilResponse perfil() {
        return servicio.perfilActual();
    }
}
