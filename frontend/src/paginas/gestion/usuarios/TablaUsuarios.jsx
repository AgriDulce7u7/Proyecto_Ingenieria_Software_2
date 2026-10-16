import { Link, useNavigate } from "react-router-dom";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import { presentarValidacion } from "../../../presentacion/usuarios";

/** Resultado de la búsqueda. Usa los estilos de la tabla de la bandeja (tarjetas en móvil). */
export function TablaUsuarios({ usuarios }) {
  const navegar = useNavigate();
  const abrirDesdeFila = (codigo) => (evento) => {
    if (!evento.target.closest("a")) navegar(`/gestion/usuarios/${codigo}`);
  };

  return (
    <div className="bandeja-tabla-contenedor">
      <table className="bandeja-tabla">
        <thead>
          <tr>
            <th scope="col">Usuario</th>
            <th scope="col">Identificación</th>
            <th scope="col">Municipio</th>
            <th scope="col">Contacto</th>
            <th scope="col">Identidad</th>
            <th scope="col">Estado</th>
            <th scope="col">
              <span className="solo-lectores">Acciones</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {usuarios.map((usuario) => {
            const validacion = presentarValidacion(usuario.estadoValidacion);
            return (
              <tr key={usuario.codigoInterno} onClick={abrirDesdeFila(usuario.codigoInterno)}>
                <td>
                  <span className="bandeja-celda-doble">
                    <Link to={`/gestion/usuarios/${usuario.codigoInterno}`} className="bandeja-radicado">
                      {usuario.nombreCompleto}
                    </Link>
                    <small className="cifra">{usuario.codigoInterno}</small>
                  </span>
                </td>
                <td className="cifra">
                  {usuario.tipoDocumento} {usuario.numeroDocumento}
                </td>
                <td>{usuario.municipio ?? "Sin municipio"}</td>
                <td>
                  <span className="bandeja-celda-doble">
                    <span>{usuario.telefono}</span>
                    <small>{usuario.correo}</small>
                  </span>
                </td>
                <td>
                  <Etiqueta tono={validacion.tono}>{validacion.corta}</Etiqueta>
                </td>
                <td>
                  <Etiqueta tono={usuario.activo ? "exito" : "neutro"}>{usuario.activo ? "Activo" : "Inactivo"}</Etiqueta>
                </td>
                <td>
                  <Link
                    to={`/gestion/usuarios/${usuario.codigoInterno}`}
                    className="bandeja-ver"
                    aria-label={`Ver perfil de ${usuario.nombreCompleto}`}
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