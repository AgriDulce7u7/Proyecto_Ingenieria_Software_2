package co.edu.uniquindio.epq.seguridad.acceso;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Registro de pagos de cuotas de acuerdos: Asesor de Comercialización, Gestor de Cartera o Administrador.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasAnyRole('ASESOR_COMERCIAL','GESTOR_CARTERA','ADMINISTRADOR')")
public @interface AccesoCobro {
}
