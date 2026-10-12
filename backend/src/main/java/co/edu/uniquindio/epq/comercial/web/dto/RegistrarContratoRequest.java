package co.edu.uniquindio.epq.comercial.web.dto;

import java.time.LocalDate;

import co.edu.uniquindio.epq.comercial.dominio.TipoServicio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * CU-02 / RF-04: contrato vinculado a un usuario y un predio con número de medidor y tarifa aplicable.
 *
 * @param numeroMedidor obligatorio para activar contratos de acueducto y gas (SWR-19)
 * @param activar       por defecto {@code true}: se activa de inmediato si cumple las reglas
 */
public record RegistrarContratoRequest(
        @NotBlank(message = "El código del usuario es obligatorio") String codigoUsuario,
        @NotNull(message = "El predio es obligatorio") Integer predioId,
        @NotNull(message = "El tipo de servicio es obligatorio (ACUEDUCTO, ALCANTARILLADO o GAS)") TipoServicio servicio,
        @Pattern(regexp = "[A-Za-z0-9-]{4,50}", message = "El número de medidor debe tener entre 4 y 50 caracteres")
        String numeroMedidor,
        LocalDate fechaInstalacionMedidor,
        LocalDate fechaInicio,
        Boolean activar) {
}
