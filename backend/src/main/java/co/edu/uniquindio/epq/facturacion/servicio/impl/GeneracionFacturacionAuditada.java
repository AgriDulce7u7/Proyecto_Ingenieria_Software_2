package co.edu.uniquindio.epq.facturacion.servicio.impl;

import java.time.YearMonth;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.facturacion.servicio.GeneracionFacturacionService;
import co.edu.uniquindio.epq.facturacion.web.dto.ResultadoGeneracionLoteResponse;
import co.edu.uniquindio.epq.seguridad.contexto.ContextoSeguridad;

/**
 * Patrón <b>Decorator</b> (SWR-10): agrega el registro en el log de auditoría a la generación masiva de
 * facturas sin modificar {@link GeneracionFacturacionServiceImpl} (OCP). Al ser {@code @Primary}, el
 * controlador y la tarea programada reciben esta versión decorada.
 *
 * <p>Registra fecha, hora de inicio y fin, cantidad de facturas generadas y el resultado; si la
 * ejecución falla, el error también queda auditado.</p>
 */
@Service
@Primary
public class GeneracionFacturacionAuditada implements GeneracionFacturacionService {

    private final GeneracionFacturacionServiceImpl delegado;
    private final ServicioAuditoria auditoria;
    private final ContextoSeguridad contexto;

    public GeneracionFacturacionAuditada(GeneracionFacturacionServiceImpl delegado, ServicioAuditoria auditoria,
                                         ContextoSeguridad contexto) {
        this.delegado = delegado;
        this.auditoria = auditoria;
        this.contexto = contexto;
    }

    @Override
    public ResultadoGeneracionLoteResponse generarLote(YearMonth periodo, String origen) {
        String usuario = ORIGEN_AUTOMATICO.equals(origen) ? ContextoSeguridad.USUARIO_SISTEMA : contexto.identificadorActual();
        String rol = contexto.usuarioActual().map(u -> u.rol().name()).orElse(null);
        try {
            ResultadoGeneracionLoteResponse r = delegado.generarLote(periodo, origen);
            auditoria.registrarIndependiente(usuario, rol, AccionAuditoria.GENERAR_LOTE_FACTURAS, "lote_facturacion",
                    r.periodo(), ("Origen %s. Inicio %s, fin %s (%d ms, dentro de 60 min: %s). Estado del lote: %s. "
                            + "Facturas generadas: %d, ya existentes: %d, total del lote: %d. Incidencias: %d. "
                            + "Sincronizadas con el ERP: %d de %d.").formatted(
                            r.origen(), r.inicio(), r.fin(), r.duracionMs(), r.dentroDelTiempoMaximo() ? "sí" : "no",
                            r.estadoLote(), r.facturasGeneradas(), r.facturasYaExistentes(), r.totalFacturasDelLote(),
                            r.incidencias().size(), r.sincronizacionErp().exitosas(),
                            r.sincronizacionErp().facturasProcesadas()), true);
            return r;
        } catch (RuntimeException ex) {
            auditoria.registrarIndependiente(usuario, rol, AccionAuditoria.GENERAR_LOTE_FACTURAS, "lote_facturacion",
                    periodo == null ? delegado.periodoPorDefecto() : periodo,
                    "La generación del lote falló: " + ex.getMessage(), false);
            throw ex;
        }
    }

    @Override
    public YearMonth periodoPorDefecto() {
        return delegado.periodoPorDefecto();
    }
}
