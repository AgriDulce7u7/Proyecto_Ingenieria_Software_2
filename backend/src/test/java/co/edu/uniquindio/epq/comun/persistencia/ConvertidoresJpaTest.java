package co.edu.uniquindio.epq.comun.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import co.edu.uniquindio.epq.facturacion.dominio.EstadoContrato;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoContratoConverter;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoFactura;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoFacturaConverter;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoLote;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoLoteConverter;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoSincronizacion;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoSincronizacionConverter;
import co.edu.uniquindio.epq.pqr.dominio.TipoNotificacion;
import co.edu.uniquindio.epq.pqr.dominio.TipoNotificacionConverter;

@DisplayName("Conversores JPA - enums y periodos hacia columnas de texto")
class ConvertidoresJpaTest {

    // ------------------------------------------------------------------ enums con código

    @Nested
    @DisplayName("ConvertidorCodigoPersistible (todos los enums)")
    class Enums {

        /** Cada conversor concreto junto con su enum. */
        static Stream<Arguments> conversores() {
            return Stream.of(
                    Arguments.of(new EstadoContratoConverter(), EstadoContrato.class),
                    Arguments.of(new EstadoFacturaConverter(), EstadoFactura.class),
                    Arguments.of(new EstadoLoteConverter(), EstadoLote.class),
                    Arguments.of(new EstadoSincronizacionConverter(), EstadoSincronizacion.class),
                    Arguments.of(new TipoNotificacionConverter(), TipoNotificacion.class));
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource("conversores")
        @DisplayName("Ida y vuelta: cada constante se guarda por su código y se recupera igual")
        <E extends Enum<E> & CodigoPersistible> void idaYVuelta(ConvertidorCodigoPersistible<E> conversor, Class<E> tipo) {
            for (E valor : tipo.getEnumConstants()) {
                String columna = conversor.convertToDatabaseColumn(valor);
                assertEquals(valor.codigo(), columna);
                assertEquals(valor, conversor.convertToEntityAttribute(columna));
            }
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource("conversores")
        @DisplayName("null se convierte en null en ambos sentidos")
        <E extends Enum<E> & CodigoPersistible> void nulos(ConvertidorCodigoPersistible<E> conversor, Class<E> tipo) {
            assertNull(conversor.convertToDatabaseColumn(null));
            assertNull(conversor.convertToEntityAttribute(null));
        }

        @Test
        @DisplayName("Ignora espacios alrededor del código leído de la BD")
        void recortaEspacios() {
            assertEquals(EstadoFactura.ERROR_SINCRONIZACION,
                    new EstadoFacturaConverter().convertToEntityAttribute("  error_sincronizacion "));
        }

        @Test
        @DisplayName("Código desconocido: IllegalStateException que indica el valor y el enum")
        void codigoDesconocido() {
            IllegalStateException error = assertThrows(IllegalStateException.class,
                    () -> new EstadoContratoConverter().convertToEntityAttribute("cancelado"));
            assertTrue(error.getMessage().contains("cancelado"));
            assertTrue(error.getMessage().contains("EstadoContrato"));
        }

        @Test
        @DisplayName("El código distingue mayúsculas: 'ACTIVO' no es un código válido")
        void distingueMayusculas() {
            assertThrows(IllegalStateException.class,
                    () -> new EstadoContratoConverter().convertToEntityAttribute("ACTIVO"));
        }
    }

    // ------------------------------------------------------------------ periodo

    @Nested
    @DisplayName("PeriodoConverter")
    class Periodo {

        private final PeriodoConverter conversor = new PeriodoConverter();

        @Test
        @DisplayName("YearMonth ↔ 'YYYY-MM'")
        void idaYVuelta() {
            assertEquals("2026-08", conversor.convertToDatabaseColumn(YearMonth.of(2026, 8)));
            assertEquals(YearMonth.of(2026, 8), conversor.convertToEntityAttribute(" 2026-08 "));
        }

        @Test
        @DisplayName("null se convierte en null en ambos sentidos")
        void nulos() {
            assertNull(conversor.convertToDatabaseColumn(null));
            assertNull(conversor.convertToEntityAttribute(null));
        }

        @Test
        @DisplayName("Valor mal formado en la BD: DateTimeParseException")
        void malFormado() {
            assertThrows(DateTimeParseException.class, () -> conversor.convertToEntityAttribute("08/2026"));
        }
    }
}