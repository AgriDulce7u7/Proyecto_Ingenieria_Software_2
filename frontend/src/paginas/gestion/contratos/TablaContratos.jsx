import { Link, useNavigate } from "react-router-dom";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import { presentarEstadoContrato } from "../../../presentacion/usuarios";

/** Listado de contratos con los estilos de la tabla de la bandeja (tarjetas en móvil). */
export function TablaContratos({ contratos }) {
  const navegar = useNavigate();
  const abrirDesdeFila = (numero) => (evento) => {
    if (!evento.target.closest("a")) navegar(`/gestion/contratos/${numero}`);
  };

  return (
    <div className="bandeja-tabla-contenedor">
      <table className="bandeja-tabla">
        <thead>
          <tr>
            <th scope="col">Contrato</th>
            <th scope="col">Dirección del servicio</th>
            <th scope="col">Municipio</th>
            <th scope="col">Estado</th>
            <th scope="col">Cartera</th>
            <th scope="col">
              <span className="solo-lectores">Acciones</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {contratos.map((contrato) => {
            const estado = presentarEstadoContrato(contrato.estado);
            return (
              <tr
                key={contrato.numeroContrato}
                className={contrato.candidatoSuspension ? "bandeja-fila-vencido" : undefined}
                onClick={abrirDesdeFila(contrato.numeroContrato)}
              >
                <td>
                  <span className="bandeja-celda-doble">
                    <Link to={`/gestion/contratos/${contrato.numeroContrato}`} className="bandeja-radicado cifra">
                      {contrato.numeroContrato}
                    </Link>
                    <small>{contrato.servicio}</small>
                  </span>
                </td>
                <td>{contrato.direccionServicio}</td>
                <td>{contrato.municipio ?? "Sin municipio"}</td>
                <td>
                  <Etiqueta tono={estado.tono}>{estado.etiqueta}</Etiqueta>
                </td>
                <td>
                  {contrato.candidatoSuspension ? (
                    <span className="contratos-alerta">
                      <Icono nombre="alerta" tamano={14} />
                      Candidato a suspensión
                    </span>
                  ) : (
                    <span className="contratos-al-dia">Sin alerta</span>
                  )}
                </td>
                <td>
                  <Link
                    to={`/gestion/contratos/${contrato.numeroContrato}`}
                    className="bandeja-ver"
                    aria-label={`Ver detalle de ${contrato.numeroContrato}`}
                  >
                    <Icono nombre="chevron" tamano={18} />
                  </Link>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}