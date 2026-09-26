package co.edu.uniquindio.epq.facturacion.dominio;

import co.edu.uniquindio.epq.comun.persistencia.ConvertidorCodigoPersistible;
import jakarta.persistence.Converter;

@Converter
public class EstadoSincronizacionConverter extends ConvertidorCodigoPersistible<EstadoSincronizacion> {

    public EstadoSincronizacionConverter() {
        super(EstadoSincronizacion.class);
    }
}
