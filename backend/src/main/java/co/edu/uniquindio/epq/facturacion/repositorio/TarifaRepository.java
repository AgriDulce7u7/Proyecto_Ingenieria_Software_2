package co.edu.uniquindio.epq.facturacion.repositorio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.uniquindio.epq.facturacion.dominio.Tarifa;

public interface TarifaRepository extends JpaRepository<Tarifa, Integer> {

    /**
     * Tarifas vigentes de un servicio en una fecha, la más reciente primero (SWR-03).
     */
    @Query("""
            select t from Tarifa t
            where t.servicio.id = :servicioId
              and t.fechaVigenciaInicio <= :fecha
              and (t.fechaVigenciaFin is null or t.fechaVigenciaFin >= :fecha)
            order by t.fechaVigenciaInicio desc
            """)
    List<Tarifa> buscarVigentes(@Param("servicioId") Integer servicioId, @Param("fecha") LocalDate fecha);
}
