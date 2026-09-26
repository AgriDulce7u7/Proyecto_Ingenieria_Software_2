package co.edu.uniquindio.epq.facturacion.servicio.calculo;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.facturacion.dominio.Contrato;

/**
 * Selecciona la estrategia de cálculo adecuada para un contrato. Spring inyecta todas las
 * implementaciones de {@link EstrategiaCalculoFactura}; no hay {@code if/switch} por servicio.
 */
@Component
public class SelectorEstrategiaCalculo {

    private final List<EstrategiaCalculoFactura> estrategias;

    public SelectorEstrategiaCalculo(List<EstrategiaCalculoFactura> estrategias) {
        this.estrategias = estrategias.stream()
                .sorted(Comparator.comparingInt(EstrategiaCalculoFactura::prioridad))
                .toList();
    }

    public EstrategiaCalculoFactura seleccionar(Contrato contrato) {
        return estrategias.stream()
                .filter(estrategia -> estrategia.aplicaA(contrato))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay estrategia de cálculo para el contrato " + contrato.getNumeroContrato()));
    }
}
