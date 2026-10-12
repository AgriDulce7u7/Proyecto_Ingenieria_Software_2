package co.edu.uniquindio.epq.seguridad.acceso;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * SWR-29: reporte de cartera morosa, solo Gestor de Cartera y Gerente.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasAnyRole('GESTOR_CARTERA','GERENTE')")
public @interface AccesoReporteCartera {
}
