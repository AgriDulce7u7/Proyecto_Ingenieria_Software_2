package co.edu.uniquindio.epq.cartera.servicio.monitoreo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.cartera.CarteraProperties;
import co.edu.uniquindio.epq.cartera.dominio.CalculadoraMora;
import co.edu.uniquindio.epq.cartera.repositorio.ConsultaCarteraJdbc;
import co.edu.uniquindio.epq.cartera.repositorio.FilaMorosidad;
import co.edu.uniquindio.epq.comercial.dominio.Contrato;
import co.edu.uniquindio.epq.comercial.repositorio.ContratoRepository;
import co.edu.uniquindio.epq.comercial.repositorio.ContratoSpecifications;
import co.edu.uniquindio.epq.comun.notificacion.NotificacionDiferidaEvento;
import co.edu.uniquindio.epq.comun.notificacion.TipoNotificacionSistema;
import co.edu.uniquindio.epq.seguridad.Rol;

/**
 * SWR-32 / RN-03: marca como candidato a suspensión a todo contrato activo con 2 o más períodos
 * consecutivos de mora sin acuerdo de pago vigente y notifica al Gestor de Cartera. Desmarca los que
 * dejaron de cumplir la condición.
 */
@Component
public class PasoCandidatosSuspension implements PasoMonitoreoCartera {

    private final ConsultaCarteraJdbc consulta;
    private final ContratoRepository contratoRepository;
    private final CarteraProperties propiedades;
    private final ServicioAuditoria auditoria;
    private final ApplicationEventPublisher eventos;

    public PasoCandidatosSuspension(ConsultaCarteraJdbc consulta, ContratoRepository contratoRepository,
                                    CarteraProperties propiedades, ServicioAuditoria auditoria,
                                    ApplicationEventPublisher eventos) {
        this.consulta = consulta;
        this.contratoRepository = contratoRepository;
        this.propiedades = propiedades;
        this.auditoria = auditoria;
        this.eventos = eventos;
    }

    @Override
    public int orden() {
        return 40;
    }

    @Override
    public String nombre() {
        return "Candidatos a suspensión";
    }

    @Override
    @Transactional
    public ResultadoPaso ejecutar(LocalDate hoy, LocalDateTime ahora) {
        int umbral = propiedades.periodosParaSuspension();
        List<FilaMorosidad> candidatos = consulta.morosos(null).stream()
                .filter(f -> "ACTIVO".equals(f.estadoContrato()))
                .filter(f -> CalculadoraMora.esCandidatoSuspension(
                        CalculadoraMora.periodosConsecutivos(f.periodosVencidos()), f.acuerdoVigente(), umbral))
                .toList();
        Set<Integer> idsCandidatos = candidatos.stream().map(FilaMorosidad::contratoId).collect(Collectors.toSet());

        List<String> nuevos = new ArrayList<>();
        for (FilaMorosidad fila : candidatos) {
            Contrato contrato = contratoRepository.findById(fila.contratoId()).orElseThrow();
            if (contrato.actualizarCandidatoSuspension(true, ahora)) {
                nuevos.add(contrato.getNumeroContrato());
                int meses = CalculadoraMora.periodosConsecutivos(fila.periodosVencidos());
                auditoria.registrar(AccionAuditoria.MARCAR_CANDIDATO_SUSPENSION, "contrato", contrato.getNumeroContrato(),
                        "%d períodos consecutivos de mora (%s), deuda %s, sin acuerdo vigente"
                                .formatted(meses, fila.periodosVencidos(), fila.deuda()));
                eventos.publishEvent(NotificacionDiferidaEvento.aRol(Rol.GESTOR_CARTERA.name(),
                        TipoNotificacionSistema.CANDIDATO_SUSPENSION,
                        "SIGCA-EPQ - Candidato a suspensión " + contrato.getNumeroContrato(),
                        ("El contrato %s de %s (%s) acumula %d períodos consecutivos de mora por %s sin acuerdo de pago. "
                                + "Es candidato a suspensión del servicio previa notificación (RN-03).")
                                .formatted(contrato.getNumeroContrato(), fila.nombreUsuario(), fila.servicio(), meses,
                                        fila.deuda()), contrato.getNumeroContrato()));
            }
        }
        int desmarcados = 0;
        for (Contrato marcado : contratoRepository.findAll(ContratoSpecifications.candidatosSuspension(), Sort.by("id"))) {
            if (!idsCandidatos.contains(marcado.getId()) && marcado.actualizarCandidatoSuspension(false, ahora)) {
                desmarcados++;
            }
        }
        return new ResultadoPaso(nuevos.size() + desmarcados, "%d candidatos (%d nuevos: %s), %d desmarcados"
                .formatted(candidatos.size(), nuevos.size(), String.join(", ", nuevos), desmarcados));
    }
}
