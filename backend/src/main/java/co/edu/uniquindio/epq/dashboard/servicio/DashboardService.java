package co.edu.uniquindio.epq.dashboard.servicio;

import co.edu.uniquindio.epq.dashboard.web.dto.CategoriaIndicadoresResponse;
import co.edu.uniquindio.epq.dashboard.web.dto.DashboardResponse;

/**
 * F-08 / CU-09: dashboard gerencial.
 */
public interface DashboardService {

    DashboardResponse consultar();

    CategoriaIndicadoresResponse categoria(String nombre);
}
