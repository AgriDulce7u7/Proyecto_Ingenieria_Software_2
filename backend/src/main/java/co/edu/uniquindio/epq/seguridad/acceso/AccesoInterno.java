package co.edu.uniquindio.epq.seguridad.acceso;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Cualquier funcionario autenticado (RF-03: consultas con rol interno).
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasAnyRole('ADMINISTRADOR','ASESOR_COMERCIAL','GESTOR_PQR','JEFE_PQR','GESTOR_CARTERA','GERENTE','DIRECTIVO')")
public @interface AccesoInterno {
}
