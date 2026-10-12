package co.edu.uniquindio.epq.comercial.servicio.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.comercial.dominio.CambioDato;
import co.edu.uniquindio.epq.comercial.dominio.Cliente;
import co.edu.uniquindio.epq.comercial.dominio.EstadoValidacionIdentidad;
import co.edu.uniquindio.epq.comercial.dominio.Predio;
import co.edu.uniquindio.epq.comercial.dominio.TipoPersona;
import co.edu.uniquindio.epq.comercial.repositorio.ClienteRepository;
import co.edu.uniquindio.epq.comercial.repositorio.ContratoRepository;
import co.edu.uniquindio.epq.comercial.repositorio.PredioRepository;
import co.edu.uniquindio.epq.comercial.rnec.ResultadoValidacionRnec;
import co.edu.uniquindio.epq.comercial.rnec.SolicitudValidacionRnec;
import co.edu.uniquindio.epq.comercial.rnec.ValidadorIdentidadRnec;
import co.edu.uniquindio.epq.comercial.servicio.ComercialMapper;
import co.edu.uniquindio.epq.comercial.servicio.GestionUsuariosService;
import co.edu.uniquindio.epq.comercial.web.dto.ActualizarUsuarioRequest;
import co.edu.uniquindio.epq.comercial.web.dto.PredioResponse;
import co.edu.uniquindio.epq.comercial.web.dto.RegistrarPredioRequest;
import co.edu.uniquindio.epq.comercial.web.dto.RegistrarUsuarioRequest;
import co.edu.uniquindio.epq.comercial.web.dto.UsuarioDetalleResponse;
import co.edu.uniquindio.epq.comercial.web.dto.UsuarioRegistradoResponse;
import co.edu.uniquindio.epq.comun.excepcion.RecursoDuplicadoException;
import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.comun.secuencia.GeneradorConsecutivos;
import co.edu.uniquindio.epq.comun.secuencia.SecuenciaNegocio;
import co.edu.uniquindio.epq.comun.territorio.Municipio;
import co.edu.uniquindio.epq.comun.territorio.ResolutorMunicipios;
import co.edu.uniquindio.epq.seguridad.contexto.ContextoSeguridad;

/**
 * CU-01 Gestionar usuario. Sigue el DS-01: valida unicidad (SWR-12), verifica contra la RNEC (IS-03),
 * crea el usuario con su código interno y su predio, y audita la operación (RF-15).
 */
@Service
@Transactional
public class GestionUsuariosServiceImpl implements GestionUsuariosService {

    static final String ENTIDAD = "cliente";

    private final ClienteRepository clienteRepository;
    private final PredioRepository predioRepository;
    private final ContratoRepository contratoRepository;
    private final ResolutorMunicipios municipios;
    private final ValidadorIdentidadRnec rnec;
    private final GeneradorConsecutivos consecutivos;
    private final ServicioAuditoria auditoria;
    private final ContextoSeguridad contexto;
    private final Clock clock;

    public GestionUsuariosServiceImpl(ClienteRepository clienteRepository, PredioRepository predioRepository,
                                      ContratoRepository contratoRepository, ResolutorMunicipios municipios,
                                      ValidadorIdentidadRnec rnec, GeneradorConsecutivos consecutivos,
                                      ServicioAuditoria auditoria, ContextoSeguridad contexto, Clock clock) {
        this.clienteRepository = clienteRepository;
        this.predioRepository = predioRepository;
        this.contratoRepository = contratoRepository;
        this.municipios = municipios;
        this.rnec = rnec;
        this.consecutivos = consecutivos;
        this.auditoria = auditoria;
        this.contexto = contexto;
        this.clock = clock;
    }

