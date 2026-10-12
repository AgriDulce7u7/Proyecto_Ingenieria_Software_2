package co.edu.uniquindio.epq.comercial.servicio.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.comercial.dominio.Cliente;
import co.edu.uniquindio.epq.comercial.dominio.Contrato;
import co.edu.uniquindio.epq.comercial.dominio.EstadoContrato;
import co.edu.uniquindio.epq.comercial.dominio.HistorialContrato;
import co.edu.uniquindio.epq.comercial.dominio.Medidor;
import co.edu.uniquindio.epq.comercial.dominio.Predio;
import co.edu.uniquindio.epq.comercial.dominio.SelectorTarifa;
import co.edu.uniquindio.epq.comercial.dominio.Servicio;
import co.edu.uniquindio.epq.comercial.dominio.Tarifa;
import co.edu.uniquindio.epq.comercial.dominio.TipoServicio;
import co.edu.uniquindio.epq.comercial.repositorio.ClienteRepository;
import co.edu.uniquindio.epq.comercial.repositorio.ContratoRepository;
import co.edu.uniquindio.epq.comercial.repositorio.HistorialContratoRepository;
import co.edu.uniquindio.epq.comercial.repositorio.MedidorRepository;
import co.edu.uniquindio.epq.comercial.repositorio.PredioRepository;
import co.edu.uniquindio.epq.comercial.repositorio.ServicioRepository;
import co.edu.uniquindio.epq.comercial.repositorio.TarifaRepository;
import co.edu.uniquindio.epq.comercial.servicio.ComercialMapper;
import co.edu.uniquindio.epq.comercial.servicio.GestionContratosService;
import co.edu.uniquindio.epq.comercial.web.dto.AsignarMedidorRequest;
import co.edu.uniquindio.epq.comercial.web.dto.CambiarEstadoContratoRequest;
import co.edu.uniquindio.epq.comercial.web.dto.ContratoDetalleResponse;
import co.edu.uniquindio.epq.comercial.web.dto.RegistrarContratoRequest;
import co.edu.uniquindio.epq.comun.excepcion.RecursoDuplicadoException;
import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.comun.secuencia.GeneradorConsecutivos;
import co.edu.uniquindio.epq.comun.secuencia.SecuenciaNegocio;
import co.edu.uniquindio.epq.seguridad.contexto.ContextoSeguridad;

/**
 * CU-02 Gestionar contrato de servicio (F-04): RN-01, SWR-17 a SWR-20.
 */
@Service
@Transactional
public class GestionContratosServiceImpl implements GestionContratosService {

    private static final String ENTIDAD = "contrato";
    /** RN-01: contratos que impiden registrar otro del mismo servicio en el predio. */
    private static final Set<EstadoContrato> VIGENTES =
            EnumSet.of(EstadoContrato.PENDIENTE, EstadoContrato.ACTIVO, EstadoContrato.SUSPENDIDO);

    private final ContratoRepository contratoRepository;
    private final ClienteRepository clienteRepository;
    private final PredioRepository predioRepository;
    private final ServicioRepository servicioRepository;
    private final TarifaRepository tarifaRepository;
    private final MedidorRepository medidorRepository;
    private final HistorialContratoRepository historialRepository;
    private final GeneradorConsecutivos consecutivos;
    private final ServicioAuditoria auditoria;
    private final ContextoSeguridad contexto;
    private final Clock clock;

    public GestionContratosServiceImpl(ContratoRepository contratoRepository, ClienteRepository clienteRepository,
                                       PredioRepository predioRepository, ServicioRepository servicioRepository,
                                       TarifaRepository tarifaRepository, MedidorRepository medidorRepository,
                                       HistorialContratoRepository historialRepository,
                                       GeneradorConsecutivos consecutivos, ServicioAuditoria auditoria,
                                       ContextoSeguridad contexto, Clock clock) {
        this.contratoRepository = contratoRepository;
        this.clienteRepository = clienteRepository;
        this.predioRepository = predioRepository;
        this.servicioRepository = servicioRepository;
        this.tarifaRepository = tarifaRepository;
        this.medidorRepository = medidorRepository;
        this.historialRepository = historialRepository;
        this.consecutivos = consecutivos;
        this.auditoria = auditoria;
        this.contexto = contexto;
        this.clock = clock;
    }

