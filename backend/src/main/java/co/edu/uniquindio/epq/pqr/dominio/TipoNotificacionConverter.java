package co.edu.uniquindio.epq.pqr.dominio;

import co.edu.uniquindio.epq.comun.persistencia.ConvertidorCodigoPersistible;
import jakarta.persistence.Converter;

@Converter
public class TipoNotificacionConverter extends ConvertidorCodigoPersistible<TipoNotificacion> {

    public TipoNotificacionConverter() {
        super(TipoNotificacion.class);
    }
}
