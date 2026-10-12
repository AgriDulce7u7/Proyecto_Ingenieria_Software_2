package co.edu.uniquindio.epq.comercial.dominio;

import java.util.EnumSet;
import java.util.Set;

import co.edu.uniquindio.epq.comun.persistencia.CodigoPersistible;

/**
 * Estados de un contrato de servicio (modelo de dominio: Activo, Suspendido, Cancelado; SWR-20).
 * Patrón <b>State</b>: cada estado declara a cuáles puede pasar.
 *
 * <pre>
 *  PENDIENTE ──activar──▶ ACTIVO ◀──reactivar── SUSPENDIDO
 *      │                    │ └──suspender──▶──┘     │
 *      └──────cancelar──────┴──────▶ CANCELADO ◀─────┘
 * </pre>
 * PENDIENTE = registrado pero aún no activado (p. ej. falta el medidor, SWR-19). Solo ACTIVO se factura.
 */
public enum EstadoContrato implements CodigoPersistible {

    PENDIENTE("pendiente") {
        @Override
        public Set<EstadoContrato> transicionesPermitidas() {
            return EnumSet.of(ACTIVO, CANCELADO);
        }
    },
    ACTIVO("activo") {
        @Override
        public Set<EstadoContrato> transicionesPermitidas() {
            return EnumSet.of(SUSPENDIDO, CANCELADO);
        }
    },
    SUSPENDIDO("suspendido") {
        @Override
        public Set<EstadoContrato> transicionesPermitidas() {
            return EnumSet.of(ACTIVO, CANCELADO);
        }
    },
    CANCELADO("cancelado") {
        @Override
        public Set<EstadoContrato> transicionesPermitidas() {
            return EnumSet.noneOf(EstadoContrato.class);
        }
    };

    private final String codigo;

    EstadoContrato(String codigo) {
        this.codigo = codigo;
    }

    public abstract Set<EstadoContrato> transicionesPermitidas();

    public boolean puedeTransitarA(EstadoContrato destino) {
        return transicionesPermitidas().contains(destino);
    }

    @Override
    public String codigo() {
        return codigo;
    }
}
