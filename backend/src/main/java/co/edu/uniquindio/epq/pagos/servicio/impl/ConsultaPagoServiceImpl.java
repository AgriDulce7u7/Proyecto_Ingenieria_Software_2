package co.edu.uniquindio.epq.pagos.servicio.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.exportacion.ArchivoGenerado;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.pagos.documento.ComprobantePagoPdf;
import co.edu.uniquindio.epq.pagos.dominio.CanalPago;
import co.edu.uniquindio.epq.pagos.dominio.Pago;
import co.edu.uniquindio.epq.pagos.repositorio.IntentoPagoEnLineaRepository;
import co.edu.uniquindio.epq.pagos.repositorio.PagoRepository;
import co.edu.uniquindio.epq.pagos.repositorio.PagoSpecifications;
import co.edu.uniquindio.epq.pagos.servicio.ConsultaPagoService;
import co.edu.uniquindio.epq.pagos.servicio.PagosMapper;
import co.edu.uniquindio.epq.pagos.web.dto.ComprobantePagoResponse;
import co.edu.uniquindio.epq.pagos.web.dto.IntentoPagoResponse;
import co.edu.uniquindio.epq.pagos.web.dto.PagoResponse;

@Service
@Transactional(readOnly = true)
public class ConsultaPagoServiceImpl implements ConsultaPagoService {

    private final PagoRepository pagoRepository;
    private final IntentoPagoEnLineaRepository intentoRepository;
    private final FacturaRepository facturaRepository;
    private final Clock clock;

    public ConsultaPagoServiceImpl(PagoRepository pagoRepository, IntentoPagoEnLineaRepository intentoRepository,
                                   FacturaRepository facturaRepository, Clock clock) {
        this.pagoRepository = pagoRepository;
        this.intentoRepository = intentoRepository;
        this.facturaRepository = facturaRepository;
        this.clock = clock;
    }

    @Override
    public List<PagoResponse> listar(String numeroFactura, String numeroContrato, CanalPago canal, LocalDate desde,
                                     LocalDate hasta) {
        Specification<Pago> criterio = PagoSpecifications.todos();
        if (numeroFactura != null && !numeroFactura.isBlank()) {
            criterio = criterio.and(PagoSpecifications.deFactura(numeroFactura.trim()));
        }
        if (numeroContrato != null && !numeroContrato.isBlank()) {
            criterio = criterio.and(PagoSpecifications.deContrato(numeroContrato.trim()));
        }
        if (canal != null) {
            criterio = criterio.and(PagoSpecifications.porCanal(canal));
        }
        if (desde != null) {
            criterio = criterio.and(PagoSpecifications.desde(desde.atStartOfDay()));
        }
        if (hasta != null) {
            criterio = criterio.and(PagoSpecifications.hasta(hasta.plusDays(1).atStartOfDay()));
        }
        return pagoRepository.findAll(criterio, Sort.by(Sort.Order.desc("fechaPago"))).stream()
                .map(PagosMapper::aPago)
                .toList();
    }

    @Override
    public ComprobantePagoResponse comprobante(String codigoPago) {
        return PagosMapper.aComprobante(buscar(codigoPago));
    }

    @Override
    public ArchivoGenerado comprobantePdf(String codigoPago) {
        return pdf(PagosMapper.aComprobante(buscar(codigoPago)));
    }

    @Override
    public List<IntentoPagoResponse> intentosEnLinea(String numeroFactura) {
        Long facturaId = facturaRepository.findByNumeroFactura(numeroFactura.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura", numeroFactura)).getId();
        return intentoRepository.findByFacturaIdOrderByFechaDesc(facturaId).stream().map(PagosMapper::aIntento).toList();
    }

    @Override
    public ComprobantePagoResponse comprobanteDelCiudadano(String codigoPago, Integer clienteId) {
        return PagosMapper.aComprobante(delCiudadano(codigoPago, clienteId));
    }

    @Override
    public ArchivoGenerado comprobantePdfDelCiudadano(String codigoPago, Integer clienteId) {
        return pdf(PagosMapper.aComprobante(delCiudadano(codigoPago, clienteId)));
    }

    private ArchivoGenerado pdf(ComprobantePagoResponse comprobante) {
        byte[] contenido = new ComprobantePagoPdf(comprobante, LocalDateTime.now(clock)).generar();
        return new ArchivoGenerado("comprobante-" + comprobante.numeroComprobante() + ".pdf", "application/pdf", contenido);
    }

    private Pago buscar(String codigoPago) {
        return pagoRepository.findByCodigoPago(codigoPago.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", codigoPago));
    }

    /** Si el pago no es del ciudadano se responde 404 (no se revela que existe). */
    private Pago delCiudadano(String codigoPago, Integer clienteId) {
        Pago pago = buscar(codigoPago);
        if (!pago.getFactura().getContrato().getCliente().getId().equals(clienteId)) {
            throw new RecursoNoEncontradoException("Pago", codigoPago);
        }
        return pago;
    }
}
