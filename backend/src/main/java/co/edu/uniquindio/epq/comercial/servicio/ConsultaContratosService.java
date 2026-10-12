package co.edu.uniquindio.epq.comercial.servicio;

import java.time.LocalDate;
import java.util.List;

import co.edu.uniquindio.epq.comercial.dominio.EstadoContrato;
import co.edu.uniquindio.epq.comercial.dominio.TipoServicio;
import co.edu.uniquindio.epq.comercial.web.dto.ContratoDetalleResponse;
import co.edu.uniquindio.epq.comercial.web.dto.ContratoResumenResponse;
import co.edu.uniquindio.epq.comercial.web.dto.HistorialContratoResponse;
import co.edu.uniquindio.epq.comercial.web.dto.TarifaResponse;

public interface ConsultaContratosService {

    List<ContratoResumenResponse> listar(EstadoContrato estado, TipoServicio servicio, String codigoUsuario,
                                         Integer municipioId, Boolean candidatoSuspension);

    ContratoDetalleResponse obtener(String numeroContrato);

    List<HistorialContratoResponse> historial(String numeroContrato);

    /** SWR-18: tarifa que se aplicaría a un predio con ese servicio, municipio y estrato. */
    TarifaResponse tarifaAplicable(TipoServicio servicio, Integer municipioId, Integer estrato, LocalDate fecha);

    List<TarifaResponse> tarifasVigentes(TipoServicio servicio, LocalDate fecha);
}
