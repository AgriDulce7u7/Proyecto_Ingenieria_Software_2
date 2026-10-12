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
 * Indicadores de recaudo y cartera (últimos 12 meses calendario, incluido el actual).
 */
@Component
public class IndicadoresCartera implements ProveedorIndicadores {

    private static final String SQL_SERIE = """
            WITH meses AS (
                SELECT m::date AS inicio, LEAST((m + interval '1 month')::date, CAST(:hoy AS date) + 1) AS corte
                FROM generate_series(date_trunc('month', CAST(:hoy AS date)) - interval '11 month',
                                     date_trunc('month', CAST(:hoy AS date)), interval '1 month') m)
            SELECT to_char(me.inicio, 'YYYY-MM') AS periodo,
                   COALESCE((SELECT sum(p.valor) FROM pago p
                             WHERE p.fecha_pago >= me.inicio AND p.fecha_pago < me.inicio + interval '1 month'), 0)
                 + COALESCE((SELECT sum(c.valor) FROM cuota_acuerdo c WHERE c.estado = 'pagada'
                             AND c.fecha_pago >= me.inicio AND c.fecha_pago < me.inicio + interval '1 month'), 0) AS recaudo,
                   COALESCE((SELECT sum(f.total) FROM factura f WHERE f.estado_cobro <> 'anulada'
                             AND f.fecha_vencimiento < me.corte
                             AND (f.fecha_pago IS NULL OR f.fecha_pago >= me.corte)), 0) AS cartera_vencida,
                   (SELECT count(*) FROM acuerdo_pago a
                    WHERE a.fecha_acuerdo >= me.inicio AND a.fecha_acuerdo < me.inicio + interval '1 month') AS acuerdos
            FROM meses me ORDER BY 1
            """;

    private static final String SQL_ACTUAL = """
            SELECT (SELECT count(DISTINCT c.cliente_id) FROM factura f JOIN contrato c ON c.id = f.contrato_id
                    WHERE f.estado_cobro = 'vencida' AND f.acuerdo_pago_id IS NULL) AS usuarios_mora,
                   (SELECT count(*) FROM contrato WHERE candidato_suspension) AS candidatos,
                   (SELECT count(*) FROM acuerdo_pago WHERE estado = 'vigente') AS acuerdos_vigentes
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public IndicadoresCartera(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int orden() {
        return 30;
    }

    @Override
    public String categoria() {
        return "Recaudo y cartera";
    }

    @Override
    public List<IndicadorResponse> calcular(LocalDate hoy) {
        List<PuntoTendencia> recaudo = new ArrayList<>();
        List<PuntoTendencia> cartera = new ArrayList<>();
        List<PuntoTendencia> acuerdos = new ArrayList<>();
        jdbc.query(SQL_SERIE, new MapSqlParameterSource("hoy", hoy), rs -> {
            String periodo = rs.getString("periodo");
            recaudo.add(new PuntoTendencia(periodo, rs.getBigDecimal("recaudo")));
            cartera.add(new PuntoTendencia(periodo, rs.getBigDecimal("cartera_vencida")));
            acuerdos.add(new PuntoTendencia(periodo, BigDecimal.valueOf(rs.getLong("acuerdos"))));
        });
        long[] actual = jdbc.queryForObject(SQL_ACTUAL, new MapSqlParameterSource(),
                (rs, n) -> new long[]{rs.getLong("usuarios_mora"), rs.getLong("candidatos"), rs.getLong("acuerdos_vigentes")});
        return List.of(
                ConstructorIndicador.desdeSerie("RECAUDO_MENSUAL", "Recaudo del mes", "COP", "SUBIR",
                        "Pagos de facturas y cuotas de acuerdos recibidos en el mes", recaudo),
                ConstructorIndicador.desdeSerie("CARTERA_VENCIDA", "Cartera vencida", "COP", "BAJAR",
                        "Valor de las facturas vencidas sin pagar al cierre de cada mes (incluye deuda en acuerdos)", cartera),
                ConstructorIndicador.conValorActual("ACUERDOS_VIGENTES", "Acuerdos de pago vigentes", "acuerdos", "SUBIR",
                        "Valor actual: acuerdos vigentes. Tendencia: acuerdos registrados por mes",
                        BigDecimal.valueOf(actual[2]), acuerdos),
                ConstructorIndicador.conValorActual("USUARIOS_EN_MORA", "Usuarios en mora", "usuarios", "BAJAR",
                        "Usuarios con facturas vencidas sin acuerdo de pago (foto actual)", BigDecimal.valueOf(actual[0]),
                        List.of()),
                ConstructorIndicador.conValorActual("CANDIDATOS_SUSPENSION", "Candidatos a suspensión", "contratos", "BAJAR",
                        "Contratos con 2 o más períodos consecutivos de mora sin acuerdo (RN-03)",
                        BigDecimal.valueOf(actual[1]), List.of()));
    }
}
