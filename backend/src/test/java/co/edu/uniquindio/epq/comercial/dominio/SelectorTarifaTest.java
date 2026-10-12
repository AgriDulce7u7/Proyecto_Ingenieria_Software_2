package co.edu.uniquindio.epq.comercial.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.uniquindio.epq.comun.territorio.Municipio;

class SelectorTarifaTest {

    private static final int SALENTO = 7;
    private static final int FILANDIA = 3;

    private final Tarifa general = tarifa(1, null, null, LocalDate.of(2026, 1, 1));
    private final Tarifa generalAntigua = tarifa(2, null, null, LocalDate.of(2025, 1, 1));
    private final Tarifa estrato2 = tarifa(3, null, 2, LocalDate.of(2026, 1, 1));
    private final Tarifa salento = tarifa(4, SALENTO, null, LocalDate.of(2026, 1, 1));
    private final Tarifa salentoEstrato2 = tarifa(5, SALENTO, 2, LocalDate.of(2026, 1, 1));

    private final List<Tarifa> vigentes = List.of(generalAntigua, general, estrato2, salento, salentoEstrato2);

    @Test
    void eligeLaTarifaMasEspecificaQueCoincideConMunicipioYEstrato() {
        assertEquals(5, SelectorTarifa.masEspecifica(vigentes, SALENTO, 2).orElseThrow().getId());
        assertEquals(4, SelectorTarifa.masEspecifica(vigentes, SALENTO, 3).orElseThrow().getId());
        assertEquals(3, SelectorTarifa.masEspecifica(vigentes, FILANDIA, 2).orElseThrow().getId());
    }

    @Test
    void aIgualEspecificidadGanaLaDeVigenciaMasReciente() {
        assertEquals(1, SelectorTarifa.masEspecifica(vigentes, FILANDIA, 4).orElseThrow().getId());
    }

    @Test
    void sinTarifasAplicablesNoRetornaNada() {
        assertTrue(SelectorTarifa.masEspecifica(List.of(salento), FILANDIA, 2).isEmpty());
    }

    private static Tarifa tarifa(int id, Integer municipioId, Integer estrato, LocalDate inicio) {
        Tarifa t = BeanUtils.instantiateClass(Tarifa.class);
        ReflectionTestUtils.setField(t, "id", id);
        ReflectionTestUtils.setField(t, "fechaVigenciaInicio", inicio);
        if (municipioId != null) {
            Municipio m = BeanUtils.instantiateClass(Municipio.class);
            ReflectionTestUtils.setField(m, "id", municipioId);
            ReflectionTestUtils.setField(t, "municipio", m);
        }
        if (estrato != null) {
            ReflectionTestUtils.setField(t, "estrato", estrato.shortValue());
        }
        return t;
    }
}
