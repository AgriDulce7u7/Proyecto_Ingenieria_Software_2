package co.edu.uniquindio.epq.comun.territorio;

import org.springframework.stereotype.Component;

/**
 * Obtiene un municipio atendido por EPQ validando que exista y esté activo (CO-03).
 */
@Component
public class ResolutorMunicipios {

    private final MunicipioRepository municipioRepository;

    public ResolutorMunicipios(MunicipioRepository municipioRepository) {
        this.municipioRepository = municipioRepository;
    }

    public Municipio atendido(Integer municipioId) {
        return municipioRepository.findById(municipioId)
                .filter(Municipio::isActivo)
                .orElseThrow(() -> new MunicipioNoAtendidoException(municipioId));
    }
}
