import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { Boton } from "../../../componentes/ui/Boton";
import { primerNombre } from "../../../presentacion/portal";
import { useSesion } from "../../../sesion/useSesion";
import "./cuenta.css";

const PESTANAS = [
  // El detalle de un servicio (/mi-cuenta/contratos/...) se abre desde el resumen y lo mantiene marcado
  {
    a: "/mi-cuenta",
    texto: "Resumen",
    activaEn: (ruta) => ruta === "/mi-cuenta" || ruta.startsWith("/mi-cuenta/contratos"),
  },
  { a: "/mi-cuenta/facturas", texto: "Facturas" },
  { a: "/mi-cuenta/pagos", texto: "Pagos" },
  { a: "/mi-cuenta/datos", texto: "Mis datos" },
];

/**
 * Estructura de «Mi cuenta» en el portal de autogestión (IU-01): saludo, cierre de sesión y pestañas
 * de las secciones. Cada sección se muestra debajo con su propio título.
 */
export function CuentaLayout() {
  const { usuario, cerrarSesion } = useSesion();
  const navegar = useNavigate();
  const { pathname } = useLocation();

  const salir = async () => {
    await cerrarSesion();
    navegar("/", { replace: true });
  };

  return (
    <div className="pagina-portal pagina-portal-normal cuenta">
      <header className="cuenta-encabezado">
        <div>
          <span className="antetitulo">Mi cuenta</span>
          <h1>Hola, {primerNombre(usuario?.nombre)}</h1>
          <p>Consulte sus servicios, descargue sus facturas y pague en línea.</p>
        </div>
        <Boton variante="discreto" icono="salir" onClick={salir}>
          Cerrar sesión
        </Boton>
      </header>

      <nav className="cuenta-pestanas" aria-label="Secciones de mi cuenta">
        {PESTANAS.map((pestana) => (
          <NavLink
            key={pestana.a}
            to={pestana.a}
            end={Boolean(pestana.activaEn)}
            className={({ isActive }) => {
              const activa = pestana.activaEn ? pestana.activaEn(pathname) : isActive;
              return activa ? "active" : undefined;
            }}
          >
            {pestana.texto}
          </NavLink>
        ))}
      </nav>

      <Outlet />
    </div>
  );
}