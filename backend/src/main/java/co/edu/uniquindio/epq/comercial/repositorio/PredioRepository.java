package co.edu.uniquindio.epq.comercial.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.epq.comercial.dominio.Predio;

public interface PredioRepository extends JpaRepository<Predio, Integer> {

    @EntityGraph(attributePaths = "municipio")
    List<Predio> findByClienteIdOrderByIdAsc(Integer clienteId);

    @EntityGraph(attributePaths = {"municipio", "cliente"})
    Optional<Predio> findWithMunicipioById(Integer id);
}
