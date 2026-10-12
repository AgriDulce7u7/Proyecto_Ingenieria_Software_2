package co.edu.uniquindio.epq.pqr.servicio.evidencia;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.comun.exportacion.ArchivoGenerado;
import co.edu.uniquindio.epq.pqr.EvidenciaPqrProperties;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.EvidenciaPqr;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.repositorio.EvidenciaPqrRepository;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;
import co.edu.uniquindio.epq.pqr.web.dto.EvidenciaPqrResponse;
import co.edu.uniquindio.epq.seguridad.contexto.ContextoSeguridad;

@Service
public class EvidenciaPqrServiceImpl implements EvidenciaPqrService {

    private final PqrRepository pqrRepository;
    private final EvidenciaPqrRepository evidenciaRepository;
    private final EvidenciaPqrProperties propiedades;
    private final ServicioAuditoria auditoria;
    private final ContextoSeguridad contexto;
    private final Clock clock;

    public EvidenciaPqrServiceImpl(PqrRepository pqrRepository, EvidenciaPqrRepository evidenciaRepository,
                                   EvidenciaPqrProperties propiedades, ServicioAuditoria auditoria,
                                   ContextoSeguridad contexto, Clock clock) {
        this.pqrRepository = pqrRepository;
        this.evidenciaRepository = evidenciaRepository;
        this.propiedades = propiedades;
        this.auditoria = auditoria;
        this.contexto = contexto;
        this.clock = clock;
    }

    @Override
    @Transactional
    public EvidenciaPqrResponse adjuntar(String radicado, MultipartFile archivo, String descripcion) {
        Pqr pqr = buscar(radicado);
        if (pqr.getEstadoTipo() == EstadoPqrTipo.CERRADO) {
            throw new ReglaNegocioException("La PQR " + radicado + " está cerrada y no admite nuevas evidencias");
        }
        validar(archivo);
        String nombre = nombreSeguro(archivo.getOriginalFilename());
        byte[] contenido;
        try {
            contenido = archivo.getBytes();
        } catch (IOException ex) {
            throw new ReglaNegocioException("No fue posible leer el archivo adjunto");
        }
        EvidenciaPqr evidencia = evidenciaRepository.save(EvidenciaPqr.adjuntar(pqr, nombre, archivo.getContentType(),
                contenido, descripcion == null || descripcion.isBlank() ? null : descripcion.trim(),
                contexto.identificadorActual(), LocalDateTime.now(clock)));
        auditoria.registrar(AccionAuditoria.ADJUNTAR_EVIDENCIA_PQR, "pqr", pqr.getRadicado(),
                "Evidencia '%s' (%s, %d bytes)".formatted(nombre, archivo.getContentType(), contenido.length));
        return new EvidenciaPqrResponse(evidencia.getId(), evidencia.getNombreArchivo(), evidencia.getTipoContenido(),
                evidencia.getTamanoBytes(), evidencia.getDescripcion(), evidencia.getFechaCarga(), evidencia.getUsuario());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenciaPqrResponse> listar(String radicado) {
        return evidenciaRepository.listarMetadatos(buscar(radicado).getId());
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoGenerado descargar(String radicado, Long evidenciaId) {
        EvidenciaPqr evidencia = evidenciaRepository.findByIdAndPqrId(evidenciaId, buscar(radicado).getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Evidencia", evidenciaId));
        return new ArchivoGenerado(evidencia.getNombreArchivo(), evidencia.getTipoContenido(), evidencia.getContenido());
    }

    private void validar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ReglaNegocioException("Debe adjuntar un archivo");
        }
        if (archivo.getSize() > propiedades.tamanoMaximoBytes()) {
            throw new ReglaNegocioException("El archivo supera el tamaño máximo de %d MB"
                    .formatted(propiedades.tamanoMaximoBytes() / (1024 * 1024)));
        }
        if (archivo.getContentType() == null || !propiedades.tiposPermitidos().contains(archivo.getContentType())) {
            throw new ReglaNegocioException("Tipo de archivo no permitido (" + archivo.getContentType()
                    + "). Se aceptan PDF, imágenes JPG/PNG y documentos Word");
        }
    }

    /** Elimina rutas y caracteres extraños del nombre recibido del cliente. */
    static String nombreSeguro(String original) {
        String base = original == null || original.isBlank() ? "evidencia" : original;
        base = base.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1);
        base = base.replaceAll("[^A-Za-z0-9._ -]", "_").trim();
        if (base.isEmpty()) {
            base = "evidencia";
        }
        return base.length() > 200 ? base.substring(base.length() - 200) : base;
    }

    private Pqr buscar(String radicado) {
        return pqrRepository.findByRadicado(radicado.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("PQR con radicado", radicado));
    }
}
