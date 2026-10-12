package co.edu.uniquindio.epq.pqr.servicio.asignacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.uniquindio.epq.comun.territorio.Municipio;
import co.edu.uniquindio.epq.pqr.DatosPruebaPqr;
import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.repositorio.GestorRepository;

/** SyR-02b: la PQR se asigna al gestor del municipio; si no hay, se usa la menor carga de la oficina. */
class AsignacionPorMunicipioTest {

    private final GestorRepository gestorRepository = mock(GestorRepository.class);
    private final AsignacionPorMenorCarga respaldo = mock(AsignacionPorMenorCarga.class);
    private final AsignacionPorMunicipio estrategia = new AsignacionPorMunicipio(gestorRepository, respaldo);

    private final Municipio salento = municipio(7, "Salento");
    private final Gestor andres = DatosPruebaPqr.gestor(2, "Andres Ocampo", true);
    private final Gestor laura = DatosPruebaPqr.gestor(1, "Laura Giraldo", true);

    @Test
    void asignaAlGestorDelMunicipioDeLaPqr() {
        ReflectionTestUtils.setField(andres, "municipio", salento);
        Pqr pqr = pqrEn(salento);
        when(gestorRepository.buscarGestorDelMunicipioConMenorCarga(eq(7), anyCollection()))
                .thenReturn(Optional.of(andres));

        assertSame(andres, estrategia.seleccionarPara(pqr).orElseThrow());
        assertEquals("Asignación automática al gestor del municipio Salento", estrategia.motivo(pqr, andres));
        verify(respaldo, never()).seleccionarPara(any());
    }

    @Test
    void sinGestorEnElMunicipioUsaLaMenorCargaDeLaOficina() {
        Pqr pqr = pqrEn(salento);
        when(gestorRepository.buscarGestorDelMunicipioConMenorCarga(eq(7), anyCollection()))
                .thenReturn(Optional.empty());
        when(respaldo.seleccionarPara(pqr)).thenReturn(Optional.of(laura));

        assertSame(laura, estrategia.seleccionarPara(pqr).orElseThrow());
        assertTrue(estrategia.motivo(pqr, laura).contains("no tiene gestor activo"));
    }

    @Test
    void unaPqrSinMunicipioVaDirectoALaMenorCarga() {
        Pqr pqr = DatosPruebaPqr.pqrRadicada();
        when(respaldo.seleccionarPara(pqr)).thenReturn(Optional.of(laura));

        assertSame(laura, estrategia.seleccionarPara(pqr).orElseThrow());
        verify(gestorRepository, never()).buscarGestorDelMunicipioConMenorCarga(any(), anyCollection());
    }

    private static Pqr pqrEn(Municipio municipio) {
        Pqr pqr = DatosPruebaPqr.pqrRadicada();
        ReflectionTestUtils.setField(pqr, "municipio", municipio);
        return pqr;
    }

    private static Municipio municipio(int id, String nombre) {
        Municipio m = BeanUtils.instantiateClass(Municipio.class);
        ReflectionTestUtils.setField(m, "id", id);
        ReflectionTestUtils.setField(m, "nombre", nombre);
        return m;
    }
}
