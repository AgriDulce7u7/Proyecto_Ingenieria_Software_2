package co.edu.uniquindio.epq.pqr.dominio;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Patrón <b>State</b>: cada estado de la PQR conoce a qué estados puede transitar (DE-02).
 *
 * <pre>
 *  RADICADO ──tomar──▶ EN_TRAMITE ──responder──▶ RESUELTO ──cerrar──▶ CERRADO
 *     │                    │  ▲                     ▲
 *     └──vencerPlazo──▶ VENCIDO ──tomar/responder───┘
 * </pre>
 *
 * <p>El nombre de cada constante coincide con los registros de la tabla {@code estado_pqr}.</p>
 */
public enum EstadoPqrTipo {

    RADICADO("Radicado") {
        @Override
        public Set<EstadoPqrTipo> transicionesPermitidas() {
            return EnumSet.of(EN_TRAMITE, VENCIDO);
        }
    },
    EN_TRAMITE("En trámite") {
        @Override
        public Set<EstadoPqrTipo> transicionesPermitidas() {
            return EnumSet.of(RESUELTO, VENCIDO);
        }
    },
    VENCIDO("Vencido") {
        @Override
        public Set<EstadoPqrTipo> transicionesPermitidas() {
            // Respuesta extemporánea (DE-02) o toma del caso por un gestor
            return EnumSet.of(EN_TRAMITE, RESUELTO);
        }
    },
    RESUELTO("Resuelto") {
        @Override
        public Set<EstadoPqrTipo> transicionesPermitidas() {
            return EnumSet.of(CERRADO);
        }
    },
    CERRADO("Cerrado") {
        @Override
        public Set<EstadoPqrTipo> transicionesPermitidas() {
            return EnumSet.noneOf(EstadoPqrTipo.class);
        }
    };

    private final String nombre;

    EstadoPqrTipo(String nombre) {
        this.nombre = nombre;
    }

    public abstract Set<EstadoPqrTipo> transicionesPermitidas();

    public String getNombre() {
        return nombre;
    }

    public boolean puedeTransitarA(EstadoPqrTipo destino) {
        return transicionesPermitidas().contains(destino);
    }

    /** La PQR aún espera respuesta (cuenta para la carga del gestor). */
    public boolean estaPendienteDeRespuesta() {
        return this == RADICADO || this == EN_TRAMITE || this == VENCIDO;
    }

    /** El plazo normativo sigue corriendo y la PQR puede vencerse. */
    public boolean tienePlazoEnCurso() {
        return this == RADICADO || this == EN_TRAMITE;
    }

    /** Se puede registrar una respuesta formal en este estado. */
    public boolean admiteRespuesta() {
        return this == EN_TRAMITE || this == VENCIDO;
    }

    public static EstadoPqrTipo desdeNombre(String nombre) {
        return Arrays.stream(values())
                .filter(estado -> estado.nombre.equalsIgnoreCase(nombre.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Estado de PQR desconocido: " + nombre));
    }

    public static List<String> nombresConPlazoEnCurso() {
        return Arrays.stream(values()).filter(EstadoPqrTipo::tienePlazoEnCurso).map(EstadoPqrTipo::getNombre).toList();
    }

    public static List<String> nombresPendientesDeRespuesta() {
        return Arrays.stream(values()).filter(EstadoPqrTipo::estaPendienteDeRespuesta).map(EstadoPqrTipo::getNombre).toList();
    }
}
