package co.edu.uniquindio.epq.comun.excepcion;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduce las excepciones a respuestas HTTP con formato estándar RFC 7807 ({@link ProblemDetail}),
 * para que el frontend reciba siempre la misma estructura de error.
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail manejarRutaInexistente(NoResourceFoundException ex) {
        return problema(HttpStatus.NOT_FOUND, "Ruta no encontrada", "No existe el recurso /" + ex.getResourcePath());
    }

    @ExceptionHandler(TransicionEstadoInvalidaException.class)
    public ProblemDetail manejarTransicionInvalida(TransicionEstadoInvalidaException ex) {
        return problema(HttpStatus.CONFLICT, "Transición de estado inválida", ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail manejarReglaNegocio(ReglaNegocioException ex) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio incumplida", ex.getMessage());
    }

    /** Errores de aplicación que conocen su propio código HTTP (entrega 2). */
    @ExceptionHandler(ExcepcionAplicacion.class)
    public ProblemDetail manejarExcepcionAplicacion(ExcepcionAplicacion ex) {
        ProblemDetail detalle = problema(ex.estadoHttp(), ex.titulo(), ex.getMessage());
        ex.propiedades().forEach(detalle::setProperty);
        return detalle;
    }

    /** {@code @PreAuthorize} rechazó la operación: el usuario no tiene el rol requerido (RNF-05). */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail manejarAccesoDenegado(AccessDeniedException ex) {
        return problema(HttpStatus.FORBIDDEN, "Acceso denegado",
                "Su rol no tiene permiso para realizar esta operación");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail manejarNoAutenticado(AuthenticationException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "No autenticado", "Debe iniciar sesión para continuar");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail manejarArchivoGrande(MaxUploadSizeExceededException ex) {
        return problema(HttpStatus.PAYLOAD_TOO_LARGE, "Archivo demasiado grande",
                "El archivo supera el tamaño máximo permitido");
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    public ProblemDetail manejarParametroFaltante(Exception ex) {
        return problema(HttpStatus.BAD_REQUEST, "Solicitud incompleta", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail detalle = problema(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "Uno o más campos no cumplen las validaciones");
        detalle.setProperty("errores", errores);
        return detalle;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    public ProblemDetail manejarSolicitudMalformada(Exception ex) {
        return problema(HttpStatus.BAD_REQUEST, "Solicitud inválida", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail manejarIntegridad(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos", ex);
        return problema(HttpStatus.CONFLICT, "Conflicto de datos",
                "La operación entra en conflicto con información existente. Intente nuevamente.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail manejarGeneral(Exception ex) {
        log.error("Error no controlado", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado. Contacte al administrador del sistema.");
    }

    private static ProblemDetail problema(HttpStatus estado, String titulo, String detalle) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(estado, detalle);
        problemDetail.setTitle(titulo);
        return problemDetail;
    }
}
