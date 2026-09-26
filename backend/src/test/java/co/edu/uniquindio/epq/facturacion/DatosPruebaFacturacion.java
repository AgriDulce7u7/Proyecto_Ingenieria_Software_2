package co.edu.uniquindio.epq.facturacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.uniquindio.epq.facturacion.dominio.Cliente;
import co.edu.uniquindio.epq.facturacion.dominio.Contrato;
import co.edu.uniquindio.epq.facturacion.dominio.DetalleLiquidacion;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoContrato;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoFactura;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.dominio.LecturaMedidor;
import co.edu.uniquindio.epq.facturacion.dominio.LoteFacturacion;
import co.edu.uniquindio.epq.facturacion.dominio.Servicio;
import co.edu.uniquindio.epq.facturacion.dominio.Tarifa;

/**
 * Fábrica de entidades del módulo de facturación para pruebas unitarias (Object Mother).
 *
 * <p>Los valores corresponden al contrato CT-ACU-0001 del seed: 21,50 m³ × $3.215,50 + $12.650 = $81.783,25.</p>
 */
public final class DatosPruebaFacturacion {

    public static final YearMonth PERIODO = YearMonth.of(2026, 8);
    public static final String NUMERO_FACTURA = "FAC-202608-000001";
    public static final String NUMERO_CONTRATO = "CT-ACU-0001";
    public static final String DOCUMENTO_CLIENTE = "1094950001";
    public static final LocalDateTime FECHA_GENERACION = LocalDateTime.of(2026, 9, 1, 1, 0);
    public static final LocalDate FECHA_VENCIMIENTO = LocalDate.of(2026, 9, 16);
    public static final DetalleLiquidacion LIQUIDACION = new DetalleLiquidacion(
            new BigDecimal("21.50"), new BigDecimal("69133.25"), new BigDecimal("12650.00"),
            new BigDecimal("81783.25"), new BigDecimal("81783.25"));

    private DatosPruebaFacturacion() {
    }

    public static Cliente cliente() {
        Cliente cliente = BeanUtils.instantiateClass(Cliente.class);
        ReflectionTestUtils.setField(cliente, "id", 1);
        ReflectionTestUtils.setField(cliente, "tipoDocumento", "CC");
        ReflectionTestUtils.setField(cliente, "numeroDocumento", DOCUMENTO_CLIENTE);
        ReflectionTestUtils.setField(cliente, "nombreCompleto", "Carlos Mario Restrepo");
        return cliente;
    }

    public static Servicio servicio(String nombre) {
        Servicio servicio = BeanUtils.instantiateClass(Servicio.class);
        ReflectionTestUtils.setField(servicio, "id", 1);
        ReflectionTestUtils.setField(servicio, "nombre", nombre);
        return servicio;
    }

    public static Contrato contrato(EstadoContrato estado) {
        Contrato contrato = BeanUtils.instantiateClass(Contrato.class);
        ReflectionTestUtils.setField(contrato, "id", 1);
        ReflectionTestUtils.setField(contrato, "numeroContrato", NUMERO_CONTRATO);
        ReflectionTestUtils.setField(contrato, "cliente", cliente());
        ReflectionTestUtils.setField(contrato, "servicio", servicio("Acueducto"));
        ReflectionTestUtils.setField(contrato, "direccionServicio", "Cra 14 # 20-35, Armenia");
        ReflectionTestUtils.setField(contrato, "fechaInicio", LocalDate.of(2022, 3, 10));
        ReflectionTestUtils.setField(contrato, "estado", estado);
        return contrato;
    }

    public static Tarifa tarifaAcueducto() {
        Tarifa tarifa = BeanUtils.instantiateClass(Tarifa.class);
        ReflectionTestUtils.setField(tarifa, "id", 2);
        ReflectionTestUtils.setField(tarifa, "servicio", servicio("Acueducto"));
        ReflectionTestUtils.setField(tarifa, "nombre", "Acueducto residencial 2026");
        ReflectionTestUtils.setField(tarifa, "valorPorUnidad", new BigDecimal("3215.50"));
        ReflectionTestUtils.setField(tarifa, "cargoFijo", new BigDecimal("12650.00"));
        ReflectionTestUtils.setField(tarifa, "fechaVigenciaInicio", LocalDate.of(2026, 1, 1));
        return tarifa;
    }

    public static LecturaMedidor lectura() {
        LecturaMedidor lectura = BeanUtils.instantiateClass(LecturaMedidor.class);
        ReflectionTestUtils.setField(lectura, "id", 1L);
        ReflectionTestUtils.setField(lectura, "periodo", PERIODO);
        ReflectionTestUtils.setField(lectura, "lecturaAnterior", new BigDecimal("1538.00"));
        ReflectionTestUtils.setField(lectura, "lecturaActual", new BigDecimal("1559.50"));
        ReflectionTestUtils.setField(lectura, "consumo", new BigDecimal("21.50"));
        return lectura;
    }

    /** Factura recién emitida (estado GENERADA). */
    public static Factura factura() {
        Factura factura = Factura.emitir(NUMERO_FACTURA, LoteFacturacion.abrir(PERIODO, FECHA_GENERACION),
                contrato(EstadoContrato.ACTIVO), lectura(), tarifaAcueducto(), LIQUIDACION,
                FECHA_GENERACION, FECHA_VENCIMIENTO);
        ReflectionTestUtils.setField(factura, "id", 10L);
        return factura;
    }

    /** Factura en el estado indicado. */
    public static Factura facturaEn(EstadoFactura estado) {
        Factura factura = factura();
        ReflectionTestUtils.setField(factura, "estado", estado);
        return factura;
    }
}