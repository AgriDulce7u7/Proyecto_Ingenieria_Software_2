package co.edu.uniquindio.epq.cartera.repositorio;

import java.sql.Date;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Modelo de lectura de la cartera morosa (CQRS liviano): una consulta SQL agregada es más eficiente que
 * cargar entidades factura por factura para un reporte (SWR-29).
 */
@Repository
public class ConsultaCarteraJdbc {

    private static final String SQL_MOROSOS = """
            SELECT c.id AS contrato_id, c.numero_contrato, s.nombre AS servicio, c.estado AS estado_contrato,
                   c.candidato_suspension, cl.codigo_interno, cl.tipo_documento, cl.numero_documento, cl.nombre_completo,
                   cl.telefono, cl.correo, m.id AS municipio_id, m.nombre AS municipio,
                   count(f.id) AS facturas_vencidas, sum(f.total) AS deuda,
                   min(f.fecha_vencimiento) AS vencimiento_mas_antiguo,
                   string_agg(f.periodo, ',' ORDER BY f.periodo) AS periodos,
                   EXISTS (SELECT 1 FROM acuerdo_pago a WHERE a.contrato_id = c.id AND a.estado = 'vigente') AS acuerdo_vigente,
                   pc.estado AS estado_proceso_cobro
            FROM factura f
            JOIN contrato c  ON c.id = f.contrato_id
            JOIN cliente cl  ON cl.id = c.cliente_id
            JOIN servicio s  ON s.id = c.servicio_id
            LEFT JOIN predio p    ON p.id = c.predio_id
            LEFT JOIN municipio m ON m.id = p.municipio_id
            LEFT JOIN proceso_cobro pc ON pc.contrato_id = c.id
            WHERE f.estado_cobro = 'vencida' AND f.acuerdo_pago_id IS NULL
              AND (CAST(:municipioId AS INTEGER) IS NULL OR m.id = CAST(:municipioId AS INTEGER))
            GROUP BY c.id, s.nombre, cl.id, m.id, m.nombre, pc.estado
            ORDER BY deuda DESC, c.numero_contrato
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public ConsultaCarteraJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<FilaMorosidad> morosos(Integer municipioId) {
        return jdbc.query(SQL_MOROSOS, new MapSqlParameterSource("municipioId", municipioId), (rs, n) -> {
            Date vencimiento = rs.getDate("vencimiento_mas_antiguo");
            String periodos = rs.getString("periodos");
            return new FilaMorosidad(
                    rs.getInt("contrato_id"),
                    rs.getString("numero_contrato"),
                    rs.getString("servicio"),
                    rs.getString("estado_contrato").toUpperCase(),
                    rs.getBoolean("candidato_suspension"),
                    rs.getString("codigo_interno"),
                    rs.getString("tipo_documento"),
                    rs.getString("numero_documento"),
                    rs.getString("nombre_completo"),
                    rs.getString("telefono"),
                    rs.getString("correo"),
                    (Integer) rs.getObject("municipio_id"),
                    rs.getString("municipio"),
                    rs.getInt("facturas_vencidas"),
                    rs.getBigDecimal("deuda"),
                    vencimiento == null ? null : vencimiento.toLocalDate(),
                    periodos == null ? List.of()
                            : Arrays.stream(periodos.split(",")).map(String::trim).map(YearMonth::parse).toList(),
                    rs.getBoolean("acuerdo_vigente"),
                    rs.getString("estado_proceso_cobro") == null ? null
                            : rs.getString("estado_proceso_cobro").toUpperCase());
        });
    }
}
