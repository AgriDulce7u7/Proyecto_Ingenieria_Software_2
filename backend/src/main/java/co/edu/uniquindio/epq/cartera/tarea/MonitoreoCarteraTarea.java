package co.edu.uniquindio.epq.cartera.tarea;

import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.cartera.servicio.MonitoreoCarteraService;
import co.edu.uniquindio.epq.comun.tarea.TareaProgramadaTemplate;

/**
 * Ejecuta diariamente el monitoreo de cartera (Template Method de las tareas programadas).
 */
@Component
public class MonitoreoCarteraTarea extends TareaProgramadaTemplate {

    private final MonitoreoCarteraService monitoreoService;

    public MonitoreoCarteraTarea(MonitoreoCarteraService monitoreoService) {
        this.monitoreoService = monitoreoService;
    }

    @Scheduled(cron = "${app.cartera.monitoreo-cron:0 30 2 * * *}", zone = "${app.zona-horaria:America/Bogota}")
    public void programar() {
        ejecutar();
    }

    @Override
    protected String nombre() {
        return "Monitoreo de cartera";
    }

    @Override
    protected String ejecutarTarea() {
        return monitoreoService.ejecutar().pasos().stream()
                .map(p -> p.nombre() + ": " + p.detalle())
                .collect(Collectors.joining(" | "));
    }
}
