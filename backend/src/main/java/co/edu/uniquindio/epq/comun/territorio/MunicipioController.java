package co.edu.uniquindio.epq.comun.territorio;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/municipios")
@Tag(name = "Catálogos", description = "Catálogos compartidos")
public class MunicipioController {

    private final MunicipioRepository municipioRepository;

    public MunicipioController(MunicipioRepository municipioRepository) {
        this.municipioRepository = municipioRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Municipios atendidos por EPQ (público)")
    public List<MunicipioResponse> listar() {
        return municipioRepository.findByActivoTrueOrderByNombreAsc().stream().map(MunicipioResponse::de).toList();
    }
}
