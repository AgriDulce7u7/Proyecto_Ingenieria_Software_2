package co.edu.uniquindio.epq.comercial.servicio;

import co.edu.uniquindio.epq.comercial.web.dto.AsignarMedidorRequest;
import co.edu.uniquindio.epq.comercial.web.dto.CambiarEstadoContratoRequest;
import co.edu.uniquindio.epq.comercial.web.dto.ContratoDetalleResponse;
import co.edu.uniquindio.epq.comercial.web.dto.RegistrarContratoRequest;

/**
 * F-04 / CU-02: registro y administración de contratos de servicio.
 */
public interface GestionContratosService {

    ContratoDetalleResponse registrar(RegistrarContratoRequest solicitud);

    ContratoDetalleResponse cambiarEstado(String numeroContrato, CambiarEstadoContratoRequest solicitud);

    ContratoDetalleResponse asignarMedidor(String numeroContrato, AsignarMedidorRequest solicitud);
}
