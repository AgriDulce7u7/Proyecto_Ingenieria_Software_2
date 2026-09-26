package co.edu.uniquindio.epq.pqr.servicio;

import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitudTipo;

/**
 * Criterios opcionales para listar PQR. Cualquier campo nulo se ignora.
 */
public record FiltroPqr(EstadoPqrTipo estado, TipoSolicitudTipo tipo, Integer gestorId, String documentoCiudadano) {
}
