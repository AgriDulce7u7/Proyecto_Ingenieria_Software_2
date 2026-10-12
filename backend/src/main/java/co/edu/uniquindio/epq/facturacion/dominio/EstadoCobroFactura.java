package co.edu.uniquindio.epq.facturacion.dominio;

import java.util.EnumSet;
import java.util.Set;

import co.edu.uniquindio.epq.comun.persistencia.CodigoPersistible;

/**
 * Ciclo de cobro de la factura (DE-01). Patrón <b>State</b>.
 *
 * <pre>
 *  PENDIENTE ──registrarPago──▶ PAGADA
 *     │  ▲ └──vencerPlazo──▶ VENCIDA ──registrarPago──▶ PAGADA
 *     │  └────generarAcuerdoPago────┘
 *     └──anular──▶ ANULADA ◀──anular── VENCIDA
 * </pre>
 * Se guarda en {@code factura.estado_cobro}; {@code factura.estado} conserva la sincronización con el ERP.
 */
public enum EstadoCobroFactura implements CodigoPersistible {

    PENDIENTE("pendiente") {
        @Override
        public Set<EstadoCobroFactura> transicionesPermitidas() {
            return EnumSet.of(PAGADA, VENCIDA, ANULADA);
        }
    },
    VENCIDA("vencida") {
        @Override
        public Set<EstadoCobroFactura> transicionesPermitidas() {
            return EnumSet.of(PAGADA, PENDIENTE, ANULADA); // PENDIENTE: incluida en un acuerdo de pago
        }
    },
    PAGADA("pagada") {
        @Override
        public Set<EstadoCobroFactura> transicionesPermitidas() {
            return EnumSet.noneOf(EstadoCobroFactura.class);
        }
    },
    ANULADA("anulada") {
        @Override
        public Set<EstadoCobroFactura> transicionesPermitidas() {
            return EnumSet.noneOf(EstadoCobroFactura.class);
        }
    };

    private final String codigo;

    EstadoCobroFactura(String codigo) {
        this.codigo = codigo;
    }

    public abstract Set<EstadoCobroFactura> transicionesPermitidas();

    public boolean puedeTransitarA(EstadoCobroFactura destino) {
        return transicionesPermitidas().contains(destino);
    }

    @Override
    public String codigo() {
        return codigo;
    }
}
