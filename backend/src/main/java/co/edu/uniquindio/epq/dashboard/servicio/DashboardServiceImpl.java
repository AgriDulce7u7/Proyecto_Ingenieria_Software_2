package co.edu.uniquindio.epq.dashboard.servicio;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.dashboard.indicadores.ProveedorIndicadores;
import co.edu.uniquindio.epq.dashboard.web.dto.CategoriaIndicadoresResponse;
import co.edu.uniquindio.epq.dashboard.web.dto.DashboardResponse;

/**
 * Compone los indicadores de todos los proveedores. Se calculan en cada consulta directamente sobre los
 * datos operativos, por lo que reflejan los cambios al instante sin procesos manuales (SWR-34).
 */
@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final List<ProveedorIndicadores> proveedores;
    private final Clock clock;

    public DashboardServiceImpl(List<ProveedorIndicadores> proveedores, Clock clock) {
        this.proveedores = proveedores.stream().sorted(Comparator.comparingInt(ProveedorIndicadores::orden)).toList();
        this.clock = clock;
    }

    @Override
    public DashboardResponse consultar() {
        LocalDate hoy = LocalDate.now(clock);
        List<CategoriaIndicadoresResponse> categorias = proveedores.stream()
                .map(p -> new CategoriaIndicadoresResponse(p.categoria(), p.calcular(hoy)))
                .toList();
        return new DashboardResponse(LocalDateTime.now(clock), YearMonth.from(hoy).toString(), categorias);
    }

    @Override
    public CategoriaIndicadoresResponse categoria(String nombre) {
        LocalDate hoy = LocalDate.now(clock);
        return proveedores.stream()
                .filter(p -> p.categoria().equalsIgnoreCase(nombre.trim()))
                .findFirst()
                .map(p -> new CategoriaIndicadoresResponse(p.categoria(), p.calcular(hoy)))
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría de indicadores", nombre));
    }
}
