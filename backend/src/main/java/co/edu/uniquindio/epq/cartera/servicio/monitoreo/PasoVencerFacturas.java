package co.edu.uniquindio.epq.cartera.servicio.monitoreo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoCobroFactura;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;

/**
 * DE-01 vencerPlazo(): las facturas pendientes cuya fecha de vencimiento pasó quedan VENCIDAS.
 */
@Component
public class PasoVencerFacturas implements PasoMonitoreoCartera {

    private final FacturaRepository facturaRepository;
    private final ServicioAuditoria auditoria;

    public PasoVencerFacturas(FacturaRepository facturaRepository, ServicioAuditoria auditoria) {
        this.facturaRepository = facturaRepository;
        this.auditoria = auditoria;
    }

    @Override
    public int orden() {
        return 10;
    }

    @Override
    public String nombre() {
        return "Vencimiento de facturas";
    }

    @Override
    @Transactional
    public ResultadoPaso ejecutar(LocalDate hoy, LocalDateTime ahora) {
        List<Factura> vencidas = facturaRepository.buscarPendientesVencidas(EstadoCobroFactura.PENDIENTE, hoy).stream()
                .filter(f -> f.vencerSiCorresponde(hoy))
                .toList();
        if (!vencidas.isEmpty()) {
            auditoria.registrar(AccionAuditoria.VENCER_FACTURAS, "factura", null,
                    "%d facturas pasaron a VENCIDA: %s".formatted(vencidas.size(),
                            String.join(", ", vencidas.stream().map(Factura::getNumeroFactura).limit(50).toList())));
        }
        return new ResultadoPaso(vencidas.size(), vencidas.size() + " facturas vencidas");
    }
}
