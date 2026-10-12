package co.edu.uniquindio.epq.seguridad.acceso;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * SWR-33: dashboard gerencial, solo Gerente y Directivo.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasAnyRole('GERENTE','DIRECTIVO')")
public @interface AccesoGerencial {
}
