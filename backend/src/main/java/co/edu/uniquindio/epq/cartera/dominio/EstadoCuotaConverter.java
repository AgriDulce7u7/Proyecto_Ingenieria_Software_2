package co.edu.uniquindio.epq.cartera.dominio;

import co.edu.uniquindio.epq.comun.persistencia.ConvertidorCodigoPersistible;
import jakarta.persistence.Converter;

@Converter
public class EstadoCuotaConverter extends ConvertidorCodigoPersistible<EstadoCuota> {

    public EstadoCuotaConverter() {
        super(EstadoCuota.class);
    }
}
