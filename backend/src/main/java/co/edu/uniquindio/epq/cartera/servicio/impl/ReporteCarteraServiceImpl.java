package co.edu.uniquindio.epq.cartera.servicio.impl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.cartera.CarteraProperties;
import co.edu.uniquindio.epq.cartera.repositorio.ConsultaCarteraJdbc;
import co.edu.uniquindio.epq.cartera.servicio.CarteraMapper;
import co.edu.uniquindio.epq.cartera.servicio.ReporteCarteraService;
import co.edu.uniquindio.epq.cartera.web.dto.MorosoResponse;
import co.edu.uniquindio.epq.cartera.web.dto.ReporteCarteraResponse;
import co.edu.uniquindio.epq.comun.exportacion.ArchivoGenerado;
import co.edu.uniquindio.epq.comun.exportacion.ColumnaReporte;
import co.edu.uniquindio.epq.comun.exportacion.FabricaExportadores;
import co.edu.uniquindio.epq.comun.exportacion.FormatoExportacion;
import co.edu.uniquindio.epq.comun.exportacion.ReporteTabular;
import co.edu.uniquindio.epq.seguridad.contexto.ContextoSeguridad;

@Service
@Transactional(readOnly = true)
public class ReporteCarteraServiceImpl implements ReporteCarteraService {

    private final ConsultaCarteraJdbc consulta;
    private final FabricaExportadores exportadores;
    private final CarteraProperties propiedades;
    private final ServicioAuditoria auditoria;
    private final ContextoSeguridad contexto;
    private final Clock clock;

    public ReporteCarteraServiceImpl(ConsultaCarteraJdbc consulta, FabricaExportadores exportadores,
                                     CarteraProperties propiedades, ServicioAuditoria auditoria,
                                     ContextoSeguridad contexto, Clock clock) {
        this.consulta = consulta;
        this.exportadores = exportadores;
        this.propiedades = propiedades;
        this.auditoria = auditoria;
        this.contexto = contexto;
        this.clock = clock;
    }

    @Override
    public ReporteCarteraResponse morosos(Integer municipioId, Integer minPeriodos, boolean soloCandidatos) {
        LocalDate hoy = LocalDate.now(clock);
        List<MorosoResponse> filas = consulta.morosos(municipioId).stream()
                .map(f -> CarteraMapper.aMoroso(f, hoy, propiedades.periodosParaSuspension()))
                .filter(m -> minPeriodos == null || m.periodosConsecutivos() >= minPeriodos)
                .filter(m -> !soloCandidatos || m.candidatoSuspension())
                .toList();
        Map<String, BigDecimal> porRango = new LinkedHashMap<>();
        for (String rango : List.of("0-30 días", "31-60 días", "61-90 días", "Más de 90 días")) {
            porRango.put(rango, BigDecimal.ZERO);
        }
        filas.forEach(m -> porRango.merge(m.rangoMora(), m.deuda(), BigDecimal::add));
        return new ReporteCarteraResponse(LocalDateTime.now(clock), filas.size(),
                filas.stream().map(MorosoResponse::codigoUsuario).distinct().count(),
                filas.stream().map(MorosoResponse::deuda).reduce(BigDecimal.ZERO, BigDecimal::add),
                filas.stream().filter(MorosoResponse::candidatoSuspension).count(), porRango, filas);
    }

    @Override
    @Transactional
    public ArchivoGenerado exportar(Integer municipioId, Integer minPeriodos, boolean soloCandidatos,
                                    FormatoExportacion formato) {
        ReporteCarteraResponse reporte = morosos(municipioId, minPeriodos, soloCandidatos);
        List<ColumnaReporte> columnas = List.of(
                ColumnaReporte.texto("Contrato", 11), ColumnaReporte.texto("Servicio", 9),
                ColumnaReporte.texto("Usuario", 18), ColumnaReporte.texto("Identificación", 11),
                ColumnaReporte.texto("Municipio", 9), ColumnaReporte.entero("Facturas", 6),
                ColumnaReporte.entero("Meses consec.", 7), ColumnaReporte.moneda("Deuda", 11),
                ColumnaReporte.fecha("Vence desde", 9), ColumnaReporte.entero("Días mora", 6),
                ColumnaReporte.texto("Rango", 9), ColumnaReporte.booleano("Acuerdo", 6),
                ColumnaReporte.booleano("Cand. suspensión", 7));
        List<List<Object>> filas = new ArrayList<>();
        for (MorosoResponse m : reporte.morosos()) {
            filas.add(List.of(m.numeroContrato(), m.servicio(), m.nombreUsuario(), m.documentoUsuario(),
                    m.municipio() == null ? "" : m.municipio(), m.facturasVencidas(), m.periodosConsecutivos(),
                    m.deuda(), m.vencimientoMasAntiguo(), m.antiguedadDias(), m.rangoMora(), m.acuerdoVigente(),
                    m.candidatoSuspension()));
        }
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("Contratos en mora", reporte.contratosEnMora());
        resumen.put("Usuarios en mora", reporte.usuariosEnMora());
        resumen.put("Deuda total", reporte.deudaTotal());
        resumen.put("Candidatos a suspensión", reporte.candidatosSuspension());
        reporte.deudaPorRango().forEach((rango, valor) -> resumen.put("Deuda " + rango, valor));

        String subtitulo = "Usuarios con facturas vencidas sin acuerdo de pago vigente"
                + (municipioId == null ? "" : " · municipio " + municipioId)
                + (soloCandidatos ? " · solo candidatos a suspensión" : "");
        ReporteTabular tabular = new ReporteTabular(
                "cartera-morosa-" + LocalDate.now(clock), "Reporte de cartera morosa", subtitulo,
                reporte.generadoEn(), contexto.identificadorActual(), columnas, filas, resumen);
        ArchivoGenerado archivo = exportadores.exportar(tabular, formato);
        auditoria.registrar(AccionAuditoria.EXPORTAR_REPORTE_CARTERA, "reporte", formato.name(),
                "Reporte de cartera exportado en %s con %d contratos y deuda total %s"
                        .formatted(formato, reporte.contratosEnMora(), reporte.deudaTotal()));
        return archivo;
    }
}
