package co.edu.uniquindio.epq.cartera.servicio;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import co.edu.uniquindio.epq.cartera.dominio.AcuerdoPago;
import co.edu.uniquindio.epq.cartera.dominio.CalculadoraMora;
import co.edu.uniquindio.epq.cartera.dominio.CuotaAcuerdo;
import co.edu.uniquindio.epq.cartera.dominio.CuotaPlan;
import co.edu.uniquindio.epq.cartera.dominio.ProcesoCobro;
import co.edu.uniquindio.epq.cartera.repositorio.FilaMorosidad;
import co.edu.uniquindio.epq.cartera.web.dto.AcuerdoPagoResponse;
import co.edu.uniquindio.epq.cartera.web.dto.CuotaResponse;
import co.edu.uniquindio.epq.cartera.web.dto.MorosoResponse;
import co.edu.uniquindio.epq.cartera.web.dto.ProcesoCobroResponse;

public final class CarteraMapper {

    private CarteraMapper() {
    }

    public static MorosoResponse aMoroso(FilaMorosidad f, LocalDate hoy, int umbralSuspension) {
        int consecutivos = CalculadoraMora.periodosConsecutivos(f.periodosVencidos());
        long antiguedad = CalculadoraMora.antiguedadDias(f.vencimientoMasAntiguo(), hoy);
        boolean candidato = "ACTIVO".equals(f.estadoContrato())
                && CalculadoraMora.esCandidatoSuspension(consecutivos, f.acuerdoVigente(), umbralSuspension);
        return new MorosoResponse(f.numeroContrato(), f.servicio(), f.estadoContrato(), f.codigoUsuario(),
                f.tipoDocumento() + " " + f.numeroDocumento(), f.nombreUsuario(), f.telefono(), f.correo(),
                f.municipio(), f.facturasVencidas(), f.periodosVencidos().stream().map(YearMonth::toString).toList(),
                consecutivos, f.deuda(), f.vencimientoMasAntiguo(), antiguedad, CalculadoraMora.rangoMora(antiguedad),
                f.acuerdoVigente(), f.estadoProcesoCobro(), candidato);
    }

    public static CuotaResponse aCuota(CuotaAcuerdo c) {
        return new CuotaResponse(c.getNumeroCuota(), c.getValor(), c.getFechaVencimiento(), c.getEstado().name(),
                c.getFechaPago(), c.getCanalPago() == null ? null : c.getCanalPago().name(), c.getReferenciaPago());
    }

    public static CuotaResponse aCuota(CuotaPlan c) {
        return new CuotaResponse(c.numero(), c.valor(), c.fechaVencimiento(), "PENDIENTE", null, null, null);
    }

    public static AcuerdoPagoResponse aAcuerdo(AcuerdoPago a, List<String> facturas) {
        var cliente = a.getContrato().getCliente();
        return new AcuerdoPagoResponse(a.getNumeroAcuerdo(), a.getContrato().getNumeroContrato(),
                a.getContrato().getServicio().getNombre(), cliente.getNombreCompleto(),
                cliente.getTipoDocumento() + " " + cliente.getNumeroDocumento(), a.getFechaAcuerdo(), a.getTotalDeuda(),
                a.valorPagado(), a.saldoPendiente(), a.getNumeroCuotas(), a.getEstado().name(), a.getFechaCambioEstado(),
                a.getObservacion(), a.getRegistradoPor(), facturas,
                a.getCuotas().stream().map(CarteraMapper::aCuota).toList());
    }

    public static ProcesoCobroResponse aProceso(ProcesoCobro p) {
        return new ProcesoCobroResponse(p.getContrato().getNumeroContrato(), p.getContrato().getCliente().getNombreCompleto(),
                p.getContrato().getServicio().getNombre(), p.getEstado().name(), p.getFechaInicio(),
                p.getFechaActualizacion(), p.getMotivo());
    }
}
