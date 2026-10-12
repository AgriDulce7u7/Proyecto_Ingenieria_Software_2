package co.edu.uniquindio.epq.comercial.web.dto;

import co.edu.uniquindio.epq.comercial.dominio.TipoPersona;
import co.edu.uniquindio.epq.comercial.dominio.TipoServicio;
import co.edu.uniquindio.epq.comercial.dominio.UsoPredio;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * CU-01 / RF-01: registro de un usuario del servicio con sus datos personales, el servicio solicitado
 * y los datos del predio. SWR-13: nombre, tipo y número de identificación, dirección del predio,
 * municipio, teléfono y correo son obligatorios.
 */
public record RegistrarUsuarioRequest(
        @NotBlank(message = "El tipo de identificación es obligatorio")
        @Pattern(regexp = "CC|CE|TI|NIT|PA", message = "Tipo de identificación válido: CC, CE, TI, NIT o PA")
        String tipoDocumento,

        @NotBlank(message = "El número de identificación es obligatorio")
        @Pattern(regexp = "[0-9A-Za-z-]{4,20}", message = "El número de identificación debe tener entre 4 y 20 caracteres alfanuméricos")
        String numeroDocumento,

        /* Opcional: si no se envía se deduce del tipo de documento (NIT → JURIDICA). */
        TipoPersona tipoPersona,

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 150)
        String nombreCompleto,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "[0-9+ ]{7,20}", message = "El teléfono debe tener entre 7 y 20 dígitos")
        String telefono,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico no es válido")
        @Size(max = 150)
        String correo,

        @NotNull(message = "El municipio es obligatorio")
        Integer municipioId,

        @NotBlank(message = "La dirección del predio es obligatoria")
        @Size(max = 200)
        String direccionPredio,

        @NotNull(message = "El estrato del predio es obligatorio")
        @Min(value = 1, message = "El estrato debe estar entre 1 y 6")
        @Max(value = 6, message = "El estrato debe estar entre 1 y 6")
        Integer estrato,

        /* Opcional, por defecto RESIDENCIAL. */
        UsoPredio usoPredio,

        /* Opcional: si no se envía se usa la dirección del predio. */
        @Size(max = 200)
        String direccionNotificacion,

        /* Servicio solicitado (RF-01). El contrato se registra después en /api/contratos. */
        TipoServicio servicioSolicitado) {
}
