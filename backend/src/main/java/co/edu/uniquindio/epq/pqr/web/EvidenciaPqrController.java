package co.edu.uniquindio.epq.pqr.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import co.edu.uniquindio.epq.pqr.servicio.evidencia.EvidenciaPqrService;
import co.edu.uniquindio.epq.pqr.web.dto.EvidenciaPqrResponse;
import co.edu.uniquindio.epq.seguridad.acceso.AccesoGestionPqr;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/pqr/{radicado}/evidencias")
@Tag(name = "F-02 PQR", description = "Registro y seguimiento de Peticiones, Quejas y Reclamos")
@AccesoGestionPqr
public class EvidenciaPqrController {

    private final EvidenciaPqrService evidenciaService;

    public EvidenciaPqrController(EvidenciaPqrService evidenciaService) {
        this.evidenciaService = evidenciaService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adjuntar evidencia documental (PDF, JPG, PNG o Word, máx. 5 MB)",
            description = "RN-06: la respuesta requiere al menos una evidencia")
    public EvidenciaPqrResponse adjuntar(@PathVariable String radicado, @RequestPart("archivo") MultipartFile archivo,
                                         @RequestParam(required = false) String descripcion) {
        return evidenciaService.adjuntar(radicado, archivo, descripcion);
    }

    @GetMapping
    @Operation(summary = "Evidencias de la PQR")
    public List<EvidenciaPqrResponse> listar(@PathVariable String radicado) {
        return evidenciaService.listar(radicado);
    }

    @GetMapping("/{evidenciaId}/archivo")
    @Operation(summary = "Descargar una evidencia")
    public ResponseEntity<byte[]> descargar(@PathVariable String radicado, @PathVariable Long evidenciaId) {
        return evidenciaService.descargar(radicado, evidenciaId).comoDescarga();
    }
}
