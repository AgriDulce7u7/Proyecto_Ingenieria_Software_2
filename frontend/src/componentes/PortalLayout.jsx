import { Link, NavLink, Outlet, ScrollRestoration } from "react-router-dom";
import { useSesion } from "../sesion/useSesion";
import { CargaPagina } from "./CargaPagina";
import { Boton } from "./ui/Boton";
import { Logo } from "./ui/Logo";
import "./portal.css";

const ENLACES = [
  { a: "/", texto: "Inicio", fin: true },
  { a: "/radicar", texto: "Radicar PQR" },
  { a: "/consultar", texto: "Consultar estado" },
  { a: "/mi-cuenta", texto: "Mi cuenta" },
];

/** Botón del encabezado según la sesión: la cuenta del ciudadano, el back-office o el ingreso. */
function AccesoEncabezado() {
  const { esCiudadano, esFuncionario } = useSesion();
  if (esCiudadano) {
    return (
      <Boton a="/mi-cuenta" icono="personas" className="portal-acceso">
        Mi cuenta
      </Boton>
    );
  }
  if (esFuncionario) {
    return (
      <Boton a="/gestion" variante="secundario" className="portal-acceso">
        Ir al back-office
      </Boton>
    );
  }
  return (
    <Boton a="/ingresar" variante="secundario" icono="candado" className="portal-acceso">
      Ingresar
    </Boton>
  );
}

/**
 * Portal del ciudadano: radicar y consultar PQR sin iniciar sesión (SWR-08) y, con su cuenta, consultar
 * y pagar sus facturas (portal de autogestión, RF-07).
 */
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
          <AccesoEncabezado />
        </div>
      </header>

      <main id="contenido" tabIndex={-1} className="portal-contenido">
        <CargaPagina>
          <Outlet />
        </CargaPagina>
      </main>

      <footer className="portal-pie">
        <Logo compacto />
        <p>Prototipo académico para Empresas Públicas del Quindío</p>
        <span>
          Ingeniería de Software 2, Universidad del Quindío ·{" "}
          <Link to="/acceso" className="portal-pie-enlace">
            Acceso funcionarios
          </Link>
        </span>
      </footer>
      <ScrollRestoration />
    </div>
  );
}