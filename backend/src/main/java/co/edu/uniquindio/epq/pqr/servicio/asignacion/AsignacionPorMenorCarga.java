package co.edu.uniquindio.epq.pqr.servicio.asignacion;

import java.util.Optional;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.repositorio.GestorRepository;

/**
 * Asigna la PQR al gestor activo con menos solicitudes pendientes de respuesta,
 * equilibrando la carga de trabajo de la Oficina de PQR.
 */
@Component
public class AsignacionPorMenorCarga implements EstrategiaAsignacionGestor {

    private final GestorRepository gestorRepository;

    public AsignacionPorMenorCarga(GestorRepository gestorRepository) {
        this.gestorRepository = gestorRepository;
    }

    @Override
    public Optional<Gestor> seleccionarPara(Pqr pqr) {
        return gestorRepository.buscarGestorConMenorCarga(EstadoPqrTipo.nombresPendientesDeRespuesta());
    }
}
