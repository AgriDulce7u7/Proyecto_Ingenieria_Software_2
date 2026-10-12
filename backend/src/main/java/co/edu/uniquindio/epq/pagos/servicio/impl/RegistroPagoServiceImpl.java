package co.edu.uniquindio.epq.pagos.servicio.impl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.comun.excepcion.RecursoDuplicadoException;
import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.pagos.dominio.CanalPago;
import co.edu.uniquindio.epq.pagos.dominio.IntentoPagoEnLinea;
import co.edu.uniquindio.epq.pagos.dominio.Pago;
import co.edu.uniquindio.epq.pagos.dominio.ResultadoIntentoPago;
import co.edu.uniquindio.epq.pagos.pasarela.DatosTarjeta;
import co.edu.uniquindio.epq.pagos.pasarela.ProcesadorTransaccionesPasarela;
import co.edu.uniquindio.epq.pagos.pasarela.RespuestaPasarela;
import co.edu.uniquindio.epq.pagos.pasarela.SolicitudCobro;
import co.edu.uniquindio.epq.pagos.repositorio.IntentoPagoEnLineaRepository;
import co.edu.uniquindio.epq.pagos.repositorio.PagoRepository;
import co.edu.uniquindio.epq.pagos.servicio.AplicadorPago;
import co.edu.uniquindio.epq.pagos.servicio.PagoRechazadoException;
import co.edu.uniquindio.epq.pagos.servicio.PagosMapper;
import co.edu.uniquindio.epq.pagos.servicio.RegistroPagoService;
import co.edu.uniquindio.epq.pagos.servicio.TimeoutPagoException;
import co.edu.uniquindio.epq.pagos.web.dto.ComprobantePagoResponse;
import co.edu.uniquindio.epq.pagos.web.dto.PagoEnLineaRequest;
import co.edu.uniquindio.epq.pagos.web.dto.RegistrarPagoPresencialRequest;
import co.edu.uniquindio.epq.seguridad.contexto.ContextoSeguridad;
import co.edu.uniquindio.epq.seguridad.contexto.UsuarioAutenticado;

/**
 * Sigue el DS-03 / CO-02. El pago en línea NO es transaccional en su conjunto: la llamada a la pasarela
 * (externa y lenta) ocurre fuera de transacción y solo la aplicación del pago confirmado se hace en una
 * transacción corta ({@link AplicadorPago}).
 */
@Service
public class RegistroPagoServiceImpl implements RegistroPagoService {

    private static final Logger log = LoggerFactory.getLogger(RegistroPagoServiceImpl.class);

    /** Evita dos pagos en línea simultáneos de la misma factura en esta instancia. */
    private final Set<String> pagosEnCurso = ConcurrentHashMap.newKeySet();

    private final AplicadorPago aplicador;
    private final FacturaRepository facturaRepository;
    private final PagoRepository pagoRepository;
    private final IntentoPagoEnLineaRepository intentoRepository;
    private final ProcesadorTransaccionesPasarela pasarela;
    private final ServicioAuditoria auditoria;
    private final ContextoSeguridad contexto;
    private final Clock clock;

