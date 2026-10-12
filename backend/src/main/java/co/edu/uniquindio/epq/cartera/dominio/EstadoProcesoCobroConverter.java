package co.edu.uniquindio.epq.cartera.dominio;

import co.edu.uniquindio.epq.comun.persistencia.ConvertidorCodigoPersistible;
import jakarta.persistence.Converter;

@Converter
public class EstadoProcesoCobroConverter extends ConvertidorCodigoPersistible<EstadoProcesoCobro> {

    public EstadoProcesoCobroConverter() {
        super(EstadoProcesoCobro.class);
    }
}
