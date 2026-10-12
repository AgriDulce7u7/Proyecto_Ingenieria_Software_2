package co.edu.uniquindio.epq.comercial.servicio;

import java.util.List;

import co.edu.uniquindio.epq.comercial.dominio.Cliente;
import co.edu.uniquindio.epq.comercial.dominio.Contrato;
import co.edu.uniquindio.epq.comercial.dominio.HistorialContrato;
import co.edu.uniquindio.epq.comercial.dominio.Medidor;
import co.edu.uniquindio.epq.comercial.dominio.Predio;
import co.edu.uniquindio.epq.comercial.dominio.Tarifa;
import co.edu.uniquindio.epq.comercial.web.dto.ContratoDetalleResponse;
import co.edu.uniquindio.epq.comercial.web.dto.ContratoResumenResponse;
import co.edu.uniquindio.epq.comercial.web.dto.HistorialContratoResponse;
import co.edu.uniquindio.epq.comercial.web.dto.MedidorResponse;
import co.edu.uniquindio.epq.comercial.web.dto.PredioResponse;
import co.edu.uniquindio.epq.comercial.web.dto.TarifaResponse;
import co.edu.uniquindio.epq.comercial.web.dto.UsuarioDetalleResponse;
import co.edu.uniquindio.epq.comercial.web.dto.UsuarioResumenResponse;

/**
 * Conversión entidad → DTO del módulo comercial.
 */
public final class ComercialMapper {

    private ComercialMapper() {
    }

    public static UsuarioResumenResponse aResumen(Cliente c) {
        return new UsuarioResumenResponse(c.getId(), c.getCodigoInterno(), c.getTipoDocumento(), c.getNumeroDocumento(),
                c.getNombreCompleto(), c.getMunicipio() == null ? null : c.getMunicipio().getNombre(), c.getTelefono(),
                c.getCorreo(), c.getEstadoValidacion().name(), c.isActivo());
    }

    public static UsuarioDetalleResponse aDetalle(Cliente c, List<Predio> predios, List<Contrato> contratos) {
        return new UsuarioDetalleResponse(c.getId(), c.getCodigoInterno(), c.getTipoPersona().name(),
                c.getTipoDocumento(), c.getNumeroDocumento(), c.getNombreCompleto(), c.getTelefono(), c.getCorreo(),
                c.getDireccionNotificacion(), c.getMunicipio() == null ? null : c.getMunicipio().getId(),
                c.getMunicipio() == null ? null : c.getMunicipio().getNombre(), c.getEstadoValidacion().name(),
                c.getDetalleValidacion(), c.getFechaValidacion(), c.isActivo(), c.tieneCuentaPortal(),
                c.getFechaRegistro(), c.getFechaActualizacion(), c.getRegistradoPor(),
                predios.stream().map(ComercialMapper::aPredio).toList(),
                contratos.stream().map(ComercialMapper::aResumen).toList());
    }

    public static PredioResponse aPredio(Predio p) {
        return new PredioResponse(p.getId(), p.getCodigoPredio(), p.getDireccion(), p.getMunicipio().getId(),
                p.getMunicipio().getNombre(), p.getEstrato(), p.getUso().name());
    }

    public static ContratoResumenResponse aResumen(Contrato c) {
        return new ContratoResumenResponse(c.getNumeroContrato(), c.getServicio().getNombre(), c.getDireccionServicio(),
                c.getPredio() == null ? null : c.getPredio().getMunicipio().getNombre(), c.getEstado().name(),
                c.isCandidatoSuspension());
    }

    public static TarifaResponse aTarifa(Tarifa t) {
        if (t == null) {
            return null;
        }
        String municipio = t.getMunicipio() == null ? null : t.getMunicipio().getNombre();
        String criterio = (t.getEstrato() == null ? "Todos los estratos" : "Estrato " + t.getEstrato())
                + " · " + (municipio == null ? "Todos los municipios" : municipio);
        return new TarifaResponse(t.getId(), t.getNombre(), t.getServicio().getNombre(), t.getValorPorUnidad(),
                t.getCargoFijo(), municipio, t.getEstrato(), criterio, t.getFechaVigenciaInicio(), t.getFechaVigenciaFin());
    }

    public static MedidorResponse aMedidor(Medidor m) {
        return m == null ? null
                : new MedidorResponse(m.getNumeroSerie(), m.getFechaInstalacion(), m.getEstado(), m.getFechaRetiro());
    }

    public static HistorialContratoResponse aHistorial(HistorialContrato h) {
        return new HistorialContratoResponse(h.getEstadoAnterior() == null ? null : h.getEstadoAnterior().name(),
                h.getEstadoNuevo().name(), h.getMotivo(), h.getUsuario(), h.getFecha());
    }

    public static ContratoDetalleResponse aDetalle(Contrato c, Medidor medidorActivo, List<HistorialContrato> historial,
                                                   List<String> advertencias) {
        return new ContratoDetalleResponse(c.getId(), c.getNumeroContrato(), c.getEstado().name(),
                c.getServicio().getNombre(), c.tipoServicio().requiereMedidor(), c.getCliente().getCodigoInterno(),
                c.getCliente().getNombreCompleto(), c.getCliente().getNumeroDocumento(),
                c.getPredio() == null ? null : aPredio(c.getPredio()), aTarifa(c.getTarifa()), aMedidor(medidorActivo),
                c.getFechaInicio(), c.getFechaRegistro(), c.getRegistradoPor(), c.isCandidatoSuspension(),
                c.getEstado().transicionesPermitidas().stream().map(Enum::name).sorted().toList(),
                historial.stream().map(ComercialMapper::aHistorial).toList(), advertencias);
    }
}
