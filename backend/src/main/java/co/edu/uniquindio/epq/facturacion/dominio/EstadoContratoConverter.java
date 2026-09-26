package co.edu.uniquindio.epq.facturacion.dominio;

import co.edu.uniquindio.epq.comun.persistencia.ConvertidorCodigoPersistible;
import jakarta.persistence.Converter;

@Converter
public class EstadoContratoConverter extends ConvertidorCodigoPersistible<EstadoContrato> {

    public EstadoContratoConverter() {
        super(EstadoContrato.class);
    }
}
