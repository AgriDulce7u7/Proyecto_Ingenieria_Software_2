package co.edu.uniquindio.epq.pqr.servicio.asignacion;

import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.gestor;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.pqrRadicada;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.repositorio.GestorRepository;

@DisplayName("AsignacionPorMenorCarga - estrategia de asignación")
class AsignacionPorMenorCargaTest {

    private final GestorRepository gestorRepository = mock(GestorRepository.class);
    private final AsignacionPorMenorCarga estrategia = new AsignacionPorMenorCarga(gestorRepository);

    @Test
    @DisplayName("Devuelve el gestor con menor carga contando solo PQR pendientes de respuesta")
    void seleccionaGestorConMenorCarga() {
        Gestor laura = gestor(1, "Laura Giraldo", true);
        when(gestorRepository.buscarGestorConMenorCarga(EstadoPqrTipo.nombresPendientesDeRespuesta()))
                .thenReturn(Optional.of(laura));

        assertEquals(Optional.of(laura), estrategia.seleccionarPara(pqrRadicada()));
        verify(gestorRepository).buscarGestorConMenorCarga(EstadoPqrTipo.nombresPendientesDeRespuesta());
    }

    @Test
    @DisplayName("Sin gestores activos devuelve vacío")
    void sinGestoresDisponibles() {
        when(gestorRepository.buscarGestorConMenorCarga(EstadoPqrTipo.nombresPendientesDeRespuesta()))
                .thenReturn(Optional.empty());

        assertTrue(estrategia.seleccionarPara(pqrRadicada()).isEmpty());
    }
}