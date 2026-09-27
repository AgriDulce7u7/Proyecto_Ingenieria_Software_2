import { useEffect, useId, useRef, useState } from "react";
import { Link, NavLink, Outlet, ScrollRestoration } from "react-router-dom";
import { useSesion } from "../sesion/useSesion";
import { SelectorGestor } from "./SelectorGestor";
import { Icono } from "./ui/Icono";
import { Logo } from "./ui/Logo";
import "./backoffice.css";

const MENU = [
  { a: "/gestion/pqr", texto: "Gestión de PQR", icono: "documento" },
  { a: "/gestion/alertas", texto: "Alertas", icono: "campana" },
  { a: "/gestion/facturacion", texto: "Facturación", icono: "recibo", fin: true },
  { a: "/gestion/facturacion/lotes", texto: "Lotes de facturación", icono: "capas" },
  { a: "/gestion/facturacion/facturas", texto: "Facturas", icono: "documento" },
];

/** Iniciales del gestor para el avatar: "Laura Giraldo Ríos" → "LG". */
const iniciales = (nombre = "") =>
  nombre
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((parte) => parte[0].toUpperCase())
    .join("");

/**
 * Back-office de funcionarios: atención de PQR (F-02) y facturación (F-01).
 * En escritorio el menú es una columna fija; en pantallas pequeñas es un panel lateral
 * que se abre con el botón de menú y se cierra al navegar, al tocar fuera o con Esc.
 */
export function BackofficeLayout() {
  const { gestor } = useSesion();
  const [menuAbierto, setMenuAbierto] = useState(false);
  const botonMenu = useRef(null);
  const menu = useRef(null);
  const idMenu = useId();

  const cerrarMenu = () => setMenuAbierto(false);

  useEffect(() => {
    if (!menuAbierto) {
      return undefined;
    }
    // Al abrir el panel, el foco pasa a la primera opción para navegar con teclado
    menu.current?.querySelector("a")?.focus();
    const alPresionarTecla = (evento) => {
      if (evento.key === "Escape") {
        setMenuAbierto(false);
        botonMenu.current?.focus();
      }
    };
    document.addEventListener("keydown", alPresionarTecla);
    return () => document.removeEventListener("keydown", alPresionarTecla);
  }, [menuAbierto]);

  return (
    <div className="gestion">
      <a className="saltar-contenido" href="#contenido-gestion">
        Saltar al contenido principal
      </a>

      <aside ref={menu} id={idMenu} className="gestion-lateral" data-abierto={menuAbierto}>
        <Link to="/gestion" className="gestion-lateral-marca" aria-label="SIGCA, inicio del back-office" onClick={cerrarMenu}>
          <Logo />
        </Link>
        <nav aria-label="Back-office">
          <ul>
            {MENU.map((opcion) => (
              <li key={opcion.a}>
                <NavLink to={opcion.a} end={opcion.fin} onClick={cerrarMenu}>
                  <Icono nombre={opcion.icono} tamano={19} />
                  <span>{opcion.texto}</span>
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>
        <div className="gestion-lateral-pie">
          <span>Portal ciudadano</span>
          <Link to="/" onClick={cerrarMenu}>
            Ir al portal
            <Icono nombre="flecha" tamano={16} />
          </Link>
        </div>
      </aside>

      <div className="gestion-velo" data-visible={menuAbierto} onClick={cerrarMenu} aria-hidden="true" />

      <div className="gestion-principal">
        <header className="gestion-barra">
          <button
            ref={botonMenu}
            type="button"
            className="gestion-boton-icono gestion-boton-menu"
            aria-label={menuAbierto ? "Cerrar menú" : "Abrir menú"}
            aria-expanded={menuAbierto}
            aria-controls={idMenu}
            onClick={() => setMenuAbierto((abierto) => !abierto)}
          >
            <Icono nombre={menuAbierto ? "cerrar" : "menu"} />
          </button>
          <Link to="/gestion" className="gestion-barra-marca" aria-label="SIGCA, inicio del back-office">
            <span className="logo-marca">
              <Icono nombre="gota" tamano={22} />
            </span>
          </Link>

          <SelectorGestor />
          <Link to="/gestion/alertas" className="gestion-boton-icono gestion-campana" aria-label="Ver alertas">
            <Icono nombre="campana" />
          </Link>
          <span className="gestion-avatar" aria-hidden="true">
            {gestor ? iniciales(gestor.nombre) : "?"}
          </span>
        </header>

        <main id="contenido-gestion" tabIndex={-1} className="gestion-contenido">
          <Outlet />
        </main>
      </div>
      <ScrollRestoration />
    </div>
  );
}