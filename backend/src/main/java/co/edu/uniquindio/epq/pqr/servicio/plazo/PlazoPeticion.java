package co.edu.uniquindio.epq.pqr.servicio.plazo;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitudTipo;

/** RN-04: una petición de información debe responderse en 10 días hábiles. */
@Component
public class PlazoPeticion implements PoliticaPlazoRespuesta {

    static final int DIAS_HABILES = 10;

    @Override
    public boolean aplicaA(TipoSolicitudTipo tipo) {
        return tipo == TipoSolicitudTipo.PETICION;
    }

    @Override
    public int diasHabiles() {
        return DIAS_HABILES;
    }
}
