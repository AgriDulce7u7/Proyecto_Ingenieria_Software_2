package co.edu.uniquindio.epq.pqr.notificacion;

/**
 * Puerto de salida para el envío de notificaciones (IS-SIS-02).
 *
 * <p>Patrón <b>Adapter</b>: cada implementación adapta un proveedor concreto (SMTP, SMS,
 * servicio del MinTIC...) a esta interfaz. El dominio no conoce el proveedor (DIP).</p>
 *
 * @return {@code true} si el mensaje se entregó correctamente al proveedor
 */
public interface CanalNotificacion {

    boolean enviar(MensajeNotificacion mensaje);
}
