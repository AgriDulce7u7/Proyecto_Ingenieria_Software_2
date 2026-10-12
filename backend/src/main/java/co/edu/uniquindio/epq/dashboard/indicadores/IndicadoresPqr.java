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
 * Indicadores de atención de PQR, incluido el cumplimiento del plazo normativo (SyR-02f, reporte SSPD).
 */
@Component
public class IndicadoresPqr implements ProveedorIndicadores {

    private static final String SQL_SERIE = """
            WITH meses AS (
                SELECT m AS inicio
                FROM generate_series(date_trunc('month', CAST(:hoy AS date)) - interval '11 month',
                                     date_trunc('month', CAST(:hoy AS date)), interval '1 month') m)
            SELECT to_char(me.inicio, 'YYYY-MM') AS periodo,
                   (SELECT count(*) FROM pqr p
                    WHERE p.fecha_recepcion >= me.inicio AND p.fecha_recepcion < me.inicio + interval '1 month') AS recibidas,
                   (SELECT count(*) FROM pqr p
                    WHERE p.fecha_resolucion >= me.inicio AND p.fecha_resolucion < me.inicio + interval '1 month') AS respondidas,
                   (SELECT count(*) FROM pqr p
                    WHERE p.fecha_resolucion >= me.inicio AND p.fecha_resolucion < me.inicio + interval '1 month'
                      AND p.respondida_en_plazo) AS en_plazo,
                   (SELECT round(avg(EXTRACT(EPOCH FROM (p.fecha_resolucion - p.fecha_recepcion)) / 86400)::numeric, 1)
                    FROM pqr p
                    WHERE p.fecha_resolucion >= me.inicio AND p.fecha_resolucion < me.inicio + interval '1 month') AS dias
            FROM meses me ORDER BY 1
            """;

    private static final String SQL_ACTUAL = """
            SELECT count(*) FILTER (WHERE e.nombre IN ('Radicado', 'En trámite', 'Vencido')) AS abiertas,
                   count(*) FILTER (WHERE e.nombre = 'Vencido') AS vencidas
            FROM pqr p JOIN estado_pqr e ON e.id = p.estado_id
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public IndicadoresPqr(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int orden() {
        return 40;
    }

    @Override
    public String categoria() {
        return "Atención de PQR";
    }

    @Override
    public List<IndicadorResponse> calcular(LocalDate hoy) {
        List<PuntoTendencia> recibidas = new ArrayList<>();
        List<PuntoTendencia> cumplimiento = new ArrayList<>();
        List<PuntoTendencia> dias = new ArrayList<>();
        jdbc.query(SQL_SERIE, new MapSqlParameterSource("hoy", hoy), rs -> {
            String periodo = rs.getString("periodo");
            recibidas.add(new PuntoTendencia(periodo, BigDecimal.valueOf(rs.getLong("recibidas"))));
            cumplimiento.add(new PuntoTendencia(periodo,
                    ConstructorIndicador.porcentaje(rs.getLong("en_plazo"), rs.getLong("respondidas"))));
            BigDecimal promedio = rs.getBigDecimal("dias");
            dias.add(new PuntoTendencia(periodo, promedio == null ? BigDecimal.ZERO : promedio));
        });
        long[] actual = jdbc.queryForObject(SQL_ACTUAL, new MapSqlParameterSource(),
                (rs, n) -> new long[]{rs.getLong("abiertas"), rs.getLong("vencidas")});
        return List.of(
                ConstructorIndicador.desdeSerie("PQR_RECIBIDAS", "PQR recibidas", "PQR", "BAJAR",
                        "PQR radicadas en el mes", recibidas),
                ConstructorIndicador.desdeSerie("PQR_CUMPLIMIENTO_PLAZO", "Respuestas dentro del plazo", "%", "SUBIR",
                        "Porcentaje de las PQR respondidas en el mes que se respondieron dentro del plazo normativo (RN-04)",
                        cumplimiento),
                ConstructorIndicador.desdeSerie("PQR_TIEMPO_RESPUESTA", "Tiempo promedio de respuesta", "días", "BAJAR",
                        "Días calendario promedio entre la radicación y la respuesta", dias),
                ConstructorIndicador.conValorActual("PQR_ABIERTAS", "PQR abiertas", "PQR", "BAJAR",
                        "PQR radicadas, en trámite o vencidas sin respuesta (foto actual)", BigDecimal.valueOf(actual[0]),
                        List.of()),
                ConstructorIndicador.conValorActual("PQR_VENCIDAS", "PQR vencidas sin respuesta", "PQR", "BAJAR",
                        "PQR cuyo plazo venció sin respuesta: reportables a la SSPD", BigDecimal.valueOf(actual[1]),
                        List.of()));
    }
}
