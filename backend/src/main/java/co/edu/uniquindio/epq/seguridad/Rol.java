package co.edu.uniquindio.epq.seguridad;

import java.util.Arrays;
import java.util.List;

/**
 * Roles del SIGCA-EPQ (Tabla 7 del SyRS) con el resumen de sus permisos (RBAC, RF-14, RNF-05).
 * El rol CIUDADANO corresponde al usuario del portal de autogestión; los demás son funcionarios.
 */
public enum Rol {

    ADMINISTRADOR("Administrador del Sistema",
            List.of("Gestionar cuentas y roles de funcionarios", "Consultar el log de auditoría",
                    "Ejecutar procesos de facturación", "Acceso a la gestión comercial")),
    ASESOR_COMERCIAL("Asesor de Comercialización",
            List.of("Registrar y actualizar usuarios del servicio", "Gestionar contratos",
                    "Registrar pagos presenciales", "Generar y anular facturas")),
    GESTOR_PQR("Gestor de PQR",
            List.of("Atender, responder y cerrar las PQR asignadas", "Adjuntar evidencias")),
    JEFE_PQR("Jefe de la Oficina de PQR",
            List.of("Supervisar las alertas de vencimiento del equipo", "Reasignar PQR",
                    "Consultar indicadores de cumplimiento para la SSPD")),
    GESTOR_CARTERA("Gestor de Cartera",
            List.of("Reporte de cartera morosa", "Registrar acuerdos de pago", "Gestionar candidatos a suspensión")),
    GERENTE("Gerente",
            List.of("Dashboard gerencial", "Reporte de cartera morosa", "Indicadores de PQR")),
    DIRECTIVO("Directivo",
            List.of("Dashboard gerencial", "Indicadores de PQR")),
    CIUDADANO("Usuario del servicio (portal de autogestión)",
            List.of("Consultar facturas e historial de consumos", "Pagar facturas en línea"));

    private final String descripcion;
    private final List<String> permisos;

    Rol(String descripcion, List<String> permisos) {
        this.descripcion = descripcion;
        this.permisos = permisos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public List<String> getPermisos() {
        return permisos;
    }

    public boolean esInterno() {
        return this != CIUDADANO;
    }

    /** Los roles de la Oficina de PQR deben estar vinculados a un registro de gestor. */
    public boolean requiereGestorPqr() {
        return this == GESTOR_PQR || this == JEFE_PQR;
    }

    public static List<Rol> internos() {
        return Arrays.stream(values()).filter(Rol::esInterno).toList();
    }
}
