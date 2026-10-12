package co.edu.uniquindio.epq.comercial.dominio;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

/**
 * Servicio de dominio que elige la tarifa aplicable a un predio (RN-08, SWR-03, SWR-18):
 * entre las tarifas vigentes del servicio, la más específica que coincida con su municipio y estrato;
 * a igual especificidad, la de vigencia más reciente.
 */
public final class SelectorTarifa {

    private SelectorTarifa() {
    }

    public static Optional<Tarifa> masEspecifica(Collection<Tarifa> vigentes, Predio predio) {
        Integer municipioId = predio == null || predio.getMunicipio() == null ? null : predio.getMunicipio().getId();
        Integer estrato = predio == null ? null : predio.getEstrato();
        return masEspecifica(vigentes, municipioId, estrato);
    }

    public static Optional<Tarifa> masEspecifica(Collection<Tarifa> vigentes, Integer municipioId, Integer estrato) {
        return vigentes.stream()
                .filter(t -> t.esAplicableA(municipioId, estrato))
                .max(Comparator.comparingInt(Tarifa::especificidad)
                        .thenComparing(Tarifa::getFechaVigenciaInicio));
    }
}
