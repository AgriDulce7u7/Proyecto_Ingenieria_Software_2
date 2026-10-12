package co.edu.uniquindio.epq.auditoria.servicio;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;

/**
 * Registro del log de auditoría (RF-15). Todos los módulos dependen de esta interfaz (DIP).
 */
public interface ServicioAuditoria {

    /**
     * Registra una operación exitosa del usuario autenticado, dentro de la transacción en curso:
     * si la operación se revierte, su auditoría también (no hay registros de operaciones que no ocurrieron).
     */
    void registrar(AccionAuditoria accion, String entidad, Object entidadId, String detalle);

    /** Igual que {@link #registrar} pero indicando explícitamente el usuario (p. ej. inicio de sesión). */
    void registrarComo(String usuario, String rol, AccionAuditoria accion, String entidad, Object entidadId,
                       String detalle, boolean exitoso);

    /**
     * Registra en una transacción independiente: queda guardado aunque la operación principal falle
     * (intentos fallidos, pagos rechazados...).
     */
    void registrarIndependiente(String usuario, String rol, AccionAuditoria accion, String entidad, Object entidadId,
                                String detalle, boolean exitoso);
}
