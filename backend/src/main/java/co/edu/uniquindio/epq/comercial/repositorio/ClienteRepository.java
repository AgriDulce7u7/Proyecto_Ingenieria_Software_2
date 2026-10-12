package co.edu.uniquindio.epq.comercial.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.uniquindio.epq.comercial.dominio.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    @EntityGraph(attributePaths = "municipio")
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);

    @EntityGraph(attributePaths = "municipio")
    Optional<Cliente> findByCodigoInterno(String codigoInterno);

    // RF-03 / SWR-16: búsquedas por identificación, nombre o dirección (índices en 03_schema_entrega2.sql)

    @EntityGraph(attributePaths = "municipio")
    List<Cliente> findByNumeroDocumentoStartingWithOrderByNombreCompletoAsc(String prefijo, Pageable limite);

    @EntityGraph(attributePaths = "municipio")
    @Query("""
            select c from Cliente c
            where lower(c.nombreCompleto) like lower(concat('%', :nombre, '%'))
            order by c.nombreCompleto
            """)
    List<Cliente> buscarPorNombre(@Param("nombre") String nombre, Pageable limite);

    @EntityGraph(attributePaths = "municipio")
    @Query("""
            select c from Cliente c
            where lower(c.direccionNotificacion) like lower(concat('%', :direccion, '%'))
               or exists (select p.id from Predio p
                          where p.cliente = c and lower(p.direccion) like lower(concat('%', :direccion, '%')))
               or exists (select k.id from Contrato k
                          where k.cliente = c and lower(k.direccionServicio) like lower(concat('%', :direccion, '%')))
            order by c.nombreCompleto
            """)
    List<Cliente> buscarPorDireccion(@Param("direccion") String direccion, Pageable limite);
}
