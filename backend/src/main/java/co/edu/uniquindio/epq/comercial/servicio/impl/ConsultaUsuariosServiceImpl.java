package co.edu.uniquindio.epq.comercial.servicio.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.comercial.dominio.Cliente;
import co.edu.uniquindio.epq.comercial.repositorio.ClienteRepository;
import co.edu.uniquindio.epq.comercial.repositorio.ContratoRepository;
import co.edu.uniquindio.epq.comercial.repositorio.PredioRepository;
import co.edu.uniquindio.epq.comercial.servicio.ComercialMapper;
import co.edu.uniquindio.epq.comercial.servicio.ConsultaUsuariosService;
import co.edu.uniquindio.epq.comercial.web.dto.PredioResponse;
import co.edu.uniquindio.epq.comercial.web.dto.UsuarioDetalleResponse;
import co.edu.uniquindio.epq.comercial.web.dto.UsuarioResumenResponse;
import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;

@Service
@Transactional(readOnly = true)
public class ConsultaUsuariosServiceImpl implements ConsultaUsuariosService {

    /** Límite de resultados: mantiene la respuesta por debajo de 3 segundos (SWR-16). */
    private static final Pageable LIMITE = PageRequest.of(0, 50);
    private static final int LONGITUD_MINIMA = 3;

    private final ClienteRepository clienteRepository;
    private final PredioRepository predioRepository;
    private final ContratoRepository contratoRepository;

    public ConsultaUsuariosServiceImpl(ClienteRepository clienteRepository, PredioRepository predioRepository,
                                       ContratoRepository contratoRepository) {
        this.clienteRepository = clienteRepository;
        this.predioRepository = predioRepository;
        this.contratoRepository = contratoRepository;
    }

    /**
     * Busca por uno o varios criterios. Si se envían varios, el resultado es la intersección.
     */
    @Override
    public List<UsuarioResumenResponse> buscar(String identificacion, String nombre, String direccion) {
        List<List<Cliente>> resultados = new ArrayList<>();
        if (tieneValor(identificacion)) {
            resultados.add(clienteRepository.findByNumeroDocumentoStartingWithOrderByNombreCompletoAsc(
                    identificacion.trim(), LIMITE));
        }
        if (tieneValor(nombre)) {
            resultados.add(clienteRepository.buscarPorNombre(validarLongitud(nombre, "nombre"), LIMITE));
        }
        if (tieneValor(direccion)) {
            resultados.add(clienteRepository.buscarPorDireccion(validarLongitud(direccion, "direccion"), LIMITE));
        }
        if (resultados.isEmpty()) {
            throw new IllegalArgumentException("Indique al menos un criterio de búsqueda: identificacion, nombre o direccion");
        }
        Map<Integer, Cliente> interseccion = new LinkedHashMap<>();
        resultados.get(0).forEach(c -> interseccion.put(c.getId(), c));
        for (List<Cliente> otro : resultados.subList(1, resultados.size())) {
            Set<Integer> ids = otro.stream().map(Cliente::getId).collect(Collectors.toSet());
            interseccion.keySet().retainAll(ids);
        }
        return interseccion.values().stream().map(ComercialMapper::aResumen).toList();
    }

    @Override
    public UsuarioDetalleResponse obtener(String codigoInterno) {
        Cliente cliente = buscarCliente(codigoInterno);
        return ComercialMapper.aDetalle(cliente, predioRepository.findByClienteIdOrderByIdAsc(cliente.getId()),
                contratoRepository.findByClienteIdOrderByNumeroContratoAsc(cliente.getId()));
    }

    @Override
    public List<PredioResponse> predios(String codigoInterno) {
        Cliente cliente = buscarCliente(codigoInterno);
        return predioRepository.findByClienteIdOrderByIdAsc(cliente.getId()).stream().map(ComercialMapper::aPredio).toList();
    }

    private Cliente buscarCliente(String codigoInterno) {
        return clienteRepository.findByCodigoInterno(codigoInterno.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario del servicio", codigoInterno));
    }

    private static boolean tieneValor(String valor) {
        return valor != null && !valor.isBlank();
    }

    private static String validarLongitud(String valor, String criterio) {
        String limpio = valor.trim();
        if (limpio.length() < LONGITUD_MINIMA) {
            throw new IllegalArgumentException("El criterio '%s' debe tener al menos %d caracteres"
                    .formatted(criterio, LONGITUD_MINIMA));
        }
        return limpio;
    }
}
