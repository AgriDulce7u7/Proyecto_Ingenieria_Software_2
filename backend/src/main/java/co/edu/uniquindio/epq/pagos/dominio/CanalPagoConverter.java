package co.edu.uniquindio.epq.pagos.dominio;

import co.edu.uniquindio.epq.comun.persistencia.ConvertidorCodigoPersistible;
import jakarta.persistence.Converter;

@Converter
public class CanalPagoConverter extends ConvertidorCodigoPersistible<CanalPago> {

    public CanalPagoConverter() {
        super(CanalPago.class);
    }
}
