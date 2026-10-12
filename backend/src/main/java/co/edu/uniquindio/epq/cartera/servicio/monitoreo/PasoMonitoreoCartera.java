package co.edu.uniquindio.epq.cartera.servicio.monitoreo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Paso del monitoreo diario de cartera. Patrón <b>Chain of Responsibility / Pipeline</b>: los pasos se
 * ejecutan en orden ({@link #orden()}) y cada uno se confirma en su propia transacción, de modo que el
 * siguiente ve los cambios del anterior. Agregar una regla nueva es agregar un paso (OCP).
 */
public interface PasoMonitoreoCartera {

    int orden();

    String nombre();

    ResultadoPaso ejecutar(LocalDate hoy, LocalDateTime ahora);

    record ResultadoPaso(int registrosAfectados, String detalle) {
    }
}