    @Override
    public ContratoDetalleResponse registrar(RegistrarContratoRequest s) {
        Cliente cliente = clienteRepository.findByCodigoInterno(s.codigoUsuario().trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario del servicio", s.codigoUsuario()));
        Predio predio = predioRepository.findWithMunicipioById(s.predioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Predio", s.predioId()));
        Servicio servicio = servicio(s.servicio());

        // RN-01 / SWR-17: no puede existir otro contrato vigente del mismo servicio en el predio
        validarUnicoContrato(predio, servicio, VIGENTES, null);

        LocalDateTime ahora = LocalDateTime.now(clock);
        LocalDate hoy = ahora.toLocalDate();
        // SWR-18: tarifa calculada automáticamente según servicio, municipio y estrato del predio
        Tarifa tarifa = SelectorTarifa.masEspecifica(tarifaRepository.buscarVigentes(servicio.getId(), hoy), predio)
                .orElseThrow(() -> new ReglaNegocioException(
                        "No hay una tarifa vigente de %s aplicable al predio (municipio %s, estrato %d) - RN-08"
                                .formatted(servicio.getNombre(), predio.getMunicipio().getNombre(), predio.getEstrato())));

        String usuario = contexto.identificadorActual();
        String numero = "CT-%s-%04d".formatted(s.servicio().getPrefijoContrato(),
                consecutivos.siguiente(SecuenciaNegocio.CONTRATO));
        Contrato contrato = contratoRepository.save(Contrato.registrar(numero, cliente, predio, servicio, tarifa,
                s.fechaInicio() == null ? hoy : s.fechaInicio(), usuario, ahora));
        historial(contrato, null, "Registro del contrato", usuario, ahora);

        Medidor medidor = null;
        if (s.numeroMedidor() != null && !s.numeroMedidor().isBlank()) {
            medidor = instalarMedidor(contrato, s.numeroMedidor(),
                    s.fechaInstalacionMedidor() == null ? hoy : s.fechaInstalacionMedidor());
        }

        List<String> advertencias = new ArrayList<>();
        if (!Boolean.FALSE.equals(s.activar())) {
            if (s.servicio().requiereMedidor() && medidor == null) {
                // SWR-19: sin medidor el contrato no se activa
                advertencias.add(("El contrato quedó PENDIENTE de activación: los contratos de %s requieren número de "
                        + "medidor (SWR-19). Asígnelo con PUT /api/contratos/%s/medidor.")
                        .formatted(servicio.getNombre(), numero));
            } else {
                contrato.activar(medidor != null);
                historial(contrato, EstadoContrato.PENDIENTE, "Activación del contrato", usuario, ahora);
            }
        }

        auditoria.registrar(AccionAuditoria.CREAR_CONTRATO, ENTIDAD, numero,
                "Contrato %s de %s para %s en el predio %s. Tarifa: %s. Medidor: %s. Estado: %s".formatted(
                        numero, servicio.getNombre(), cliente.getCodigoInterno(), predio.getCodigoPredio(),
                        tarifa.getNombre(), medidor == null ? "sin medidor" : medidor.getNumeroSerie(),
                        contrato.getEstado()));
        return detalle(contrato, medidor, advertencias);
    }

    @Override
    public ContratoDetalleResponse cambiarEstado(String numeroContrato, CambiarEstadoContratoRequest s) {
        Contrato contrato = buscar(numeroContrato);
        EstadoContrato anterior = contrato.getEstado();
        LocalDateTime ahora = LocalDateTime.now(clock);
        switch (s.estado()) {
            case ACTIVO -> {
                validarUnicoContrato(contrato.getPredio(), contrato.getServicio(), EnumSet.of(EstadoContrato.ACTIVO),
                        contrato.getId());
                contrato.activar(medidorActivo(contrato) != null);
            }
            case SUSPENDIDO -> contrato.suspender();
            case CANCELADO -> {
                contrato.cancelar();
                Medidor medidor = medidorActivo(contrato);
                if (medidor != null) {
                    medidor.retirar(ahora);
                }
            }
            case PENDIENTE -> throw new ReglaNegocioException("Un contrato no puede volver al estado PENDIENTE");
        }
        String usuario = contexto.identificadorActual();
        historial(contrato, anterior, s.motivo().trim(), usuario, ahora);
        auditoria.registrar(AccionAuditoria.CAMBIAR_ESTADO_CONTRATO, ENTIDAD, numeroContrato,
                "Estado %s → %s. Motivo: %s".formatted(anterior, contrato.getEstado(), s.motivo().trim()));
        return detalle(contrato, medidorActivo(contrato), List.of());
    }

    @Override
    public ContratoDetalleResponse asignarMedidor(String numeroContrato, AsignarMedidorRequest s) {
        Contrato contrato = buscar(numeroContrato);
        if (contrato.getEstado() == EstadoContrato.CANCELADO) {
            throw new ReglaNegocioException("No se puede asignar un medidor a un contrato cancelado");
        }
        LocalDateTime ahora = LocalDateTime.now(clock);
        Medidor anterior = medidorActivo(contrato);
        if (anterior != null) {
            anterior.retirar(ahora);
        }
        Medidor nuevo = instalarMedidor(contrato, s.numeroSerie(),
                s.fechaInstalacion() == null ? ahora.toLocalDate() : s.fechaInstalacion());

        List<String> advertencias = new ArrayList<>();
        if (Boolean.TRUE.equals(s.activarContrato()) && contrato.getEstado() == EstadoContrato.PENDIENTE) {
            validarUnicoContrato(contrato.getPredio(), contrato.getServicio(), EnumSet.of(EstadoContrato.ACTIVO),
                    contrato.getId());
            contrato.activar(true);
            historial(contrato, EstadoContrato.PENDIENTE, "Activación tras instalar el medidor " + nuevo.getNumeroSerie(),
                    contexto.identificadorActual(), ahora);
        }
        auditoria.registrar(AccionAuditoria.ASIGNAR_MEDIDOR, ENTIDAD, numeroContrato,
                "Medidor %s instalado%s".formatted(nuevo.getNumeroSerie(),
                        anterior == null ? "" : " (reemplaza a " + anterior.getNumeroSerie() + ")"));
        return detalle(contrato, nuevo, advertencias);
    }

    // ------------------------------------------------------------------ apoyo

    private void validarUnicoContrato(Predio predio, Servicio servicio, Set<EstadoContrato> estados, Integer excluirId) {
        if (predio == null) {
            return; // contratos migrados sin predio
        }
        contratoRepository.buscarPorPredioServicioYEstados(predio.getId(), servicio.getId(), estados).stream()
                .filter(c -> !c.getId().equals(excluirId))
                .findFirst()
                .ifPresent(existente -> {
                    throw new RecursoDuplicadoException(("El predio %s ya tiene el contrato %s de %s en estado %s. "
                            + "RN-01: solo puede existir un contrato activo por tipo de servicio y predio.")
                            .formatted(predio.getCodigoPredio(), existente.getNumeroContrato(), servicio.getNombre(),
                                    existente.getEstado()))
                            .conDato("contratoExistente", existente.getNumeroContrato());
                });
    }

    private Medidor instalarMedidor(Contrato contrato, String numeroSerie, LocalDate fecha) {
        String serie = numeroSerie.trim().toUpperCase();
        if (medidorRepository.existsByNumeroSerieIgnoreCase(serie)) {
            throw new RecursoDuplicadoException("El medidor " + serie + " ya está registrado en otro contrato");
        }
        return medidorRepository.save(Medidor.instalar(contrato, serie, fecha));
    }

    private Medidor medidorActivo(Contrato contrato) {
        return medidorRepository.findFirstByContratoIdAndEstadoOrderByFechaInstalacionDesc(contrato.getId(),
                Medidor.ESTADO_ACTIVO).orElse(null);
    }

    private void historial(Contrato contrato, EstadoContrato anterior, String motivo, String usuario, LocalDateTime fecha) {
        historialRepository.save(HistorialContrato.registrar(contrato, anterior, motivo, usuario, fecha));
    }

    private Servicio servicio(TipoServicio tipo) {
        return servicioRepository.findByNombreIgnoreCase(tipo.getNombre())
                .orElseThrow(() -> new IllegalStateException("No existe el servicio " + tipo.getNombre()
                        + " en el catálogo. Verifique 02_seed_data.sql"));
    }

    private Contrato buscar(String numeroContrato) {
        return contratoRepository.findByNumeroContrato(numeroContrato.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Contrato", numeroContrato));
    }

    private ContratoDetalleResponse detalle(Contrato contrato, Medidor medidor, List<String> advertencias) {
        return ComercialMapper.aDetalle(contrato, medidor,
                historialRepository.findByContratoIdOrderByFechaAscIdAsc(contrato.getId()), advertencias);
    }
}
