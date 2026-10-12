package co.edu.uniquindio.epq.portal.servicio.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.comercial.dominio.CambioDato;
import co.edu.uniquindio.epq.comercial.dominio.Cliente;
import co.edu.uniquindio.epq.comercial.dominio.Contrato;
import co.edu.uniquindio.epq.comercial.dominio.EstadoContrato;
import co.edu.uniquindio.epq.comercial.repositorio.ClienteRepository;
import co.edu.uniquindio.epq.comercial.repositorio.ContratoRepository;
import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.exportacion.ArchivoGenerado;
import co.edu.uniquindio.epq.facturacion.documento.GeneradorDocumentosFactura;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoCobroFactura;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.facturacion.servicio.FacturacionMapper;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaDetalleResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaResumenResponse;
import co.edu.uniquindio.epq.pagos.repositorio.PagoRepository;
import co.edu.uniquindio.epq.pagos.repositorio.PagoSpecifications;
import co.edu.uniquindio.epq.pagos.servicio.PagosMapper;
import co.edu.uniquindio.epq.pagos.web.dto.PagoResponse;
import co.edu.uniquindio.epq.portal.servicio.CalculadoraDisponibilidadFactura;
import co.edu.uniquindio.epq.portal.servicio.PortalCiudadanoService;
import co.edu.uniquindio.epq.portal.web.dto.ActualizarContactoRequest;
import co.edu.uniquindio.epq.portal.web.dto.ContratoPortalResponse;
import co.edu.uniquindio.epq.portal.web.dto.FacturaActualResponse;
import co.edu.uniquindio.epq.portal.web.dto.HistorialConsumosResponse;
import co.edu.uniquindio.epq.portal.web.dto.PerfilPortalResponse;
import co.edu.uniquindio.epq.seguridad.contexto.ContextoSeguridad;

@Service
@Transactional(readOnly = true)
public class PortalCiudadanoServiceImpl implements PortalCiudadanoService {

    /** RF-07: el historial disponible es de los últimos 24 meses. */
    static final int MAXIMO_MESES = 24;

    private final ClienteRepository clienteRepository;
    private final ContratoRepository contratoRepository;
    private final FacturaRepository facturaRepository;
    private final PagoRepository pagoRepository;
    private final GeneradorDocumentosFactura documentos;
    private final CalculadoraDisponibilidadFactura disponibilidad;
    private final ServicioAuditoria auditoria;
    private final ContextoSeguridad contexto;
    private final Clock clock;

    public PortalCiudadanoServiceImpl(ClienteRepository clienteRepository, ContratoRepository contratoRepository,
                                      FacturaRepository facturaRepository, PagoRepository pagoRepository,
                                      GeneradorDocumentosFactura documentos,
                                      CalculadoraDisponibilidadFactura disponibilidad, ServicioAuditoria auditoria,
                                      ContextoSeguridad contexto, Clock clock) {
        this.clienteRepository = clienteRepository;
        this.contratoRepository = contratoRepository;
        this.facturaRepository = facturaRepository;
        this.pagoRepository = pagoRepository;
        this.documentos = documentos;
        this.disponibilidad = disponibilidad;
        this.auditoria = auditoria;
        this.contexto = contexto;
        this.clock = clock;
    }

    @Override
    public PerfilPortalResponse perfil() {
        return aPerfil(clienteActual());
    }

    @Override
    @Transactional
    public PerfilPortalResponse actualizarContacto(ActualizarContactoRequest s) {
        Cliente cliente = clienteActual();
        List<CambioDato> cambios = cliente.actualizarContacto(s.correo(), s.telefono(), s.direccionNotificacion(),
                LocalDateTime.now(clock));
        if (!cambios.isEmpty()) {
            auditoria.registrar(AccionAuditoria.MODIFICAR_USUARIO, "cliente", cliente.getCodigoInterno(),
                    "Actualización desde el portal: " + cambios.stream().map(CambioDato::toString)
                            .collect(Collectors.joining("; ")));
        }
        return aPerfil(cliente);
    }

    @Override
    public List<ContratoPortalResponse> contratos() {
        Integer clienteId = clienteId();
        return contratoRepository.findByClienteIdOrderByNumeroContratoAsc(clienteId).stream()
                .filter(c -> c.getEstado() != EstadoContrato.CANCELADO)
                .map(this::aContratoPortal)
                .toList();
    }

