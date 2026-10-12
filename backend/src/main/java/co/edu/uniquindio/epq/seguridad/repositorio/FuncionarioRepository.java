package co.edu.uniquindio.epq.seguridad.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.epq.seguridad.Rol;
import co.edu.uniquindio.epq.seguridad.dominio.Funcionario;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Integer> {

    @EntityGraph(attributePaths = "municipio")
    Optional<Funcionario> findByUsuarioIgnoreCase(String usuario);

    boolean existsByUsuarioIgnoreCase(String usuario);

    boolean existsByCorreoIgnoreCase(String correo);

    Optional<Funcionario> findByGestorId(Integer gestorId);

    @EntityGraph(attributePaths = "municipio")
    List<Funcionario> findAllByOrderByNombreCompletoAsc();

    List<Funcionario> findByRolAndActivoTrue(Rol rol);

    long countByRolAndActivoTrue(Rol rol);
}
