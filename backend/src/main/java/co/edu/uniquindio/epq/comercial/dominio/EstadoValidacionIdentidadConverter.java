package co.edu.uniquindio.epq.comercial.dominio;

import co.edu.uniquindio.epq.comun.persistencia.ConvertidorCodigoPersistible;
import jakarta.persistence.Converter;

@Converter
public class EstadoValidacionIdentidadConverter extends ConvertidorCodigoPersistible<EstadoValidacionIdentidad> {

    public EstadoValidacionIdentidadConverter() {
        super(EstadoValidacionIdentidad.class);
    }
}
