package co.edu.uniquindio.epq.comun.tarea;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Patrón <b>Template Method</b> para las tareas programadas del sistema.
 *
 * <p>Define el esqueleto común (verificar si debe ejecutarse, medir tiempo, registrar en log y
 * aislar errores para que un fallo no detenga el planificador). Cada tarea concreta solo
 * implementa los pasos que varían: {@link #debeEjecutarse()} y {@link #ejecutarTarea()}.</p>
 */
public abstract class TareaProgramadaTemplate {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** Método plantilla: no se sobrescribe. */
    public final void ejecutar() {
        if (!debeEjecutarse()) {
            log.debug("[{}] Omitida: no se cumplen las condiciones de ejecución", nombre());
            return;
        }
        long inicio = System.nanoTime();
        log.info("[{}] Inicio de ejecución", nombre());
        try {
            String resultado = ejecutarTarea();
            log.info("[{}] Finalizada en {} ms. {}", nombre(),
                    Duration.ofNanos(System.nanoTime() - inicio).toMillis(), resultado);
        } catch (RuntimeException ex) {
            log.error("[{}] Falló la ejecución", nombre(), ex);
            alFallar(ex);
        }
    }

    /** Nombre descriptivo para el log. */
    protected abstract String nombre();

    /** Paso variable: condición de ejecución. Por defecto siempre se ejecuta. */
    protected boolean debeEjecutarse() {
        return true;
    }

    /** Paso variable: trabajo de la tarea. Retorna un resumen para el log. */
    protected abstract String ejecutarTarea();

    /** Gancho opcional ante errores. */
    protected void alFallar(RuntimeException ex) {
        // Sin acción por defecto
    }
}
