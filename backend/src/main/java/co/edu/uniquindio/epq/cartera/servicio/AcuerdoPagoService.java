package co.edu.uniquindio.epq.cartera.servicio;

import java.time.LocalDate;
import java.util.List;

import co.edu.uniquindio.epq.cartera.dominio.EstadoAcuerdo;
import co.edu.uniquindio.epq.cartera.web.dto.AcuerdoPagoResponse;
import co.edu.uniquindio.epq.cartera.web.dto.PagarCuotaRequest;
import co.edu.uniquindio.epq.cartera.web.dto.PlanCuotasResponse;
import co.edu.uniquindio.epq.cartera.web.dto.ProcesoCobroResponse;
import co.edu.uniquindio.epq.cartera.web.dto.RegistrarAcuerdoRequest;

/**
 * RF-12 / CU-08: acuerdos de pago para usuarios morosos.
 */
public interface AcuerdoPagoService {

    PlanCuotasResponse simular(String numeroContrato, Integer numeroCuotas, LocalDate fechaPrimeraCuota);

    AcuerdoPagoResponse registrar(RegistrarAcuerdoRequest solicitud);

    AcuerdoPagoResponse pagarCuota(String numeroAcuerdo, int numeroCuota, PagarCuotaRequest solicitud);

    List<AcuerdoPagoResponse> listar(EstadoAcuerdo estado, String numeroContrato);

    AcuerdoPagoResponse obtener(String numeroAcuerdo);

    List<ProcesoCobroResponse> procesosCobro(co.edu.uniquindio.epq.cartera.dominio.EstadoProcesoCobro estado);
}
