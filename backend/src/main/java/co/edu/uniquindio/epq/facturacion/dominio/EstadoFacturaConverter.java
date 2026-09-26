package co.edu.uniquindio.epq.facturacion.dominio;

import co.edu.uniquindio.epq.comun.persistencia.ConvertidorCodigoPersistible;
import jakarta.persistence.Converter;

@Converter
public class EstadoFacturaConverter extends ConvertidorCodigoPersistible<EstadoFactura> {

    public EstadoFacturaConverter() {
        super(EstadoFactura.class);
    }
}
