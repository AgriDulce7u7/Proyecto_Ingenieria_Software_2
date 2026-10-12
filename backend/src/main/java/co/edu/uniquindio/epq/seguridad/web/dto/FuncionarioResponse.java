package co.edu.uniquindio.epq.seguridad.web.dto;

import java.time.LocalDateTime;

import co.edu.uniquindio.epq.seguridad.dominio.Funcionario;

public record FuncionarioResponse(
        Integer id,
        String usuario,
        String nombreCompleto,
        String correo,
        String rol,
        String descripcionRol,
        Integer municipioId,
        String municipio,
        Integer gestorId,
        boolean activo,
        boolean bloqueado,
        LocalDateTime ultimoAcceso,
        LocalDateTime fechaCreacion) {

    public static FuncionarioResponse de(Funcionario f, LocalDateTime ahora) {
        return new FuncionarioResponse(f.getId(), f.getUsuario(), f.getNombreCompleto(), f.getCorreo(), f.getRol().name(),
                f.getRol().getDescripcion(), f.getMunicipio() == null ? null : f.getMunicipio().getId(),
                f.getMunicipio() == null ? null : f.getMunicipio().getNombre(), f.getGestorId(), f.isActivo(),
                f.controlAcceso().estaBloqueado(ahora), f.getUltimoAcceso(), f.getFechaCreacion());
    }
}
