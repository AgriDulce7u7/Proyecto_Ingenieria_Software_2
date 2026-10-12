package co.edu.uniquindio.epq.cartera.servicio.monitoreo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.cartera.dominio.EstadoProcesoCobro;
import co.edu.uniquindio.epq.cartera.dominio.ProcesoCobro;
import co.edu.uniquindio.epq.cartera.repositorio.ConsultaCarteraJdbc;
import co.edu.uniquindio.epq.cartera.repositorio.FilaMorosidad;
import co.edu.uniquindio.epq.cartera.repositorio.ProcesoCobroRepository;
import co.edu.uniquindio.epq.comercial.repositorio.ContratoRepository;

/**
 * Abre el proceso de cobro de los contratos que entraron en mora y cierra el de los que se pusieron al día.
 */
@Component
public class PasoProcesosCobro implements PasoMonitoreoCartera {

    private final ConsultaCarteraJdbc consulta;
    private final ProcesoCobroRepository procesoRepository;
    private final ContratoRepository contratoRepository;

    public PasoProcesosCobro(ConsultaCarteraJdbc consulta, ProcesoCobroRepository procesoRepository,
                             ContratoRepository contratoRepository) {
        this.consulta = consulta;
        this.procesoRepository = procesoRepository;
        this.contratoRepository = contratoRepository;
    }

    @Override
    public int orden() {
        return 30;
    }

    @Override
    public String nombre() {
        return "Procesos de cobro";
    }

    @Override
    @Transactional
    public ResultadoPaso ejecutar(LocalDate hoy, LocalDateTime ahora) {
        Map<Integer, FilaMorosidad> enMora = consulta.morosos(null).stream()
                .collect(Collectors.toMap(FilaMorosidad::contratoId, Function.identity()));
        int abiertos = 0;
        for (FilaMorosidad fila : enMora.values()) {
            if (procesoRepository.findByContratoId(fila.contratoId()).isEmpty()) {
                procesoRepository.save(ProcesoCobro.iniciar(contratoRepository.getReferenceById(fila.contratoId()),
                        EstadoProcesoCobro.ACTIVO, "Facturas vencidas sin pago desde " + fila.vencimientoMasAntiguo(), ahora));
                abiertos++;
            }
        }
        Set<Integer> conDeuda = enMora.keySet();
        int cerrados = 0;
        for (ProcesoCobro proceso : procesoRepository.findByEstadoOrderByFechaInicioAsc(EstadoProcesoCobro.ACTIVO)) {
            if (!conDeuda.contains(proceso.getContrato().getId())) {
                proceso.cerrar("El contrato no tiene facturas vencidas", ahora);
                cerrados++;
            }
        }
        return new ResultadoPaso(abiertos + cerrados, "%d procesos abiertos, %d cerrados".formatted(abiertos, cerrados));
    }
}
