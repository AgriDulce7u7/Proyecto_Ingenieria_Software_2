package co.edu.uniquindio.epq.seguridad.acceso;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * SyR-02f: indicadores de cumplimiento del plazo normativo (reporte SSPD).
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasAnyRole('JEFE_PQR','ADMINISTRADOR','GERENTE','DIRECTIVO')")
public @interface AccesoIndicadoresPqr {
}
