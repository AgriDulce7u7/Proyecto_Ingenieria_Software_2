package co.edu.uniquindio.epq.comercial.web.dto;

public record PredioResponse(Integer id, String codigoPredio, String direccion, Integer municipioId, String municipio,
                             int estrato, String uso) {
}
