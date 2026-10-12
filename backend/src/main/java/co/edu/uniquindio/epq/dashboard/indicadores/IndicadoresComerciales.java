package co.edu.uniquindio.epq.dashboard.indicadores;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.dashboard.web.dto.IndicadorResponse;
import co.edu.uniquindio.epq.dashboard.web.dto.PuntoTendencia;

/**
 * Indicadores de gestión comercial: contratos activos, contratos nuevos y usuarios registrados.
 */
@Component
public class IndicadoresComerciales implements ProveedorIndicadores {

    private static final String SQL_SERIE = """
            WITH meses AS (
                SELECT m::date AS inicio, LEAST((m + interval '1 month')::date, CAST(:hoy AS date) + 1) AS corte
                FROM generate_series(date_trunc('month', CAST(:hoy AS date)) - interval '11 month',
                                     date_trunc('month', CAST(:hoy AS date)), interval '1 month') m)
            SELECT to_char(me.inicio, 'YYYY-MM') AS periodo,
                   (SELECT count(*) FROM contrato c WHERE c.fecha_inicio < me.corte
                      AND (c.estado = 'activo' OR (c.estado IN ('suspendido', 'cancelado')
                           AND (SELECT max(h.fecha) FROM historial_contrato h
                                WHERE h.contrato_id = c.id AND h.estado_nuevo = c.estado) >= me.corte))) AS activos,
                   (SELECT count(*) FROM contrato c
                    WHERE c.fecha_inicio >= me.inicio AND c.fecha_inicio < me.inicio + interval '1 month') AS nuevos,
                   (SELECT count(*) FROM cliente cl
                    WHERE cl.fecha_registro >= me.inicio AND cl.fecha_registro < me.inicio + interval '1 month') AS usuarios
            FROM meses me ORDER BY 1
            """;

    private static final String SQL_ACTUAL = """
            SELECT (SELECT count(*) FROM contrato WHERE estado = 'activo') AS activos,
                   (SELECT count(*) FROM cliente WHERE activo) AS usuarios,
                   (SELECT count(*) FROM cliente WHERE activo AND estado_validacion = 'pendiente') AS pendientes_rnec
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public IndicadoresComerciales(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int orden() {
        return 10;
    }

    @Override
    public String categoria() {
        return "Gestión comercial";
    }

    @Override
    public List<IndicadorResponse> calcular(LocalDate hoy) {
        List<PuntoTendencia> activos = new ArrayList<>();
        List<PuntoTendencia> nuevos = new ArrayList<>();
        List<PuntoTendencia> usuarios = new ArrayList<>();
        jdbc.query(SQL_SERIE, new MapSqlParameterSource("hoy", hoy), rs -> {
            String periodo = rs.getString("periodo");
            activos.add(new PuntoTendencia(periodo, BigDecimal.valueOf(rs.getLong("activos"))));
            nuevos.add(new PuntoTendencia(periodo, BigDecimal.valueOf(rs.getLong("nuevos"))));
            usuarios.add(new PuntoTendencia(periodo, BigDecimal.valueOf(rs.getLong("usuarios"))));
        });
        long[] actual = jdbc.queryForObject(SQL_ACTUAL, new MapSqlParameterSource(),
                (rs, n) -> new long[]{rs.getLong("activos"), rs.getLong("usuarios"), rs.getLong("pendientes_rnec")});
        return List.of(
                ConstructorIndicador.conValorActual("CONTRATOS_ACTIVOS", "Contratos activos", "contratos", "SUBIR",
                        "Contratos en estado activo (se facturan cada mes)", BigDecimal.valueOf(actual[0]), activos),
                ConstructorIndicador.desdeSerie("CONTRATOS_NUEVOS", "Contratos nuevos", "contratos", "SUBIR",
                        "Contratos que iniciaron en el mes", nuevos),
                ConstructorIndicador.conValorActual("USUARIOS_REGISTRADOS", "Usuarios del servicio", "usuarios", "SUBIR",
                        "Valor actual: usuarios activos. Tendencia: usuarios registrados por mes",
                        BigDecimal.valueOf(actual[1]), usuarios),
                ConstructorIndicador.conValorActual("USUARIOS_PENDIENTES_RNEC", "Usuarios pendientes de validación RNEC",
                        "usuarios", "BAJAR", "Registrados con advertencia porque la RNEC no estaba disponible (SWR-14)",
                        BigDecimal.valueOf(actual[2]), List.of()));
    }
}
