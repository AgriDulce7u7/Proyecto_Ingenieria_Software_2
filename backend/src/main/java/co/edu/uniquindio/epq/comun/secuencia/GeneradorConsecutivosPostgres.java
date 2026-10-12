package co.edu.uniquindio.epq.comun.secuencia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Consecutivos basados en secuencias de PostgreSQL: atómicos y seguros ante concurrencia,
 * incluso con varias instancias del backend.
 */
@Component
public class GeneradorConsecutivosPostgres implements GeneradorConsecutivos {

    private final JdbcTemplate jdbcTemplate;

    public GeneradorConsecutivosPostgres(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long siguiente(SecuenciaNegocio secuencia) {
        Long valor = jdbcTemplate.queryForObject("SELECT nextval('" + secuencia.nombreSecuencia() + "')", Long.class);
        if (valor == null) {
            throw new IllegalStateException("La secuencia " + secuencia.nombreSecuencia() + " no retornó valor");
        }
        return valor;
    }
}
