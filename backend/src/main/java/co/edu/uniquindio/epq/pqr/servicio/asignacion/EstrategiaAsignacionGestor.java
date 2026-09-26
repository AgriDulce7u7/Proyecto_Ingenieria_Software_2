package co.edu.uniquindio.epq.pqr.servicio.asignacion;

import java.util.Optional;

import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;

/**
 * Patrón <b>Strategy</b>: criterio para asignar automáticamente una PQR a un gestor.
 * Se puede reemplazar (por municipio, por especialidad, round-robin...) sin tocar el
 * servicio de registro (OCP/DIP).
 */
public interface EstrategiaAsignacionGestor {

    Optional<Gestor> seleccionarPara(Pqr pqr);
}
