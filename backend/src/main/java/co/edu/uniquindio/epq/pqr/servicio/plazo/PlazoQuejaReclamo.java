package co.edu.uniquindio.epq.pqr.servicio.plazo;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitudTipo;

/** RN-04: quejas y reclamos deben responderse en 15 días hábiles (Ley 142 de 1994). */
@Component
public class PlazoQuejaReclamo implements PoliticaPlazoRespuesta {

    static final int DIAS_HABILES = 15;

    @Override
    public boolean aplicaA(TipoSolicitudTipo tipo) {
        return tipo == TipoSolicitudTipo.QUEJA || tipo == TipoSolicitudTipo.RECLAMO;
    }

    @Override
    public int diasHabiles() {
        return DIAS_HABILES;
    }
}
