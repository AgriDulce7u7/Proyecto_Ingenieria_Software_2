import { Link } from "react-router-dom";
import { Boton } from "../../../componentes/ui/Boton";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import { presentarEstadoProceso, tonoRangoMora } from "../../../presentacion/cartera";
import { formatearMoneda } from "../../../presentacion/facturacion";
import { formatearFechaCorta } from "../../../presentacion/fechas";
import { useSesion } from "../../../sesion/useSesion";

/** Situación de cobro del contrato: candidato a suspensión, acuerdo vigente o estado del cobro coercitivo. */
function SituacionCobro({ moroso }) {
  if (moroso.candidatoSuspension) {
    return (
      <span className="cartera-candidato">
        <Icono nombre="alerta" tamano={14} />
        Candidato a suspensión
      </span>
    );
  }
  if (moroso.estadoContrato === "SUSPENDIDO") return <Etiqueta tono="peligro">Servicio suspendido</Etiqueta>;
  if (moroso.acuerdoVigente) return <Etiqueta tono="info">Acuerdo vigente</Etiqueta>;
  if (moroso.estadoProcesoCobro) {
    const proceso = presentarEstadoProceso(moroso.estadoProcesoCobro);
    return <Etiqueta tono={proceso.tono}>{proceso.etiqueta}</Etiqueta>;
  }
  return <span className="cartera-sin-gestion">Sin gestión</span>;
}

/**
 * Contratos en mora con los estilos de la tabla de la bandeja (tarjetas en móvil). Quien gestiona la
 * cartera puede abrir un acuerdo de pago desde la fila si el contrato aún no tiene uno vigente.
 */
export function TablaMorosos({ morosos }) {
  const puedeGestionar = useSesion().puede("CARTERA_GESTIONAR");

  return (
    <div className="bandeja-tabla-contenedor">
      <table className="bandeja-tabla cartera-tabla">
        <thead>
          <tr>
            <th scope="col">Contrato</th>
            <th scope="col">Usuario</th>
            <th scope="col">Deuda</th>
            <th scope="col">Antigüedad</th>
            <th scope="col">Periodos seguidos</th>
            <th scope="col">Situación</th>
            <th scope="col">
              <span className="solo-lectores">Acciones</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {morosos.map((moroso) => {
            const puedeAcordar = puedeGestionar && !moroso.acuerdoVigente && moroso.estadoContrato !== "CANCELADO";
            return (
              <tr
                key={moroso.numeroContrato}
                className={moroso.candidatoSuspension ? "bandeja-fila-vencido" : undefined}
              >
                <td>
                  <span className="bandeja-celda-doble">
                    <Link to={`/gestion/contratos/${moroso.numeroContrato}`} className="bandeja-radicado cifra">
                      {moroso.numeroContrato}
                    </Link>
                    <small>
                      {moroso.servicio}
                      {moroso.municipio && ` · ${moroso.municipio}`}
                    </small>
                  </span>
                </td>
                <td>
                  <span className="bandeja-celda-doble">
                    <Link to={`/gestion/usuarios/${moroso.codigoUsuario}`} className="cartera-usuario">
                      {moroso.nombreUsuario}
                    </Link>
                    <small>
                      <span className="cifra">{moroso.documentoUsuario}</span>
                      {moroso.telefono && ` · ${moroso.telefono}`}
                    </small>
                  </span>
                </td>
                <td>
                  <span className="bandeja-celda-doble">
                    <strong className="cartera-deuda cifra">{formatearMoneda(moroso.deuda)}</strong>
                    <small>
                      {moroso.facturasVencidas} {moroso.facturasVencidas === 1 ? "factura vencida" : "facturas vencidas"}
                    </small>
                  </span>
                </td>
                <td>
                  <span className="bandeja-celda-doble">
                    <Etiqueta tono={tonoRangoMora(moroso.rangoMora)}>{moroso.rangoMora}</Etiqueta>
                    <small>
                      {moroso.antiguedadDias} días · desde {formatearFechaCorta(moroso.vencimientoMasAntiguo)}
                    </small>
                  </span>
                </td>
                <td>
                  <span className="cartera-periodos cifra">{moroso.periodosConsecutivos}</span>
                </td>
                <td>
                  <SituacionCobro moroso={moroso} />
                </td>
                <td>
                  {puedeAcordar ? (
                    <Boton
                      a={`/gestion/cartera/acuerdos/nuevo?contrato=${moroso.numeroContrato}`}
                      variante="secundario"
                      className="cartera-accion"
                    >
                      Acuerdo
                    </Boton>
                  ) : (
                    <Link
                      to={`/gestion/contratos/${moroso.numeroContrato}`}
                      className="bandeja-ver"
                      aria-label={`Ver contrato ${moroso.numeroContrato}`}
                    >
                      <Icono nombre="chevron" tamano={18} />
                    </Link>
                  )}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}