    @Override
    public FacturaActualResponse facturaActual(String numeroContrato) {
        Contrato contrato = contratoDelCiudadano(numeroContrato);
        LocalDate hoy = LocalDate.now(clock);
        YearMonth periodo = disponibilidad.periodoActual(hoy);
        var actual = facturaRepository.findByContratoIdAndPeriodo(contrato.getId(), periodo)
                .filter(f -> f.getEstadoCobro() != EstadoCobroFactura.ANULADA);
        if (actual.isPresent()) {
            return new FacturaActualResponse(contrato.getNumeroContrato(), periodo.toString(), true,
                    FacturacionMapper.aDetalle(actual.get()), null, null, null);
        }
        // SWR-28 / CU-05 4a: la factura aún no se ha generado
        LocalDate estimada = disponibilidad.fechaEstimada(hoy);
        FacturaResumenResponse ultima = facturasVigentes(contrato.getId()).stream().findFirst()
                .map(f -> resumenCon(contrato, f))
                .orElse(null);
        return new FacturaActualResponse(contrato.getNumeroContrato(), periodo.toString(), false, null, estimada,
                "La factura del período %s aún no ha sido generada. Fecha estimada de disponibilidad: %s."
                        .formatted(periodo, estimada), ultima);
    }

    @Override
    public HistorialConsumosResponse historialConsumos(String numeroContrato, int meses) {
        Contrato contrato = contratoDelCiudadano(numeroContrato);
        int cantidad = Math.min(Math.max(meses, 1), MAXIMO_MESES);
        YearMonth hasta = disponibilidad.periodoActual(LocalDate.now(clock));
        YearMonth desde = hasta.minusMonths(cantidad - 1L);

        Map<YearMonth, Factura> porPeriodo = facturasVigentes(contrato.getId()).stream()
                .filter(f -> !f.getPeriodo().isBefore(desde) && !f.getPeriodo().isAfter(hasta))
                .collect(Collectors.toMap(Factura::getPeriodo, Function.identity(), (a, b) -> a));

        List<HistorialConsumosResponse.PuntoConsumo> puntos = new ArrayList<>();
        for (YearMonth p = desde; !p.isAfter(hasta); p = p.plusMonths(1)) {
            Factura f = porPeriodo.get(p);
            puntos.add(f == null
                    ? new HistorialConsumosResponse.PuntoConsumo(p.toString(), null, null, null, null)
                    : new HistorialConsumosResponse.PuntoConsumo(p.toString(), f.getConsumo(), f.getTotal(),
                            f.getNumeroFactura(), f.getEstadoCobro().name()));
        }
        return new HistorialConsumosResponse(contrato.getNumeroContrato(), contrato.getServicio().getNombre(), "m³",
                desde.toString(), hasta.toString(), puntos, resumir(puntos));
    }

    @Override
    public List<FacturaResumenResponse> facturas(String numeroContrato) {
        Integer clienteId = clienteId();
        if (numeroContrato != null && !numeroContrato.isBlank()) {
            Contrato contrato = contratoDelCiudadano(numeroContrato);
            return facturaRepository.findByContratoIdOrderByPeriodoDesc(contrato.getId()).stream()
                    .map(f -> resumenCon(contrato, f))
                    .toList();
        }
        return contratoRepository.findByClienteIdOrderByNumeroContratoAsc(clienteId).stream()
                .flatMap(c -> facturaRepository.findByContratoIdOrderByPeriodoDesc(c.getId()).stream()
                        .map(f -> resumenCon(c, f)))
                .sorted(Comparator.comparing(FacturaResumenResponse::periodo).reversed())
                .toList();
    }

    @Override
    public FacturaDetalleResponse factura(String numeroFactura) {
        return FacturacionMapper.aDetalle(facturaDelCiudadano(numeroFactura));
    }

    @Override
    public ArchivoGenerado facturaPdf(String numeroFactura) {
        return documentos.facturaPdf(facturaDelCiudadano(numeroFactura));
    }

    @Override
    public List<PagoResponse> pagos() {
        Integer clienteId = clienteId();
        return contratoRepository.findByClienteIdOrderByNumeroContratoAsc(clienteId).stream()
                .flatMap(c -> pagoRepository.findAll(PagoSpecifications.deContrato(c.getNumeroContrato()),
                        Sort.by(Sort.Order.desc("fechaPago"))).stream())
                .map(PagosMapper::aPago)
                .sorted(Comparator.comparing(PagoResponse::fechaPago).reversed())
                .toList();
    }

