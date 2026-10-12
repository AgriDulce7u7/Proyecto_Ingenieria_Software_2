package co.edu.uniquindio.epq.facturacion.servicio.impl;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.facturacion.servicio.AnulacionFacturaService;
import co.edu.uniquindio.epq.facturacion.servicio.FacturacionMapper;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaDetalleResponse;
import co.edu.uniquindio.epq.seguridad.contexto.ContextoSeguridad;

@Service
public class AnulacionFacturaServiceImpl implements AnulacionFacturaService {

    private final FacturaRepository facturaRepository;
    private final ServicioAuditoria auditoria;
    private final ContextoSeguridad contexto;
    private final Clock clock;

    public AnulacionFacturaServiceImpl(FacturaRepository facturaRepository, ServicioAuditoria auditoria,
                                       ContextoSeguridad contexto, Clock clock) {
        this.facturaRepository = facturaRepository;
        this.auditoria = auditoria;
        this.contexto = contexto;
        this.clock = clock;
    }

    @Override
    @Transactional
    public FacturaDetalleResponse anular(String numeroFactura, String motivo) {
        Factura factura = facturaRepository.buscarParaActualizar(numeroFactura.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura", numeroFactura));
        String estadoAnterior = factura.getEstadoCobro().name();
        factura.anular(motivo, contexto.identificadorActual(), LocalDateTime.now(clock));
        auditoria.registrar(AccionAuditoria.ANULAR_FACTURA, "factura", factura.getNumeroFactura(),
                "Factura anulada (estado anterior %s, total %s). Motivo: %s"
                        .formatted(estadoAnterior, factura.getTotal(), motivo.trim()));
        return FacturacionMapper.aDetalle(facturaRepository.findByNumeroFactura(factura.getNumeroFactura()).orElseThrow());
    }
}
