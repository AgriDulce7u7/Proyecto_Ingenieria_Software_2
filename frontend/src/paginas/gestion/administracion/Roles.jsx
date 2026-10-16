import { Link } from "react-router-dom";
import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { useFuncionarios, useRoles } from "../../../hooks/useAdministracion";
import { PermisosRol } from "./PermisosRol";
import "./administracion.css";

/** Cuántas cuentas activas tiene cada rol. */
function contarActivas(funcionarios) {
  return funcionarios
    .filter((funcionario) => funcionario.activo)
    .reduce((conteo, funcionario) => ({ ...conteo, [funcionario.rol]: (conteo[funcionario.rol] ?? 0) + 1 }), {});
}

/**
 * Roles del sistema y sus permisos (RBAC, RF-14, RNF-05). Los roles están definidos en el backend:
 * aquí se consultan y se ve cuántas cuentas tiene cada uno. El rol de un funcionario se cambia desde su cuenta.
 */
export function Roles() {
  const roles = useRoles();
  const funcionarios = useFuncionarios();
  const activas = contarActivas(funcionarios.data ?? []);
  const internos = (roles.data ?? []).filter((rol) => rol.interno);
  const ciudadano = (roles.data ?? []).find((rol) => !rol.interno);

  return (
    <>
      <EncabezadoGestion
        antetitulo="Administración"
        titulo="Roles y permisos"
        descripcion="Cada funcionario tiene un rol, y el rol define qué puede consultar y hacer. El backend verifica el permiso en cada operación."
      />

      {roles.isPending && <Cargando texto="Cargando roles…" />}
      <MensajeError error={roles.error} />

      {roles.data && (
        <>
          <ul className="admin-roles">
            {internos.map((rol) => {
              const cantidad = activas[rol.rol] ?? 0;
              return (
                <li key={rol.rol} className="panel admin-rol">
                  <div className="admin-rol-cabecera">
                    <h2>{rol.descripcion}</h2>
                    <span className="cifra">{rol.rol}</span>
                  </div>
                  <PermisosRol rol={rol} />
                  <Link to={`/gestion/administracion/funcionarios?rol=${rol.rol}`} className="admin-rol-pie">
                    {funcionarios.isPending
                      ? "Contando cuentas…"
                      : `${cantidad} ${cantidad === 1 ? "cuenta activa" : "cuentas activas"}`}
                  </Link>
                </li>
              );
            })}
          </ul>
          {ciudadano && (
            <div className="aviso aviso-info admin-ciudadano">
              <p>
                <strong>{ciudadano.descripcion}.</strong> Es el rol de los usuarios del servicio que activan su cuenta en
                el portal ({ciudadano.permisos.join(", ").toLowerCase()}). No se asigna a funcionarios.
              </p>
            </div>
          )}
        </>
      )}
    </>
  );
}