package co.edu.uniquindio.epq.pqr.servicio.ubicacion;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.comercial.dominio.Contrato;
import co.edu.uniquindio.epq.comercial.repositorio.ContratoRepository;
import co.edu.uniquindio.epq.comun.territorio.Municipio;
import co.edu.uniquindio.epq.comun.territorio.ResolutorMunicipios;

/**
 * Determina el municipio y el contrato de una PQR. Si se informa un contrato, debe existir y estar
 * activo (CU-06 4a) y su predio define el municipio; si no, se usa el municipio informado (CO-03).
 */
@Component
public class ResolutorUbicacionPqr {

    private final ContratoRepository contratoRepository;
    private final ResolutorMunicipios municipios;

    public ResolutorUbicacionPqr(ContratoRepository contratoRepository, ResolutorMunicipios municipios) {
        this.contratoRepository = contratoRepository;
        this.municipios = municipios;
    }

    public UbicacionPqr resolver(String numeroContrato, Integer municipioId) {
        if (numeroContrato != null && !numeroContrato.isBlank()) {
            String numero = numeroContrato.trim().toUpperCase();
            Contrato contrato = contratoRepository.findByNumeroContrato(numero)
                    .filter(Contrato::esFacturable)
                    .orElseThrow(() -> new ContratoNoEncontradoException(numero));
            Municipio municipio = contrato.getPredio() != null ? contrato.getPredio().getMunicipio()
                    : municipioId == null ? null : municipios.atendido(municipioId);
            return new UbicacionPqr(municipio, contrato);
        }
        return new UbicacionPqr(municipioId == null ? null : municipios.atendido(municipioId), null);
    }
}
