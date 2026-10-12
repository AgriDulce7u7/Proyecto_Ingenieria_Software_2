package co.edu.uniquindio.epq.comercial.web.dto;

import co.edu.uniquindio.epq.comercial.dominio.UsoPredio;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarPredioRequest(
        @NotBlank(message = "La dirección es obligatoria") @Size(max = 200) String direccion,
        @NotNull(message = "El municipio es obligatorio") Integer municipioId,
        @NotNull @Min(1) @Max(6) Integer estrato,
        UsoPredio uso) {
}
