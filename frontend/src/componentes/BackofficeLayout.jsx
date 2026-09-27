import { useEffect, useId, useRef, useState } from "react";
import { Link, NavLink, Outlet, ScrollRestoration } from "react-router-dom";
import { SelectorGestor } from "./SelectorGestor";
import { Logo } from "./ui/Logo";
import "./layouts.css";

const SECCIONES = [
  {
    titulo: "PQR",
    enlaces: [
      { a: "/gestion/pqr", texto: "Bandeja", fin: true },
      { a: "/gestion/alertas", texto: "Mis alertas" },
    ],
  },
  {
    titulo: "Facturación",
    enlaces: [
      { a: "/gestion/facturacion", texto: "Programación", fin: true },
      { a: "/gestion/facturacion/lotes", texto: "Lotes" },
      { a: "/gestion/facturacion/facturas", texto: "Facturas" },
    ],
  },
];

/**
 * Back-office de funcionarios: atención de PQR (F-02) y facturación (F-01).
 * En escritorio el menú es una columna fija; en pantallas pequeñas es un panel lateral
 * que se abre con el botón "Menú" y se cierra al navegar, al tocar fuera o con Esc.
 */
export function BackofficeLayout() {
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
      <header className="gestion-barra">
        <button
          ref={botonMenu}
          type="button"
          className="gestion-boton-menu"
          aria-expanded={menuAbierto}
          aria-controls={idMenu}
          onClick={() => setMenuAbierto((abierto) => !abierto)}
        >
          <span className="icono-menu" aria-hidden="true" />
          Menú
        </button>
        <Link to="/gestion" aria-label="SIGCA, inicio del back-office">
          <Logo compacto />
        </Link>
        <SelectorGestor />
      </header>

      <div
        className="gestion-velo"
        data-visible={menuAbierto}
        onClick={cerrarMenu}
        aria-hidden="true"
      />

      <nav ref={menu} id={idMenu} className="gestion-nav" data-abierto={menuAbierto} aria-label="Back-office">
        <div className="gestion-nav-cabecera">
          <span>SIGCA-EPQ</span>
          <button
            type="button"
            className="gestion-boton-cerrar"
            onClick={() => {
              cerrarMenu();
              botonMenu.current?.focus();
            }}
          >
            Cerrar
          </button>
        </div>
        {SECCIONES.map((seccion) => (
          <div key={seccion.titulo} className="gestion-nav-grupo">
            <h2>{seccion.titulo}</h2>
            <ul>
              {seccion.enlaces.map((enlace) => (
                <li key={enlace.a}>
                  <NavLink to={enlace.a} end={enlace.fin} onClick={cerrarMenu}>
                    {enlace.texto}
                  </NavLink>
                </li>
              ))}
            </ul>
          </div>
        ))}
        <Link to="/" className="gestion-nav-portal" onClick={cerrarMenu}>
          Ver portal ciudadano
        </Link>
      </nav>

      <main className="gestion-contenido">
        <Outlet />
      </main>
      <ScrollRestoration />
    </div>
  );
}