    // ------------------------------------------------------------------ apoyo

    private Integer clienteId() {
        return contexto.requerirUsuario().id();
    }

    private Cliente clienteActual() {
        return clienteRepository.findById(clienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario del servicio", clienteId()));
    }

    /** Si el contrato no es del ciudadano se responde 404: no se revela la existencia de datos ajenos. */
    private Contrato contratoDelCiudadano(String numeroContrato) {
        Integer clienteId = clienteId();
        return contratoRepository.findByNumeroContrato(numeroContrato.trim())
                .filter(c -> c.getCliente().getId().equals(clienteId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Contrato", numeroContrato));
    }

    private Factura facturaDelCiudadano(String numeroFactura) {
        Integer clienteId = clienteId();
        return facturaRepository.findByNumeroFactura(numeroFactura.trim())
                .filter(f -> f.getContrato().getCliente().getId().equals(clienteId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura", numeroFactura));
    }

    private List<Factura> facturasVigentes(Integer contratoId) {
        return facturaRepository.findByContratoIdOrderByPeriodoDesc(contratoId).stream()
                .filter(f -> f.getEstadoCobro() != EstadoCobroFactura.ANULADA)
                .toList();
    }

    private ContratoPortalResponse aContratoPortal(Contrato c) {
        List<Factura> facturas = facturasVigentes(c.getId());
        BigDecimal saldo = facturas.stream()
                .filter(f -> f.getEstadoCobro() == EstadoCobroFactura.PENDIENTE
                        || f.getEstadoCobro() == EstadoCobroFactura.VENCIDA)
                .map(Factura::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long vencidas = facturas.stream().filter(f -> f.getEstadoCobro() == EstadoCobroFactura.VENCIDA).count();
        Factura ultima = facturas.isEmpty() ? null : facturas.get(0);
        return new ContratoPortalResponse(c.getNumeroContrato(), c.getServicio().getNombre(), c.getDireccionServicio(),
                c.getPredio() == null ? null : c.getPredio().getMunicipio().getNombre(), c.getEstado().name(),
                ultima == null ? null : ultima.getNumeroFactura(), ultima == null ? null : ultima.getPeriodo().toString(),
                saldo, vencidas);
    }

    private static FacturaResumenResponse resumenCon(Contrato c, Factura f) {
        return new FacturaResumenResponse(f.getNumeroFactura(), f.getPeriodo().toString(), c.getNumeroContrato(),
                c.getCliente().getNombreCompleto(), c.getServicio().getNombre(), f.getConsumo(), f.getTotal(),
                f.getFechaVencimiento(), f.getEstado().name(), f.getEstadoCobro().name());
    }

    private static HistorialConsumosResponse.Resumen resumir(List<HistorialConsumosResponse.PuntoConsumo> puntos) {
        List<HistorialConsumosResponse.PuntoConsumo> conDatos = puntos.stream()
                .filter(p -> p.consumo() != null).toList();
        if (conDatos.isEmpty()) {
            return new HistorialConsumosResponse.Resumen(0, null, null, null, BigDecimal.ZERO);
        }
        BigDecimal suma = conDatos.stream().map(HistorialConsumosResponse.PuntoConsumo::consumo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new HistorialConsumosResponse.Resumen(conDatos.size(),
                suma.divide(BigDecimal.valueOf(conDatos.size()), 2, RoundingMode.HALF_UP),
                conDatos.stream().map(HistorialConsumosResponse.PuntoConsumo::consumo).max(Comparator.naturalOrder()).orElse(null),
                conDatos.stream().map(HistorialConsumosResponse.PuntoConsumo::consumo).min(Comparator.naturalOrder()).orElse(null),
                conDatos.stream().map(HistorialConsumosResponse.PuntoConsumo::valorFacturado).filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static PerfilPortalResponse aPerfil(Cliente c) {
        return new PerfilPortalResponse(c.getCodigoInterno(), c.getTipoDocumento(), c.getNumeroDocumento(),
                c.getNombreCompleto(), c.getCorreo(), c.getTelefono(), c.getDireccionNotificacion(),
                c.getMunicipio() == null ? null : c.getMunicipio().getNombre());
    }
}
