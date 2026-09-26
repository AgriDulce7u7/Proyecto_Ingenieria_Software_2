package co.edu.uniquindio.epq.pqr;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.uniquindio.epq.pqr.dominio.CanalAtencion;
import co.edu.uniquindio.epq.pqr.dominio.Ciudadano;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqr;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.dominio.PlazoRespuesta;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitud;

/**
 * Fábrica de entidades del módulo PQR para pruebas unitarias (Object Mother).
 *
 * <p>Las entidades del dominio tienen constructores protegidos y no exponen setters, por lo que
 * se instancian por reflexión. Así los tests trabajan con objetos reales del dominio y sus reglas,
 * sin depender de la base de datos.</p>
 */
public final class DatosPruebaPqr {

    public static final String RADICADO = "202609-0001";
    public static final LocalDateTime FECHA_RECEPCION = LocalDateTime.of(2026, 9, 1, 8, 0);

    private DatosPruebaPqr() {
    }

    public static EstadoPqr estado(EstadoPqrTipo tipo) {
        EstadoPqr estado = BeanUtils.instantiateClass(EstadoPqr.class);
        ReflectionTestUtils.setField(estado, "id", tipo.ordinal() + 1);
        ReflectionTestUtils.setField(estado, "nombre", tipo.getNombre());
        return estado;
    }

    public static Gestor gestor(int id, String nombre, boolean activo) {
        Gestor gestor = BeanUtils.instantiateClass(Gestor.class);
        ReflectionTestUtils.setField(gestor, "id", id);
        ReflectionTestUtils.setField(gestor, "nombre", nombre);
        ReflectionTestUtils.setField(gestor, "correo", nombre.toLowerCase().replace(" ", ".") + "@epq.com.co");
        ReflectionTestUtils.setField(gestor, "area", "Oficina de PQR");
        ReflectionTestUtils.setField(gestor, "activo", activo);
        return gestor;
    }

    public static Ciudadano ciudadano() {
        Ciudadano ciudadano = BeanUtils.instantiateClass(Ciudadano.class);
        ReflectionTestUtils.setField(ciudadano, "id", 1);
        ReflectionTestUtils.setField(ciudadano, "tipoDocumento", "CC");
        ReflectionTestUtils.setField(ciudadano, "numeroDocumento", "1094000111");
        ReflectionTestUtils.setField(ciudadano, "nombreCompleto", "Ana Gómez");
        ReflectionTestUtils.setField(ciudadano, "correo", "ana@correo.com");
        return ciudadano;
    }

    public static TipoSolicitud tipoSolicitud(String nombre) {
        TipoSolicitud tipo = BeanUtils.instantiateClass(TipoSolicitud.class);
        ReflectionTestUtils.setField(tipo, "id", 1);
        ReflectionTestUtils.setField(tipo, "nombre", nombre);
        return tipo;
    }

    public static CanalAtencion canalWeb() {
        CanalAtencion canal = BeanUtils.instantiateClass(CanalAtencion.class);
        ReflectionTestUtils.setField(canal, "id", 1);
        ReflectionTestUtils.setField(canal, "nombre", "Web");
        return canal;
    }

    /** PQR recién radicada, sin gestor asignado. */
    public static Pqr pqrRadicada() {
        Pqr pqr = Pqr.radicar(RADICADO, ciudadano(), tipoSolicitud("Reclamo"), canalWeb(),
                estado(EstadoPqrTipo.RADICADO), "Cobro elevado", "El valor facturado duplica mi consumo habitual.",
                FECHA_RECEPCION, new PlazoRespuesta(LocalDateTime.of(2026, 9, 22, 23, 59), LocalDate.of(2026, 9, 22)));
        ReflectionTestUtils.setField(pqr, "id", 100L);
        return pqr;
    }

    /** PQR en el estado indicado y asignada al gestor indicado (puede ser null). */
    public static Pqr pqrEn(EstadoPqrTipo tipo, Gestor gestor) {
        Pqr pqr = pqrRadicada();
        ReflectionTestUtils.setField(pqr, "estado", estado(tipo));
        ReflectionTestUtils.setField(pqr, "gestor", gestor);
        return pqr;
    }
}