    public RegistroPagoServiceImpl(AplicadorPago aplicador, FacturaRepository facturaRepository,
                                   PagoRepository pagoRepository, IntentoPagoEnLineaRepository intentoRepository,
                                   ProcesadorTransaccionesPasarela pasarela, ServicioAuditoria auditoria,
                                   ContextoSeguridad contexto, Clock clock) {
        this.aplicador = aplicador;
        this.facturaRepository = facturaRepository;
        this.pagoRepository = pagoRepository;
        this.intentoRepository = intentoRepository;
        this.pasarela = pasarela;
        this.auditoria = auditoria;
        this.contexto = contexto;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ComprobantePagoResponse registrarPresencial(RegistrarPagoPresencialRequest s) {
        if (s.canal() == CanalPago.EN_LINEA) {
            throw new ReglaNegocioException("Los pagos en línea solo se registran con la confirmación de la pasarela (RN-05)");
        }
        Pago pago = aplicador.aplicar(s.numeroFactura(), s.valor(), s.canal(), null,
                s.referencia() == null || s.referencia().isBlank() ? null : s.referencia().trim(),
                contexto.identificadorActual());
        return PagosMapper.aComprobante(pago);
    }

    @Override
    public ComprobantePagoResponse pagarEnLinea(String numeroFactura, PagoEnLineaRequest s) {
        UsuarioAutenticado ciudadano = contexto.requerirUsuario();
        Factura factura = facturaDelCiudadano(numeroFactura, ciudadano);
        factura.validarPagable();                                   // SWR-23
        BigDecimal monto = factura.getTotal();
        if (s.valor() != null && s.valor().compareTo(monto) != 0) {  // CO-02: el monto coincide con el total
            throw new ReglaNegocioException("El valor a pagar debe ser el total de la factura: " + monto);
        }
        if (!pagosEnCurso.add(factura.getNumeroFactura())) {
            throw new RecursoDuplicadoException("Ya hay un pago en proceso para la factura " + factura.getNumeroFactura());
        }
        DatosTarjeta tarjeta = new DatosTarjeta(s.numeroTarjeta(), s.titular().trim(), s.mesVencimiento(),
                s.anioVencimiento(), s.cvv());
        String usuario = ciudadano.identificadorAuditoria();
        try {
            String referencia = factura.getNumeroFactura() + "-" + UUID.randomUUID().toString().substring(0, 8);
            ProcesadorTransaccionesPasarela.ResultadoCobro resultado;
            try {
                resultado = pasarela.cobrar(new SolicitudCobro(referencia, monto, tarjeta,
                        "EPQ factura " + factura.getNumeroFactura()));
            } catch (TimeoutPagoException ex) {
                registrarIntento(factura, monto, ResultadoIntentoPago.SIN_RESPUESTA, null, null, ex.getMessage(),
                        tarjeta, (Integer) ex.propiedades().get("intentos"), usuario);
                throw ex;
            }
            RespuestaPasarela respuesta = resultado.respuesta();
            if (!respuesta.aprobada()) {
                registrarIntento(factura, monto, ResultadoIntentoPago.RECHAZADO, null, respuesta.codigoRespuesta(),
                        respuesta.mensaje(), tarjeta, resultado.intentos(), usuario);
                auditoria.registrarIndependiente(usuario, ciudadano.rol().name(), AccionAuditoria.PAGO_EN_LINEA_RECHAZADO,
                        "factura", factura.getNumeroFactura(),
                        "Código %s: %s".formatted(respuesta.codigoRespuesta(), respuesta.mensaje()), false);
                throw new PagoRechazadoException(respuesta.codigoRespuesta(), respuesta.mensaje());
            }
            registrarIntento(factura, monto, ResultadoIntentoPago.APROBADO, respuesta.codigoTransaccion(),
                    respuesta.codigoRespuesta(), respuesta.mensaje(), tarjeta, resultado.intentos(), usuario);
            return aplicarPagoConfirmado(factura, monto, respuesta.codigoTransaccion(), referencia, usuario);
        } finally {
            pagosEnCurso.remove(factura.getNumeroFactura());
        }
    }

    private ComprobantePagoResponse aplicarPagoConfirmado(Factura factura, BigDecimal monto, String codigoTransaccion,
                                                          String referencia, String usuario) {
        try {
            Pago pago = aplicador.aplicar(factura.getNumeroFactura(), monto, CanalPago.EN_LINEA, codigoTransaccion,
                    referencia, usuario);
            return PagosMapper.aComprobante(pagoRepository.findWithDetalleById(pago.getId()).orElseThrow());
        } catch (RuntimeException ex) {
            // La pasarela cobró pero el pago no pudo aplicarse (p. ej. se pagó por otro canal en paralelo)
            log.error("REQUIERE REVERSO: transacción {} aprobada para la factura {} no pudo aplicarse",
                    codigoTransaccion, factura.getNumeroFactura(), ex);
            throw new ReglaNegocioException(("La transacción %s fue aprobada pero no pudo aplicarse a la factura %s: %s. "
                    + "EPQ gestionará el reverso del cobro.").formatted(codigoTransaccion, factura.getNumeroFactura(),
                    ex.getMessage()));
        }
    }

    private Factura facturaDelCiudadano(String numeroFactura, UsuarioAutenticado ciudadano) {
        return facturaRepository.findByNumeroFactura(numeroFactura.trim())
                .filter(f -> f.getContrato().getCliente().getId().equals(ciudadano.id()))
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura", numeroFactura));
    }

    /** Se guarda aunque el pago falle: este método corre fuera de transacción y save() confirma de inmediato. */
    private void registrarIntento(Factura factura, BigDecimal valor, ResultadoIntentoPago resultado, String codigoTransaccion,
                          String codigoRespuesta, String mensaje, DatosTarjeta tarjeta, Integer intentos, String usuario) {
        intentoRepository.save(IntentoPagoEnLinea.registrar(factura.getId(), valor, resultado, codigoTransaccion,
                codigoRespuesta, mensaje, tarjeta.ultimosDigitos(), intentos == null ? 1 : intentos, usuario,
                LocalDateTime.now(clock)));
    }
}
