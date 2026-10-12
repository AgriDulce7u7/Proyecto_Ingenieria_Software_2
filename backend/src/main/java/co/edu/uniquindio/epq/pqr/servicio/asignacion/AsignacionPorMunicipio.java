package co.edu.uniquindio.epq.pqr.servicio.asignacion;

import java.util.Objects;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.repositorio.GestorRepository;

/**
 * SyR-02b / RF-08: asigna la PQR al Gestor de PQR del municipio correspondiente (el de menor carga si hay
 * varios). Si la PQR no tiene municipio o el municipio no tiene gestor activo, delega en la asignación
 * por menor carga de toda la oficina (patrón <b>Strategy</b> con respaldo por composición).
 */
@Component
@Primary
public class AsignacionPorMunicipio implements EstrategiaAsignacionGestor {

    private final GestorRepository gestorRepository;
    private final AsignacionPorMenorCarga respaldo;

    public AsignacionPorMunicipio(GestorRepository gestorRepository, AsignacionPorMenorCarga respaldo) {
        this.gestorRepository = gestorRepository;
        this.respaldo = respaldo;
    }

    @Override
    public Optional<Gestor> seleccionarPara(Pqr pqr) {
        if (pqr.getMunicipio() != null) {
            Optional<Gestor> delMunicipio = gestorRepository.buscarGestorDelMunicipioConMenorCarga(
                    pqr.getMunicipio().getId(), EstadoPqrTipo.nombresPendientesDeRespuesta());
            if (delMunicipio.isPresent()) {
                return delMunicipio;
            }
        }
        return respaldo.seleccionarPara(pqr);
    }

    @Override
    public String motivo(Pqr pqr, Gestor gestor) {
        boolean mismoMunicipio = pqr.getMunicipio() != null && gestor.getMunicipio() != null
                && Objects.equals(pqr.getMunicipio().getId(), gestor.getMunicipio().getId());
        if (mismoMunicipio) {
            return "Asignación automática al gestor del municipio " + pqr.getMunicipio().getNombre();
        }
        return pqr.getMunicipio() == null
                ? "Asignación automática por menor carga (PQR sin municipio)"
                : "Asignación automática por menor carga: el municipio " + pqr.getMunicipio().getNombre()
                  + " no tiene gestor activo";
    }
}
