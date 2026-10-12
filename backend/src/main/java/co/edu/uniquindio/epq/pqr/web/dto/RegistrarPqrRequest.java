package co.edu.uniquindio.epq.pqr.web.dto;

import co.edu.uniquindio.epq.pqr.dominio.CanalAtencionTipo;
import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitudTipo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Formulario de registro de PQR diligenciado por el ciudadano (CU-06, pasos 2-3).
 */
public record RegistrarPqrRequest(
        @NotBlank(message = "El tipo de documento es obligatorio")
        @Pattern(regexp = "CC|CE|TI|NIT|PA", message = "Tipo de documento válido: CC, CE, TI, NIT o PA")
        String tipoDocumento,

        @NotBlank(message = "El número de documento es obligatorio")
        @Pattern(regexp = "[0-9A-Za-z-]{4,20}", message = "El número de documento debe tener entre 4 y 20 caracteres alfanuméricos")
        String numeroDocumento,

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombreCompleto,

        @NotBlank(message = "El correo electrónico es obligatorio para notificar la radicación")
        @Email(message = "El correo electrónico no es válido")
        @Size(max = 150)
        String correo,

        @Pattern(regexp = "([0-9+ ]{7,20})?", message = "El teléfono debe tener entre 7 y 20 dígitos")
        String telefono,

        @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
        String direccion,

        @NotNull(message = "El tipo de solicitud es obligatorio (PETICION, QUEJA o RECLAMO)")
        TipoSolicitudTipo tipoSolicitud,

        /* Opcional; por defecto WEB (portal de autogestión). */
        CanalAtencionTipo canal,

        @NotBlank(message = "El asunto es obligatorio")
        @Size(max = 200, message = "El asunto no puede superar 200 caracteres")
        String asunto,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 10, max = 5000, message = "La descripción debe tener entre 10 y 5000 caracteres")
        String descripcion,

        /* Entrega 2 (CU-06 paso 2, CO-01): número de contrato, opcional. Si se envía debe existir y estar activo. */
        @Pattern(regexp = "[A-Za-z0-9-]{4,30}", message = "El número de contrato no es válido")
        String numeroContrato,

        /* Entrega 2 (SyR-02b): municipio de la solicitud; si hay contrato se toma el municipio de su predio. */
        Integer municipioId) {

    /** Constructor de la entrega 1 (sin contrato ni municipio), conservado por compatibilidad. */
    public RegistrarPqrRequest(String tipoDocumento, String numeroDocumento, String nombreCompleto, String correo,
                               String telefono, String direccion, TipoSolicitudTipo tipoSolicitud,
                               CanalAtencionTipo canal, String asunto, String descripcion) {
        this(tipoDocumento, numeroDocumento, nombreCompleto, correo, telefono, direccion, tipoSolicitud, canal, asunto,
                descripcion, null, null);
    }
}
