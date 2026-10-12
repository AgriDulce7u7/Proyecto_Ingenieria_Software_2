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
 * Indicadores de facturación por período de consumo (últimos 12 períodos facturables).
 */
@Component
public class IndicadoresFacturacion implements ProveedorIndicadores {

    private static final String SQL = """
            WITH meses AS (
                SELECT to_char(m, 'YYYY-MM') AS periodo
                FROM generate_series(date_trunc('month', CAST(:hoy AS date)) - interval '12 month',
                                     date_trunc('month', CAST(:hoy AS date)) - interval '1 month', interval '1 month') m)
            SELECT me.periodo, COALESCE(sum(f.total), 0) AS facturado, count(f.id) AS facturas,
                   COALESCE(sum(f.consumo), 0) AS consumo, count(f.id) FILTER (WHERE f.estado_cobro = 'pagada') AS pagadas
            FROM meses me LEFT JOIN factura f ON f.periodo = me.periodo AND f.estado_cobro <> 'anulada'
            GROUP BY me.periodo ORDER BY me.periodo
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public IndicadoresFacturacion(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int orden() {
        return 20;
    }

    @Override
    public String categoria() {
        return "Facturación";
    }

    @Override
    public List<IndicadorResponse> calcular(LocalDate hoy) {
        List<PuntoTendencia> facturado = new ArrayList<>();
        List<PuntoTendencia> facturas = new ArrayList<>();
        List<PuntoTendencia> consumo = new ArrayList<>();
        List<PuntoTendencia> pagoOportuno = new ArrayList<>();
        jdbc.query(SQL, new MapSqlParameterSource("hoy", hoy), rs -> {
            String periodo = rs.getString("periodo");
            long emitidas = rs.getLong("facturas");
            facturado.add(new PuntoTendencia(periodo, rs.getBigDecimal("facturado")));
            facturas.add(new PuntoTendencia(periodo, BigDecimal.valueOf(emitidas)));
            consumo.add(new PuntoTendencia(periodo, rs.getBigDecimal("consumo")));
            pagoOportuno.add(new PuntoTendencia(periodo, ConstructorIndicador.porcentaje(rs.getLong("pagadas"), emitidas)));
        });
        return List.of(
                ConstructorIndicador.desdeSerie("FACTURADO_PERIODO", "Valor facturado por período", "COP", "SUBIR",
                        "Suma del total de las facturas no anuladas del período de consumo", facturado),
                ConstructorIndicador.desdeSerie("FACTURAS_EMITIDAS", "Facturas emitidas", "facturas", "SUBIR",
                        "Cantidad de facturas generadas por período (F-01)", facturas),
                ConstructorIndicador.desdeSerie("CONSUMO_FACTURADO", "Consumo facturado", "m³", "SUBIR",
                        "Consumo total facturado en el período", consumo),
                ConstructorIndicador.desdeSerie("FACTURAS_PAGADAS_PCT", "Facturas pagadas del período", "%", "SUBIR",
                        "Porcentaje de las facturas del período que ya están pagadas", pagoOportuno));
    }
}
