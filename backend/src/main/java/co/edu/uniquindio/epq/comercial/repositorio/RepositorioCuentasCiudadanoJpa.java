package co.edu.uniquindio.epq.comercial.repositorio;

import java.util.Optional;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.seguridad.servicio.CuentaCiudadano;
import co.edu.uniquindio.epq.seguridad.servicio.RepositorioCuentasCiudadano;

/**
 * Adaptador del puerto de seguridad: la cuenta del portal es el propio usuario del servicio.
 */
@Component
public class RepositorioCuentasCiudadanoJpa implements RepositorioCuentasCiudadano {

    private final ClienteRepository clienteRepository;

    public RepositorioCuentasCiudadanoJpa(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Optional<CuentaCiudadano> buscarPorNumeroDocumento(String numeroDocumento) {
        return clienteRepository.findByNumeroDocumento(numeroDocumento).map(c -> c);
    }
}