    @Override
    public UsuarioRegistradoResponse registrar(RegistrarUsuarioRequest s) {
        String numeroDocumento = s.numeroDocumento().trim();
        // SWR-12 / CU-01 4a: identificación única; se ofrece consultar el perfil existente
        clienteRepository.findByNumeroDocumento(numeroDocumento).ifPresent(existente -> {
            throw new RecursoDuplicadoException(
                    "El número de identificación %s ya está registrado".formatted(numeroDocumento))
                    .conDato("codigoUsuarioExistente", existente.getCodigoInterno())
                    .conDato("perfilExistente", "/api/usuarios-servicio/" + existente.getCodigoInterno());
        });
        Municipio municipio = municipios.atendido(s.municipioId());   // CO-03: MunicipioNoAtendidoException
        LocalDateTime ahora = LocalDateTime.now(clock);
        String usuario = contexto.identificadorActual();

        TipoPersona tipoPersona = s.tipoPersona() != null ? s.tipoPersona()
                : "NIT".equals(s.tipoDocumento()) ? TipoPersona.JURIDICA : TipoPersona.NATURAL;
        String direccionNotificacion = s.direccionNotificacion() == null || s.direccionNotificacion().isBlank()
                ? s.direccionPredio() : s.direccionNotificacion();
        Cliente cliente = Cliente.registrar(
                "USR-%06d".formatted(consecutivos.siguiente(SecuenciaNegocio.USUARIO)),
                s.tipoDocumento(), numeroDocumento, tipoPersona, s.nombreCompleto(), s.correo(), s.telefono(),
                direccionNotificacion, municipio, usuario, ahora);

        // CU-01 paso 4 / SWR-14: si la RNEC falla, se continúa con advertencia y queda pendiente de validación
        ResultadoValidacionRnec validacion = validarEnRnec(cliente);
        cliente.registrarValidacion(validacion.estadoUsuario(), validacion.mensaje(), ahora);
        clienteRepository.save(cliente);

        Predio predio = predioRepository.save(Predio.registrar(
                "PRD-%06d".formatted(consecutivos.siguiente(SecuenciaNegocio.PREDIO)),
                s.direccionPredio(), municipio, s.estrato(), s.usoPredio(), cliente, ahora));

        String servicio = s.servicioSolicitado() == null ? "no indicado" : s.servicioSolicitado().getNombre();
        auditoria.registrar(AccionAuditoria.REGISTRAR_USUARIO, ENTIDAD, cliente.getCodigoInterno(),
                "Usuario %s %s registrado con predio %s (%s, estrato %d). Servicio solicitado: %s. Validación RNEC: %s - %s"
                        .formatted(cliente.getTipoDocumento(), numeroDocumento, predio.getCodigoPredio(),
                                municipio.getNombre(), predio.getEstrato(), servicio,
                                validacion.estado(), validacion.mensaje()));

        String advertencia = validacion.esExitosa() ? null
                : "No fue posible validar la identificación contra la RNEC (%s). El usuario quedó pendiente de "
                  + "validación; puede reintentarla con POST /api/usuarios-servicio/%s/validacion-rnec."
                        .formatted(validacion.mensaje(), cliente.getCodigoInterno());
        return new UsuarioRegistradoResponse(cliente.getId(), cliente.getCodigoInterno(), cliente.getNombreCompleto(),
                cliente.getEstadoValidacion().name(), advertencia, ComercialMapper.aPredio(predio),
                s.servicioSolicitado() == null ? null : s.servicioSolicitado().name(),
                "Usuario registrado con el código %s. Para vincular el servicio registre el contrato del predio %s."
                        .formatted(cliente.getCodigoInterno(), predio.getCodigoPredio()));
    }

    @Override
    public UsuarioDetalleResponse actualizar(String codigoInterno, ActualizarUsuarioRequest s) {
        Cliente cliente = buscar(codigoInterno);
        List<CambioDato> cambios = cliente.actualizarDatos(s.nombreCompleto(), s.correo(), s.telefono(),
                s.direccionNotificacion(), municipios.atendido(s.municipioId()), LocalDateTime.now(clock));
        if (!cambios.isEmpty()) {
            // SWR-15: auditoría de cada cambio con fecha, hora y responsable
            auditoria.registrar(AccionAuditoria.MODIFICAR_USUARIO, ENTIDAD, codigoInterno,
                    cambios.stream().map(CambioDato::toString).collect(Collectors.joining("; ")));
        }
        return detalle(cliente);
    }

