package co.edu.uniquindio.epq.pagos.dominio;

import co.edu.uniquindio.epq.comun.persistencia.ConvertidorCodigoPersistible;
import jakarta.persistence.Converter;

@Converter
public class ResultadoIntentoPagoConverter extends ConvertidorCodigoPersistible<ResultadoIntentoPago> {

    public ResultadoIntentoPagoConverter() {
        super(ResultadoIntentoPago.class);
    }
}
