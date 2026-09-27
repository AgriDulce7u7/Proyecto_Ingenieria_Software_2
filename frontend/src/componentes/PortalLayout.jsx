import { Link, NavLink, Outlet, ScrollRestoration } from "react-router-dom";
import { Boton } from "./ui/Boton";
import { Logo } from "./ui/Logo";
import "./portal.css";

const ENLACES = [
  { a: "/", texto: "Inicio", fin: true },
  { a: "/radicar", texto: "Radicar PQR" },
  { a: "/consultar", texto: "Consultar estado" },
];

/** Portal público del ciudadano: radicar y consultar PQR sin iniciar sesión (SWR-08). */
export function PortalLayout() {
  return (
    <div className="portal">
      <a className="saltar-contenido" href="#contenido">
        Saltar al contenido principal
      </a>

      <header className="portal-encabezado">
        <div className="portal-encabezado-interior">
          <Link to="/" className="portal-marca" aria-label="SIGCA, inicio del portal">
            <Logo />
          </Link>
          <nav className="portal-nav" aria-label="Portal ciudadano">
            {ENLACES.map((enlace) => (
              <NavLink key={enlace.a} to={enlace.a} end={enlace.fin}>
                {enlace.texto}
              </NavLink>
            ))}
          </nav>
          <Boton a="/gestion" variante="secundario" className="portal-acceso">
            Acceso funcionarios
          </Boton>
        </div>
      </header>

      <main id="contenido" tabIndex={-1} className="portal-contenido">
        <Outlet />
      </main>

      <footer className="portal-pie">
        <Logo compacto />
        <p>Prototipo académico para Empresas Públicas del Quindío</p>
        <span>Ingeniería de Software 2, Universidad del Quindío</span>
      </footer>
      <ScrollRestoration />
    </div>
  );
}