package co.edu.uniquindio.epq.comun.tarea;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TareaProgramadaTemplate - esqueleto común de las tareas programadas (Template Method)")
class TareaProgramadaTemplateTest {

    /** Tarea de prueba que registra qué pasos del algoritmo se ejecutaron. */
    private static class TareaEspia extends TareaProgramadaTemplate {

        private final boolean condicion;
        private final RuntimeException fallo;
        private final List<String> pasos = new ArrayList<>();
        private RuntimeException errorRecibido;

        TareaEspia(boolean condicion, RuntimeException fallo) {
            this.condicion = condicion;
            this.fallo = fallo;
        }

        @Override
        protected String nombre() {
            return "Tarea de prueba";
        }

        @Override
        protected boolean debeEjecutarse() {
            pasos.add("condicion");
            return condicion;
        }

        @Override
        protected String ejecutarTarea() {
            pasos.add("tarea");
            if (fallo != null) {
                throw fallo;
            }
            return "ok";
        }

        @Override
        protected void alFallar(RuntimeException ex) {
            pasos.add("alFallar");
            errorRecibido = ex;
        }
    }

    @Test
    @DisplayName("Si se cumple la condición, ejecuta la tarea")
    void ejecutaSiSeCumpleLaCondicion() {
        TareaEspia tarea = new TareaEspia(true, null);

        tarea.ejecutar();

        assertEquals(List.of("condicion", "tarea"), tarea.pasos);
    }

    @Test
    @DisplayName("Si no se cumple la condición, la tarea se omite")
    void omiteSiNoSeCumpleLaCondicion() {
        TareaEspia tarea = new TareaEspia(false, null);

        tarea.ejecutar();

        assertEquals(List.of("condicion"), tarea.pasos);
    }

    @Test
    @DisplayName("Si la tarea falla, llama a alFallar con la excepción y no la propaga al scheduler")
    void aislaLosErrores() {
        IllegalStateException error = new IllegalStateException("fallo");
        TareaEspia tarea = new TareaEspia(true, error);

        assertDoesNotThrow(tarea::ejecutar);

        assertEquals(List.of("condicion", "tarea", "alFallar"), tarea.pasos);
        assertSame(error, tarea.errorRecibido);
    }

    @Test
    @DisplayName("Valores por defecto: se ejecuta siempre y un fallo no se propaga aunque alFallar no se sobrescriba")
    void comportamientoPorDefecto() {
        List<String> pasos = new ArrayList<>();
        TareaProgramadaTemplate tarea = new TareaProgramadaTemplate() {
            @Override
            protected String nombre() {
                return "Tarea mínima";
            }

            @Override
            protected String ejecutarTarea() {
                pasos.add("tarea");
                throw new IllegalStateException("fallo");
            }
        };

        assertDoesNotThrow(tarea::ejecutar);
        assertEquals(List.of("tarea"), pasos);
    }
}