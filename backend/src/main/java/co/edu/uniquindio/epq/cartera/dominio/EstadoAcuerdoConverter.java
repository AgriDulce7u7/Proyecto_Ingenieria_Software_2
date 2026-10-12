package co.edu.uniquindio.epq.cartera.dominio;

import co.edu.uniquindio.epq.comun.persistencia.ConvertidorCodigoPersistible;
import jakarta.persistence.Converter;

@Converter
public class EstadoAcuerdoConverter extends ConvertidorCodigoPersistible<EstadoAcuerdo> {

    public EstadoAcuerdoConverter() {
        super(EstadoAcuerdo.class);
    }
}
