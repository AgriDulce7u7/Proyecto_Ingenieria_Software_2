import { Icono } from "../../../componentes/ui/Icono";
import "./administracion.css";

/** Permisos de un rol, tal como los define el backend (RBAC, RF-14). */
export function PermisosRol({ rol, className = "" }) {
  return (
    <div className={`admin-permisos ${className}`.trim()}>
      <span className="antetitulo">Puede</span>
      <ul>
        {rol.permisos.map((permiso) => (
          <li key={permiso}>
            <Icono nombre="check" tamano={15} />
            {permiso}
          </li>
        ))}
      </ul>
      {rol.requiereGestorPqr && <p>Este rol se vincula a un gestor de la Oficina de PQR.</p>}
    </div>
  );
}