package co.edu.uniquindio.epq.seguridad.servicio.impl;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.comun.notificacion.DestinatarioInterno;
import co.edu.uniquindio.epq.comun.notificacion.DirectorioFuncionarios;
import co.edu.uniquindio.epq.seguridad.Rol;
import co.edu.uniquindio.epq.seguridad.repositorio.FuncionarioRepository;

/**
 * Adaptador del puerto {@link DirectorioFuncionarios} sobre la tabla de funcionarios.
 */
@Component
public class DirectorioFuncionariosJpa implements DirectorioFuncionarios {

    private final FuncionarioRepository repositorio;

    public DirectorioFuncionariosJpa(FuncionarioRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DestinatarioInterno> activosConRol(String rol) {
        return repositorio.findByRolAndActivoTrue(Rol.valueOf(rol)).stream()
                .map(f -> new DestinatarioInterno(f.getId(), f.getNombreCompleto(), f.getCorreo()))
                .toList();
    }
}
