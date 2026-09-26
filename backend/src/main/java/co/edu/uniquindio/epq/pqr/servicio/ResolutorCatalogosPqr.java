package co.edu.uniquindio.epq.pqr.servicio;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.pqr.dominio.CanalAtencion;
import co.edu.uniquindio.epq.pqr.dominio.CanalAtencionTipo;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqr;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitud;
import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitudTipo;
import co.edu.uniquindio.epq.pqr.repositorio.CanalAtencionRepository;
import co.edu.uniquindio.epq.pqr.repositorio.EstadoPqrRepository;
import co.edu.uniquindio.epq.pqr.repositorio.GestorRepository;
import co.edu.uniquindio.epq.pqr.repositorio.TipoSolicitudRepository;

/**
 * Traduce los enums del dominio a los registros de las tablas de catálogo (SRP: los servicios
 * no repiten la lógica de búsqueda ni el manejo de catálogos incompletos).
 */
@Component
public class ResolutorCatalogosPqr {

    private static final String AYUDA = " Verifique que se haya ejecutado database/init/02_seed_data.sql";

    private final EstadoPqrRepository estadoRepository;
    private final TipoSolicitudRepository tipoSolicitudRepository;
    private final CanalAtencionRepository canalRepository;
    private final GestorRepository gestorRepository;

    public ResolutorCatalogosPqr(EstadoPqrRepository estadoRepository,
                                 TipoSolicitudRepository tipoSolicitudRepository,
                                 CanalAtencionRepository canalRepository,
                                 GestorRepository gestorRepository) {
        this.estadoRepository = estadoRepository;
        this.tipoSolicitudRepository = tipoSolicitudRepository;
        this.canalRepository = canalRepository;
        this.gestorRepository = gestorRepository;
    }

    public EstadoPqr estado(EstadoPqrTipo tipo) {
        return estadoRepository.findByNombre(tipo.getNombre())
                .orElseThrow(() -> new IllegalStateException("No existe el estado '" + tipo.getNombre() + "'." + AYUDA));
    }

    public TipoSolicitud tipoSolicitud(TipoSolicitudTipo tipo) {
        return tipoSolicitudRepository.findByNombre(tipo.getNombre())
                .orElseThrow(() -> new IllegalStateException("No existe el tipo '" + tipo.getNombre() + "'." + AYUDA));
    }

    public CanalAtencion canal(CanalAtencionTipo tipo) {
        return canalRepository.findByNombre(tipo.getNombre())
                .orElseThrow(() -> new IllegalStateException("No existe el canal '" + tipo.getNombre() + "'." + AYUDA));
    }

    public Gestor gestorActivo(Integer gestorId) {
        Gestor gestor = gestorRepository.findById(gestorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Gestor", gestorId));
        if (!gestor.isActivo()) {
            throw new ReglaNegocioException("El gestor '%s' está inactivo".formatted(gestor.getNombre()));
        }
        return gestor;
    }
}
