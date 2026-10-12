package co.edu.uniquindio.epq.comercial.servicio.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.comercial.dominio.Contrato;
import co.edu.uniquindio.epq.comercial.dominio.EstadoContrato;
import co.edu.uniquindio.epq.comercial.dominio.Medidor;
import co.edu.uniquindio.epq.comercial.dominio.SelectorTarifa;
import co.edu.uniquindio.epq.comercial.dominio.Servicio;
import co.edu.uniquindio.epq.comercial.dominio.TipoServicio;
import co.edu.uniquindio.epq.comercial.repositorio.ContratoRepository;
import co.edu.uniquindio.epq.comercial.repositorio.ContratoSpecifications;
import co.edu.uniquindio.epq.comercial.repositorio.HistorialContratoRepository;
import co.edu.uniquindio.epq.comercial.repositorio.MedidorRepository;
import co.edu.uniquindio.epq.comercial.repositorio.ServicioRepository;
import co.edu.uniquindio.epq.comercial.repositorio.TarifaRepository;
import co.edu.uniquindio.epq.comercial.servicio.ComercialMapper;
import co.edu.uniquindio.epq.comercial.servicio.ConsultaContratosService;
import co.edu.uniquindio.epq.comercial.web.dto.ContratoDetalleResponse;
import co.edu.uniquindio.epq.comercial.web.dto.ContratoResumenResponse;
import co.edu.uniquindio.epq.comercial.web.dto.HistorialContratoResponse;
import co.edu.uniquindio.epq.comercial.web.dto.TarifaResponse;
import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;

@Service
@Transactional(readOnly = true)
public class ConsultaContratosServiceImpl implements ConsultaContratosService {

    private final ContratoRepository contratoRepository;
    private final HistorialContratoRepository historialRepository;
    private final MedidorRepository medidorRepository;
    private final TarifaRepository tarifaRepository;
    private final ServicioRepository servicioRepository;
    private final Clock clock;

    public ConsultaContratosServiceImpl(ContratoRepository contratoRepository,
                                        HistorialContratoRepository historialRepository,
                                        MedidorRepository medidorRepository, TarifaRepository tarifaRepository,
                                        ServicioRepository servicioRepository, Clock clock) {
        this.contratoRepository = contratoRepository;
        this.historialRepository = historialRepository;
        this.medidorRepository = medidorRepository;
        this.tarifaRepository = tarifaRepository;
        this.servicioRepository = servicioRepository;
        this.clock = clock;
    }

    @Override
    public List<ContratoResumenResponse> listar(EstadoContrato estado, TipoServicio servicio, String codigoUsuario,
                                                Integer municipioId, Boolean candidatoSuspension) {
        Specification<Contrato> criterio = ContratoSpecifications.todos();
        if (estado != null) {
            criterio = criterio.and(ContratoSpecifications.conEstado(estado));
        }
        if (servicio != null) {
            criterio = criterio.and(ContratoSpecifications.deServicio(servicio));
        }
        if (codigoUsuario != null && !codigoUsuario.isBlank()) {
            criterio = criterio.and(ContratoSpecifications.delUsuario(codigoUsuario.trim()));
        }
        if (municipioId != null) {
            criterio = criterio.and(ContratoSpecifications.delMunicipio(municipioId));
        }
        if (Boolean.TRUE.equals(candidatoSuspension)) {
            criterio = criterio.and(ContratoSpecifications.candidatosSuspension());
        }
        return contratoRepository.findAll(criterio, Sort.by("numeroContrato")).stream()
                .map(ComercialMapper::aResumen)
                .toList();
    }

    @Override
    public ContratoDetalleResponse obtener(String numeroContrato) {
        Contrato contrato = buscar(numeroContrato);
        Medidor medidor = medidorRepository.findFirstByContratoIdAndEstadoOrderByFechaInstalacionDesc(contrato.getId(),
                Medidor.ESTADO_ACTIVO).orElse(null);
        return ComercialMapper.aDetalle(contrato, medidor,
                historialRepository.findByContratoIdOrderByFechaAscIdAsc(contrato.getId()), List.of());
    }

    @Override
    public List<HistorialContratoResponse> historial(String numeroContrato) {
        return historialRepository.findByContratoIdOrderByFechaAscIdAsc(buscar(numeroContrato).getId()).stream()
                .map(ComercialMapper::aHistorial)
                .toList();
    }

    @Override
    public TarifaResponse tarifaAplicable(TipoServicio servicio, Integer municipioId, Integer estrato, LocalDate fecha) {
        LocalDate dia = fecha == null ? LocalDate.now(clock) : fecha;
        Servicio entidad = servicio(servicio);
        return SelectorTarifa.masEspecifica(tarifaRepository.buscarVigentes(entidad.getId(), dia), municipioId, estrato)
                .map(ComercialMapper::aTarifa)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tarifa vigente de " + servicio.getNombre(),
                        "municipio " + municipioId + ", estrato " + estrato));
    }

    @Override
    public List<TarifaResponse> tarifasVigentes(TipoServicio servicio, LocalDate fecha) {
        LocalDate dia = fecha == null ? LocalDate.now(clock) : fecha;
        List<TipoServicio> servicios = servicio == null ? Arrays.asList(TipoServicio.values()) : List.of(servicio);
        return servicios.stream()
                .flatMap(t -> tarifaRepository.buscarVigentes(servicio(t).getId(), dia).stream())
                .map(ComercialMapper::aTarifa)
                .toList();
    }

    private Servicio servicio(TipoServicio tipo) {
        return servicioRepository.findByNombreIgnoreCase(tipo.getNombre())
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", tipo.getNombre()));
    }

    private Contrato buscar(String numeroContrato) {
        return contratoRepository.findByNumeroContrato(numeroContrato.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Contrato", numeroContrato));
    }
}