    @Override
    public UsuarioDetalleResponse desactivar(String codigoInterno, String motivo) {
        Cliente cliente = buscar(codigoInterno);
        if (!cliente.isActivo()) {
            throw new ReglaNegocioException("El usuario " + codigoInterno + " ya está inactivo");
        }
        boolean tieneContratosVigentes = contratoRepository.findByClienteIdOrderByNumeroContratoAsc(cliente.getId())
                .stream().anyMatch(c -> c.getEstado() != co.edu.uniquindio.epq.comercial.dominio.EstadoContrato.CANCELADO);
        if (tieneContratosVigentes) {
            throw new ReglaNegocioException(
                    "El usuario tiene contratos vigentes. Cancele sus contratos antes de desactivarlo.");
        }
        cliente.desactivar(LocalDateTime.now(clock));
        auditoria.registrar(AccionAuditoria.DESACTIVAR_USUARIO, ENTIDAD, codigoInterno, "Motivo: " + motivo.trim());
        return detalle(cliente);
    }

    @Override
    public UsuarioDetalleResponse reactivar(String codigoInterno, String motivo) {
        Cliente cliente = buscar(codigoInterno);
        cliente.reactivar(LocalDateTime.now(clock));
        auditoria.registrar(AccionAuditoria.MODIFICAR_USUARIO, ENTIDAD, codigoInterno,
                "Usuario reactivado. Motivo: " + motivo.trim());
        return detalle(cliente);
    }

    @Override
    public UsuarioDetalleResponse revalidarIdentidad(String codigoInterno) {
        Cliente cliente = buscar(codigoInterno);
        if (cliente.getEstadoValidacion() == EstadoValidacionIdentidad.VALIDADO) {
            throw new ReglaNegocioException("La identidad del usuario " + codigoInterno + " ya está validada");
        }
        ResultadoValidacionRnec validacion = validarEnRnec(cliente);
        cliente.registrarValidacion(validacion.estadoUsuario(), validacion.mensaje(), LocalDateTime.now(clock));
        auditoria.registrar(AccionAuditoria.VALIDAR_IDENTIDAD_RNEC, ENTIDAD, codigoInterno,
                "Revalidación RNEC: %s - %s".formatted(validacion.estado(), validacion.mensaje()));
        return detalle(cliente);
    }

    @Override
    public PredioResponse registrarPredio(String codigoInterno, RegistrarPredioRequest s) {
        Cliente cliente = buscar(codigoInterno);
        Municipio municipio = municipios.atendido(s.municipioId());
        Predio predio = predioRepository.save(Predio.registrar(
                "PRD-%06d".formatted(consecutivos.siguiente(SecuenciaNegocio.PREDIO)),
                s.direccion(), municipio, s.estrato(), s.uso(), cliente, LocalDateTime.now(clock)));
        auditoria.registrar(AccionAuditoria.REGISTRAR_PREDIO, "predio", predio.getCodigoPredio(),
                "Predio %s registrado para %s: %s, %s, estrato %d".formatted(predio.getCodigoPredio(), codigoInterno,
                        predio.getDireccion(), municipio.getNombre(), predio.getEstrato()));
        return ComercialMapper.aPredio(predio);
    }

    private ResultadoValidacionRnec validarEnRnec(Cliente cliente) {
        try {
            return rnec.validar(new SolicitudValidacionRnec(cliente.getTipoDocumento(), cliente.getNumeroDocumento(),
                    cliente.getNombreCompleto()));
        } catch (RuntimeException ex) {
            return ResultadoValidacionRnec.noDisponible("Error al consultar la RNEC: " + ex.getMessage());
        }
    }

    private Cliente buscar(String codigoInterno) {
        return clienteRepository.findByCodigoInterno(codigoInterno.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario del servicio", codigoInterno));
    }

    private UsuarioDetalleResponse detalle(Cliente cliente) {
        return ComercialMapper.aDetalle(cliente, predioRepository.findByClienteIdOrderByIdAsc(cliente.getId()),
                contratoRepository.findByClienteIdOrderByNumeroContratoAsc(cliente.getId()));
    }
}
